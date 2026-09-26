package net.sourceforge.kolmafia.scripts.svn;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JOptionPane;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.KoLmafiaCLI;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.RequestThread;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.scripts.ScriptManager;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionException;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionRepository;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy.Change;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy.ChangeType;
import net.sourceforge.kolmafia.utilities.FileUtilities;
import net.sourceforge.kolmafia.utilities.PauseObject;

public class SVNManager extends ScriptManager {
  static final Lock SVN_LOCK = new ReentrantLock();

  private record PendingChange(File project, String relpath, ChangeType type) {}

  private static final List<PendingChange> pendingChanges = new ArrayList<>();
  private static final TreeMap<File, long[]> updateMessages = new TreeMap<>();

  private SVNManager() {}

  public static synchronized void setupLibrary() {}

  private static void initialize() {
    pendingChanges.clear();
    updateMessages.clear();
  }

  private static SubversionRepository repository(URI url) throws SubversionException {
    return SubversionRepository.at(url);
  }

  private static SubversionWorkingCopy workingCopy(File project) throws SubversionException {
    return SubversionWorkingCopy.at(project.toPath());
  }

  public static void doCheckout(URI repo) {
    initialize();

    SubversionRepository repository;
    try {
      repository = repository(repo);
    } catch (SubversionException e) {
      error(e, "Unable to connect with repository at " + repo.getPath());
      return;
    }

    if (validateRepo(repository)) {
      return;
    }

    var uuid = getFolderUUID(repo, repository);
    if (uuid == null) return;

    var project = doDirSetup(uuid);
    if (project == null) {
      RequestLogger.printLine("Could not create directory for " + uuid);
      return;
    }

    RequestLogger.printLine("Starting checkout...");
    try {
      SVN_LOCK.lock();
      var copy = workingCopy(project);
      record(project, copy.checkout(repository));
      RequestLogger.printLine("Checked out at r" + copy.getRevision() + ".");
    } catch (SubversionException e) {
      error(e, "SVN ERROR during checkout operation.  Aborting...");
      return;
    } finally {
      SVN_LOCK.unlock();
    }

    pushUpdates(true);

    if (Preferences.getBoolean("svnInstallDependencies")) checkDependencies();
  }

  public static void doUpdate() {
    final File[] projects = KoLConstants.SVN_LOCATION.listFiles();

    if (projects == null || projects.length == 0) {
      RequestLogger.printLine("No projects currently installed with SVN.");
      return;
    }

    initialize();

    Runnable runMe =
        () -> {
          KoLmafia.updateDisplay("Checking all SVN projects...");

          var behind = checkAllProjects(projects);

          KoLmafia.updateDisplay("Updating all SVN projects...");
          for (var project : behind) {
            if (!KoLmafia.permitsContinue()) {
              return;
            }

            updateProject(project);
            pushUpdates();
          }
        };

    if (SVN_LOCK.tryLock()) {
      try {
        RequestThread.postRequest(runMe);

        showCommitMessages();

        Preferences.setBoolean("_svnUpdated", true);
      } finally {
        SVN_LOCK.unlock();
      }
    } else {
      RequestLogger.printLine("SVN busy; update operation cancelled.");
      return;
    }

    if (Preferences.getBoolean("syncAfterSvnUpdate")) {
      syncAll();
    }

    if (Preferences.getBoolean("svnInstallDependencies")) checkDependencies();
  }

