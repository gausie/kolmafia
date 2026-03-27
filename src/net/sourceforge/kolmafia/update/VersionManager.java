package net.sourceforge.kolmafia.update;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.StaticEntity;
import net.sourceforge.kolmafia.preferences.Preferences;
import net.sourceforge.kolmafia.utilities.InputFieldUtilities;

public class VersionManager {

  private static final Pattern JAR_PATTERN = Pattern.compile("KoLmafia-r(\\d+)\\.jar");

  private VersionManager() {}

  public static File getVersionsDirectory() {
    return KoLConstants.VERSIONS_LOCATION;
  }

  public static List<VersionInfo> getStoredVersions() {
    return getStoredVersions(getVersionsDirectory());
  }

  static List<VersionInfo> getStoredVersions(File dir) {
    if (!dir.exists()) {
      return List.of();
    }

    File[] files = dir.listFiles();
    if (files == null) {
      return List.of();
    }

    List<VersionInfo> versions = new ArrayList<>();
    for (File file : files) {
      Matcher matcher = JAR_PATTERN.matcher(file.getName());
      if (matcher.matches()) {
        int revision = Integer.parseInt(matcher.group(1));
        versions.add(new VersionInfo(revision, file, file.lastModified()));
      }
    }

    versions.sort(Comparator.comparingInt(VersionInfo::revision).reversed());
    return versions;
  }

  public static File getJarPath(int revision) {
    return new File(getVersionsDirectory(), "KoLmafia-r" + revision + ".jar");
  }

  public static boolean hasVersion(int revision) {
    return getJarPath(revision).exists();
  }

  public static File getCurrentJarPath() {
    try {
      CodeSource source = VersionManager.class.getProtectionDomain().getCodeSource();
      if (source != null && source.getLocation() != null) {
        File file = new File(source.getLocation().toURI());
        if (file.isFile() && file.getName().endsWith(".jar")) {
          return file;
        }
      }
    } catch (Exception e) {
      // Running from IDE or exploded classes
    }
    return null;
  }

public static boolean isJpackageInstall() {
    return System.getProperty("jpackage.app-version") != null;
  }

  private static File getMetadataPath(int revision) {
    return new File(getVersionsDirectory(), "KoLmafia-r" + revision + ".json");
  }

  private static void saveMetadata(GitHubRelease release) {
    JSONObject obj = new JSONObject();
    obj.put("revision", release.revision());
    obj.put("body", release.body());
    obj.put("tagName", release.tagName());
    obj.put("htmlUrl", release.htmlUrl());
    obj.put("downloadedAt", System.currentTimeMillis());
    try {
      Files.writeString(getMetadataPath(release.revision()).toPath(), obj.toJSONString());
    } catch (IOException e) {
      // Non-critical, ignore
    }
  }

  public static String loadMetadataBody(int revision) {
    File metaFile = getMetadataPath(revision);
    if (!metaFile.exists()) {
      return null;
    }
    try {
      String content = Files.readString(metaFile.toPath());
      JSONObject obj = JSON.parseObject(content);
      return obj.getString("body");
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean downloadVersion(GitHubRelease release) {
    if (release.jarDownloadUrl() == null) {
      RequestLogger.printLine("No JAR download available for r" + release.revision());
      return false;
    }

    File dir = getVersionsDirectory();
    if (!dir.exists() && !dir.mkdirs()) {
      RequestLogger.printLine("Failed to create versions directory: " + dir.getAbsolutePath());
      return false;
    }

    File target = getJarPath(release.revision());
    if (target.exists()) {
      RequestLogger.printLine("Version r" + release.revision() + " is already downloaded.");
      return true;
    }

    try {
      RequestLogger.printLine("Downloading KoLmafia r" + release.revision() + "...");

      if (!release.downloadJar(target)) {
        RequestLogger.printLine("Download failed for r" + release.revision() + ".");
        return false;
      }

      saveMetadata(release);
      RequestLogger.printLine(
          "Downloaded KoLmafia r"
              + release.revision()
              + " ("
              + (target.length() / 1024 / 1024)
              + " MB)");
      return true;
    } catch (IOException e) {
      StaticEntity.printStackTrace(e, "Error downloading update");
      RequestLogger.printLine("Download failed: " + e.getMessage());
      return false;
    }
  }

  public static boolean deleteVersion(int revision) {
    File jar = getJarPath(revision);
    if (!jar.exists()) {
      RequestLogger.printLine("Version r" + revision + " not found.");
      return false;
    }

    if (revision == StaticEntity.getRevision()) {
      RequestLogger.printLine("Cannot delete the currently running version.");
      return false;
    }

    if (jar.delete()) {
      getMetadataPath(revision).delete();
      RequestLogger.printLine("Deleted version r" + revision + ".");
      return true;
    } else {
      RequestLogger.printLine("Failed to delete version r" + revision + ".");
      return false;
    }
  }

  public static void pruneVersions() {
    int maxKeep = Preferences.getInteger("maxStoredVersions");
    if (maxKeep <= 0) {
      maxKeep = 5;
    }

    // Keep N newest versions + always keep current (not counting toward N).
    // getStoredVersions() returns descending by revision.
    List<VersionInfo> versions = getStoredVersions();
    int currentRevision = StaticEntity.getRevision();
    int kept = 0;

    for (VersionInfo version : versions) {
      if (version.revision() == currentRevision) {
        continue;
      }

      kept++;
      if (kept > maxKeep) {
        deleteVersion(version.revision());
      }
    }
  }

  // --- Restart / bootstrap ---

  public static boolean bootstrapIfNeeded() {
    int selectedVersion = Preferences.getInteger("selectedVersion");
    if (selectedVersion <= 0 || selectedVersion == StaticEntity.getRevision()) {
      return false;
    }

    // Clear the pin before launching so that if the new process crashes,
    // the next launch won't loop. restartInto will re-set it on success.
    Preferences.setInteger("selectedVersion", 0);

    if (!getJarPath(selectedVersion).exists()) {
      return false;
    }

    System.out.println("Switching to selected version r" + selectedVersion + "...");
    return restartInto(selectedVersion, false);
  }

  public static boolean restartInto(int revision) {
    return restartInto(revision, true);
  }

  private static boolean restartInto(int revision, boolean confirm) {
    File jarPath = getJarPath(revision);
    if (!jarPath.exists()) {
      RequestLogger.printLine("JAR file not found: " + jarPath.getAbsolutePath());
      return false;
    }

    if (confirm
        && !InputFieldUtilities.confirm(
            "KoLmafia will restart into r" + revision + ". Continue?")) {
      RequestLogger.printLine("Version switch cancelled.");
      return false;
    }

    String javaBinary = resolveJavaBinary();
    if (javaBinary == null) {
      RequestLogger.printLine("Could not locate Java binary. Please restart manually.");
      return false;
    }

    List<String> command = new ArrayList<>();
    command.add(javaBinary);

    // Forward JVM arguments (-Xmx, -D properties, GC flags, etc.)
    command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());

    command.add("-jar");
    command.add(jarPath.getAbsolutePath());

    try {
      RequestLogger.printLine("Starting KoLmafia r" + revision + "...");

      ProcessBuilder pb = new ProcessBuilder(command);
      pb.inheritIO();
      pb.start();

      Preferences.setInteger("selectedVersion", revision);

      KoLmafia.quit();
      return true;
    } catch (IOException e) {
      StaticEntity.printStackTrace(e, "Error restarting KoLmafia");
      RequestLogger.printLine("Failed to restart: " + e.getMessage());
      return false;
    }
  }

  private static String resolveJavaBinary() {
    try {
      var command = ProcessHandle.current().info().command();
      if (command.isPresent()) {
        return command.get();
      }
    } catch (Exception e) {
      // Fall through
    }

    String javaHome = System.getProperty("java.home");
    if (javaHome != null) {
      String separator = File.separator;
      String binary = javaHome + separator + "bin" + separator + "java";
      if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
        binary += ".exe";
      }
      if (new File(binary).exists()) {
        return binary;
      }
    }

    return null;
  }
}