  private static List<File> checkAllProjects(File[] projects) {
    var candidates = new ArrayList<File>();
    for (var project : projects) {
      if (!KoLmafia.permitsContinue()) {
        return List.of();
      }
      if (project.getName().startsWith(".")) {
        continue;
      }
      candidates.add(project);
    }

    if (candidates.isEmpty()) {
      return List.of();
    }

    var poolSize = Math.max(1, Preferences.getInteger("svnThreadPoolSize"));
    var executor = Executors.newFixedThreadPool(poolSize);
    var behind = new ArrayList<File>();

    try {
      var futures =
          executor.invokeAll(
              candidates.stream()
                  .map(project -> (java.util.concurrent.Callable<File>) () -> checkProject(project))
                  .toList());
      for (var future : futures) {
        var project = future.get();
        if (project != null) behind.add(project);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      RequestLogger.printLine("Error while checking projects: " + e.getCause());
    } finally {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }

    behind.sort(File::compareTo);
    return behind;
  }

  private static File checkProject(File project) {
    try {
      var copy = workingCopy(project);
      if (!copy.exists() && migrate(project)) {
        copy = workingCopy(project);
      }
      if (!copy.exists()) {
        RequestLogger.printLine(
            project.getPath()
                + " selected for repository operation but may not have corresponding remote");
        return null;
      }

      var repository = repository(copy.getUrl());
      if (copy.isAtHead(repository)) {
        RequestLogger.printLine(project.getName() + " is at HEAD (r" + copy.getRevision() + ")");
        return null;
      }

      RequestLogger.printLine(
          project.getName()
              + " is at r"
              + copy.getRevision()
              + ", repository is at r"
              + repository.getRevision());
      return project;
    } catch (SubversionException e) {
      RequestLogger.printLine(project.getName() + " not checked - exception: " + e.getMessage());
      return null;
    }
  }

  public static boolean WCAtHead(File f, boolean quiet) {
    try {
      var copy = workingCopy(f);
      if (!copy.exists()) {
        if (!quiet) {
          RequestLogger.printLine(
              f.getPath()
                  + " selected for repository operation but may not have corresponding remote");
        }
        return false;
      }

      var repository = repository(copy.getUrl());
      var atHead = copy.isAtHead(repository);
      if (!quiet) {
        RequestLogger.printLine(
            atHead
                ? f.getName() + " is at HEAD (r" + copy.getRevision() + ")"
                : f.getName()
                    + " is at r"
                    + copy.getRevision()
                    + ", repository is at r"
                    + repository.getRevision());
      }
      return atHead;
    } catch (SubversionException e) {
      error(e, null);
      return true;
    }
  }

  public static void doUpdate(String p) {
    File project = new File(KoLConstants.SVN_LOCATION, p);

    if (!project.exists()) return;

    initialize();

    if (!SVN_LOCK.tryLock()) {
      return;
    }

    try {
      updateProject(project);
      pushUpdates();
      showCommitMessages();
    } finally {
      SVN_LOCK.unlock();
    }

    if (Preferences.getBoolean("svnInstallDependencies")) checkDependencies();
  }

  public static void doUpdate(URI repo) {
    var uuid = getFolderUUIDNoRemote(repo);
    if (uuid == null) return;

    doUpdate(uuid);
  }

  private static void updateProject(File project) {
    try {
      var copy = workingCopy(project);
      if (!copy.exists() && migrate(project)) {
        copy = workingCopy(project);
      }
      if (!copy.exists()) {
        RequestLogger.printLine(project.getName() + " is not a working copy.");
        return;
      }

      var repository = repository(copy.getUrl());
      var from = copy.getRevision();
      var changes = copy.update(repository);

      RequestLogger.printLine(
          changes.isEmpty()
              ? project.getName() + " is at HEAD (r" + copy.getRevision() + ")"
              : project.getName() + " updated to r" + copy.getRevision() + ".");

      record(project, changes);
      if (!changes.isEmpty() && from != copy.getRevision()) {
        updateMessages.put(project, new long[] {from, copy.getRevision()});
      }
    } catch (SubversionException e) {
      error(e, "SVN ERROR during update operation.  Aborting...");
    }
  }

  private static void record(File project, List<Change> changes) {
    for (var change : changes) {
      pendingChanges.add(new PendingChange(project, change.path(), change.type()));
    }
  }

  static boolean migrate(File project) {
    if (!SubversionMigration.isLegacyWorkingCopy(project)) {
      return false;
    }

    var url = SubversionMigration.urlFor(project);
    if (url.isEmpty()) {
      RequestLogger.printLine(
          project.getName()
              + " was installed by an older version of KoLmafia and could not be matched to a"
              + " repository; reinstall it with \"svn checkout\".");
      return false;
    }

    RequestLogger.printLine("Migrating " + project.getName() + " from " + url.get() + "...");
    try {
      SubversionMigration.removeLegacyMetadata(project);
      var copy = workingCopy(project);
      record(project, copy.checkout(repository(url.get())));
      RequestLogger.printLine("Migrated " + project.getName() + " at r" + copy.getRevision() + ".");
      return true;
    } catch (IOException e) {
      RequestLogger.printLine("Could not migrate " + project.getName() + ": " + e.getMessage());
      return false;
    }
  }

  public static void doCleanup() {
    RequestLogger.printLine("Working copies no longer need cleaning up.");
  }

  static boolean validateRepo(SubversionRepository repository) {
    return validateRepo(repository, false);
  }

  private static boolean validateRepo(SubversionRepository repository, boolean quiet) {
    List<SubversionRepository.Entry> entries;
    try {
      entries = repository.list("", repository.getRevision());
    } catch (SubversionException e) {
      if (!quiet) error(e, "Something went wrong while fetching svn directory info");
      return true;
    }

    if (!quiet) RequestLogger.printLine("Validating repo...");

    var failed = false;
    for (var entry : entries) {
      if (entry.isDirectory()) {
        failed |= !permissibles.contains(entry.name());
      } else {
        failed = !entry.name().equals(DEPENDENCIES);
      }
    }

    if (failed && !quiet) {
      KoLmafia.updateDisplay(
          MafiaState.ERROR,
          "The requested repo (" + repository.getLocation().getPath() + ") failed validation.");
    } else if (!quiet) {
      RequestLogger.printLine("Repo validated.");
    }

    return failed;
  }

  private static void pushUpdates() {
    pushUpdates(false);
  }

  private static void pushUpdates(boolean wasCheckout) {
    if (pendingChanges.isEmpty()) {
      RequestLogger.printLine("Done.");
      return;
    }

    var pathsToSkip = doFinalChecks(wasCheckout);
    if (!pathsToSkip.isEmpty()) {
      RequestLogger.printLine("NOTE: Skipping some updates due to user request.");
    }

    RequestLogger.printLine("Pushing local updates...");

    for (var change : pendingChanges) {
      if (isTopLevel(change.relpath())) {
        continue;
      }
      if (!isPermissible(change.relpath())) {
        continue;
      }
      if (pathsToSkip.contains(change.relpath())) {
        RequestLogger.printLine("Skipping " + change.relpath());
        continue;
      }

      var file = new File(change.project(), change.relpath());
      if (shouldPush(change, wasCheckout)) {
        doPush(file, change.relpath());
      } else if (change.type() == ChangeType.DELETED) {
        doDelete(change.relpath());
      }
    }

    RequestLogger.printLine("Done.");
    pendingChanges.clear();
  }

  private static boolean isTopLevel(String relpath) {
    return !relpath.contains("/");
  }

  private static boolean isPermissible(String relpath) {
    var top = relpath.substring(0, relpath.indexOf('/'));
    if (permissibles.contains(top)) {
      return true;
    }
    RequestLogger.printLine(
        "Non-permissible folder in SVN root: " + top + " Stopping local updates.");
    return false;
  }

  private static List<String> doFinalChecks(boolean wasCheckout) {
    if (wasCheckout) {
      return checkExisting();
    }

    var skipFiles = new ArrayList<String>();
    for (var change : pendingChanges) {
      if (change.type() != ChangeType.ADDED) continue;
      if (isTopLevel(change.relpath())) continue;
      skipFiles.add(change.relpath());
    }

    if (skipFiles.isEmpty()) {
      return skipFiles;
    }

    var message = new StringBuilder("<html>New file(s) requesting confirmation:<p>");
    var extra = 0;
    for (var i = 0; i < skipFiles.size(); ++i) {
      if (i > 9) {
        extra += 1;
        continue;
      }
      message.append("<b>file</b>: ").append(skipFiles.get(i)).append("<p>");
    }
    if (extra > 0) {
      message.append("<b>and ").append(extra).append(" more...</b>");
    }
    message.append(
        "<br><b>Only click yes if you trust the author.</b>"
            + "<p>Clicking no will stop the files from being added locally. (until you checkout the project again)");

    if (Preferences.getBoolean("svnAlwaysAdd") || confirm(message, "SVN wants to add new files")) {
      skipFiles.clear();
    }

    return skipFiles;
  }

  private static List<String> checkExisting() {
    var skipFiles = new ArrayList<String>();
    for (var change : pendingChanges) {
      if (change.type() != ChangeType.ADDED) continue;
      if (isTopLevel(change.relpath())) continue;

      var rebase = getRebase(change.relpath());
      if (rebase != null && rebase.exists()) {
        skipFiles.add(change.relpath());
      }
    }

    if (skipFiles.isEmpty()) {
      return skipFiles;
    }

    var message = new StringBuilder("<html>New file(s) will overwrite local files:<p>");
    for (var relpath : skipFiles) {
      var rebase = getRebase(relpath);
      message
          .append("<b>file</b>: ")
          .append(FileUtilities.getRelativePath(KoLConstants.ROOT_LOCATION, rebase))
          .append("<p>");
    }
    message.append(
        "<br>Checking out this project will result in some local files (described above) being overwritten."
            + "<p>Click yes to overwrite them, no to skip installing them.");

    if (Preferences.getBoolean("svnAlwaysOverwrite")
        || confirm(message, "SVN checkout wants to overwrite local files")) {
      skipFiles.clear();
    }

    return skipFiles;
  }

  private static boolean confirm(StringBuilder message, String title) {
    return JOptionPane.showConfirmDialog(
            null, message.toString(), title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
        == JOptionPane.YES_OPTION;
  }

  private static void doPush(File file, String relpath) {
    File rebase = getRebase(relpath);

    if (rebase == null) rebase = new File(KoLConstants.ROOT_LOCATION, relpath);

    rebase.getParentFile().mkdirs();

    RequestLogger.printLine(file.getName() + " => " + rebase.getPath());
    FileUtilities.copyFile(file, rebase);
  }

  private static void doDelete(String relpath) {
    File rebase = getRebase(relpath);

    if (rebase == null) return;

    if (rebase.exists() && !Files.isSymbolicLink(rebase.toPath())) {
      String rerebase = FileUtilities.getRelativePath(KoLConstants.ROOT_LOCATION, rebase);
      if (rebase.delete()) {
        RequestLogger.printLine(rerebase + " => DELETED");
        RequestLogger.updateSessionLog(rerebase + " => DELETED");
      }
    }
  }

  private static boolean shouldPush(PendingChange change, boolean wasCheckout) {
    if (wasCheckout) return true;

    return switch (change.type()) {
      case ADDED -> true;
      case UPDATED -> rebaseExists(change.relpath());
      case DELETED -> false;
    };
  }

  private static boolean rebaseExists(String relpath) {
    File rebase = new File(KoLConstants.ROOT_LOCATION, relpath);

    List<File> matches = KoLmafiaCLI.findScriptFile(rebase.getName());
    if (relpath.startsWith("data")) {
      if (rebase.exists()) matches.add(rebase);
    }
    if (matches.size() > 1) {
      RequestLogger.printLine(
          "WARNING: too many matches for "
              + rebase.getName()
              + " in your namespace; no local files were updated.");
    }
    if (matches.isEmpty()) {
      RequestLogger.printLine(
          "NOTE: no local file named "
              + rebase.getName()
              + " in your namespace; no updates performed for this file.");
    }
    return matches.size() == 1;
  }

  private static void showCommitMessages() {
    for (var entry : updateMessages.entrySet()) {
      var revisions = entry.getValue();
      if (revisions[0] <= 0 || revisions[0] >= revisions[1]) {
        continue;
      }

      RequestLogger.printHtml("Update log for <b>" + entry.getKey().getName() + "</b>:");
      RequestLogger.printLine("------");
      try {
        var copy = workingCopy(entry.getKey());
        var repository = repository(copy.getUrl());
        for (var log : repository.log(revisions[0] + 1, revisions[1])) {
          RequestLogger.printLine("r" + log.revision() + " by " + log.author());
          if (log.message() != null) RequestLogger.printLine(log.message());
          RequestLogger.printLine("------");
        }
      } catch (SubversionException e) {
        error(e, null);
      }
    }
    updateMessages.clear();
  }

  public record Info(
      String url,
      long revision,
      String lastChangedAuthor,
      long lastChangedRev,
      String lastChangedDate) {}

  public static Info doInfo(File project) throws SubversionException {
    var copy = workingCopy(project);
    if (!copy.exists()) {
      throw new SubversionException(project + " is not a working copy");
    }

    var repository = repository(copy.getUrl());
    var log = repository.log(copy.getRevision(), copy.getRevision());
    var latest = log.isEmpty() ? null : log.getFirst();

    return new Info(
        copy.getUrl().toString(),
        copy.getRevision(),
        latest == null ? "" : latest.author(),
        latest == null ? copy.getRevision() : latest.revision(),
        latest == null ? "" : latest.date());
  }

  public static boolean isWorkingCopy(File project) {
    try {
      return workingCopy(project).exists();
    } catch (SubversionException e) {
      return false;
    }
  }

  public static void showInfo(File project) {
    try {
      var info = doInfo(project);

      RequestLogger.printLine("Path: " + project.getName());
      RequestLogger.printLine("URL: " + info.url());
      RequestLogger.printLine("Revision: " + info.revision());
      RequestLogger.printLine("Last Changed Author: " + info.lastChangedAuthor());
      RequestLogger.printLine("Last Changed Rev: " + info.lastChangedRev());
      RequestLogger.printLine("Last Changed Date: " + info.lastChangedDate());
    } catch (SubversionException e) {
      error(e, null);
    }
  }

  public static String getFolderUUID(URI repo) {
    var local = getFolderUUIDNoRemote(repo);
    if (local != null) return local;

    try {
      return repository(repo).getUUID();
    } catch (SubversionException e) {
      error(e, "Unable to connect with repository at " + repo.getPath());
      return null;
    }
  }

  private static String getFolderUUID(URI repo, SubversionRepository repository) {
    var local = getFolderUUIDNoRemote(repo);
    return local != null ? local : repository.getUUID();
  }

  public static String getFolderUUIDNoRemote(URI repo) {
    return getProjectIdentifier(repo.getHost(), repo.getPath());
  }

  static File doDirSetup(String uuid) {
    File makeDir = new File(KoLConstants.SVN_LOCATION, uuid);

    if (!makeDir.mkdirs() && !makeDir.exists()) {
      return null;
    }

    return makeDir;
  }

  public static void deleteInstalledProject(String p) {
    final File project = new File(KoLConstants.SVN_LOCATION, p);

    if (!project.exists()) {
      return;
    }
    deleteInstalledProject(project);
  }

  public static void deleteInstalledProject(final File project) {
    RequestLogger.printLine("Uninstalling project..." + project.getName());
    RequestLogger.updateSessionLog("Uninstalling project..." + project.getName());

    recursiveDelete(project);
    if (project.exists()) {
      RequestThread.runInParallel(
          () -> {
            PauseObject p = new PauseObject();
            p.pause(5000);

            recursiveDelete(project);
          });
    }
    RequestLogger.printLine("Project uninstalled." + project.getName());
    RequestLogger.updateSessionLog("Project uninstalled." + project.getName());
  }

  private static void recursiveDelete(File f) {
    if (f.isDirectory()) {
      var children = f.listFiles();
      if (children != null) {
        for (File c : children) recursiveDelete(c);
      }
    }

    var project = projectRootOf(f);
    if (project != null) {
      var relpath = FileUtilities.getRelativePath(project, f);
      if (!relpath.startsWith(".") && !isTopLevel(relpath)) {
        doDelete(relpath);
      }
    }

    f.delete();
  }

  private static File projectRootOf(File file) {
    var current = file;
    while (current != null) {
      var parent = current.getParentFile();
      if (parent == null) return null;
      if (parent.equals(KoLConstants.SVN_LOCATION)) {
        return current.equals(file) ? null : current;
      }
      current = parent;
    }
    return null;
  }

  public static void incrementProject(String p, int amount) {
    if (amount == 0) return;

    File project = new File(KoLConstants.SVN_LOCATION, p);

    if (!project.exists()) return;

    initialize();

    try {
      SVN_LOCK.lock();
      var copy = workingCopy(project);
      if (!copy.exists()) {
        RequestLogger.printLine(project.getName() + " is not a working copy.");
        return;
      }

      var currentRev = copy.getRevision();
      if (currentRev + amount <= 0) {
        RequestLogger.printLine(
            "At r" + currentRev + "; cannot decrement revision by " + amount + ".");
        return;
      }

      RequestLogger.printLine(
          ((amount > 0) ? "Incrementing" : "Decrementing")
              + " project "
              + project.getName()
              + " from r"
              + currentRev
              + " to r"
              + (currentRev + amount));

      var repository = repository(copy.getUrl());
      record(project, copy.update(repository, currentRev + amount));
    } catch (SubversionException e) {
      error(e, "SVN ERROR during update operation.  Aborting...");
      return;
    } finally {
      SVN_LOCK.unlock();
    }

    pushUpdates();
    showCommitMessages();
  }

  public static void syncAll() {
    if (!KoLmafia.permitsContinue()) return;

    File[] projects = KoLConstants.SVN_LOCATION.listFiles();

    if (projects == null || projects.length == 0) {
      return;
    }

    initialize();

    RequestLogger.printLine("Checking for working copy modifications...");

    var pushed = 0;
    for (File project : projects) {
      if (project.getName().startsWith(".")) continue;

      try {
        var copy = workingCopy(project);
        if (!copy.exists()) continue;

        for (var relpath : copy.getFiles()) {
          if (isTopLevel(relpath)) continue;

          var rebase = getRebase(relpath);
          if (rebase == null) continue;

          var file = new File(project, relpath);
          if (!file.isFile()) continue;

          if (rebaseExists(relpath) && differs(file, rebase)) {
            doPush(file, relpath);
            pushed++;
          }
        }
      } catch (SubversionException e) {
        error(e, null);
        return;
      }
    }

    if (pushed == 0) {
      RequestLogger.printLine("No modifications.");
      return;
    }

    RequestLogger.printLine("Sync complete.");
  }

  private static boolean differs(File left, File right) {
    try {
      return Files.mismatch(left.toPath(), right.toPath()) != -1L;
    } catch (IOException e) {
      return false;
    }
  }

  private static File getRebase(String relpath) {
    File rebase = new File(KoLConstants.ROOT_LOCATION, relpath);

    if (relpath.startsWith("scripts") || relpath.startsWith("relay")) {
      List<File> matches = KoLmafiaCLI.findScriptFile(rebase.getName());

      if (matches.size() == 1) {
        File matched = matches.get(0);
        if (matched.compareTo(rebase) != 0) {
          return null;
        } else {
          return matched;
        }
      }

      if (matches.size() > 1) return null;
    }

    if (rebase.exists()) return rebase;

    return null;
  }

  private static void checkDependencies() {
    if (!KoLmafia.permitsContinue()) return;

    File[] projects = KoLConstants.SVN_LOCATION.listFiles();
    boolean printInstall = true;

    if (projects == null || projects.length == 0) {
      return;
    }

    for (File f : projects) {
      File dep = new File(f, DEPENDENCIES);

      if (dep.exists()) {
        if (printInstall) {
          KoLmafia.updateDisplay("Installing dependencies");
          printInstall = false;
        }
        ScriptManager.installDependencies(dep.toPath());
      }
    }
  }

  public static void error(SubversionException e, String addMessage) {
    RequestLogger.printLine(e.getMessage());
    if (addMessage != null) KoLmafia.updateDisplay(MafiaState.ERROR, addMessage);
  }

  public static URI workingCopyToSVNURL(File wcDir) throws SubversionException {
    var copy = workingCopy(wcDir);
    if (!copy.exists()) {
      throw new SubversionException(wcDir + " is not a working copy");
    }
    return copy.getUrl();
  }

  public static String getRepoId(String repoUrl) throws SubversionException {
    return getFolderUUID(URI.create(repoUrl));
  }

  public static Map<File, long[]> getUpdateMessages() {
    return Map.copyOf(updateMessages);
  }
}
