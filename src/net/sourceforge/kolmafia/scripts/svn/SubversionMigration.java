package net.sourceforge.kolmafia.scripts.svn;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.scripts.ScriptManager;

public class SubversionMigration {
  private SubversionMigration() {}

  public static boolean isLegacyWorkingCopy(File project) {
    return Files.isDirectory(project.toPath().resolve(".svn"))
        && !Files.isRegularFile(
            project
                .toPath()
                .resolve(net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy.METADATA));
  }

  public static List<File> findLegacyWorkingCopies() {
    var found = new ArrayList<File>();
    var projects = KoLConstants.SVN_LOCATION.listFiles();
    if (projects == null) return found;

    for (var project : projects) {
      if (!project.isDirectory()) continue;
      if (project.getName().startsWith(".")) continue;
      if (isLegacyWorkingCopy(project)) found.add(project);
    }
    return found;
  }

  public static Optional<URI> urlFor(String projectName) {
    return urlFor(new File(KoLConstants.SVN_LOCATION, projectName));
  }

  public static Optional<URI> urlFor(File project) {
    var listed = fromRepoListing(project.getName());
    if (listed.isPresent()) return listed;

    return fromWorkingCopyDatabase(project);
  }

  private static Optional<URI> fromRepoListing(String projectName) {
    var repoFile = KoLConstants.SVN_REPO_FILE;
    if (!repoFile.isFile()) return Optional.empty();

    try {
      var array = JSON.parseArray(Files.readString(repoFile.toPath(), StandardCharsets.UTF_8));
      if (array == null) return Optional.empty();

      for (var element : array) {
        if (!(element instanceof JSONObject entry)) continue;
        if ("git".equals(entry.getString("type"))) continue;

        var repo = entry.getString("repo");
        if (repo == null) continue;

        var url = URI.create(repo);
        var identifier = ScriptManager.getProjectIdentifier(url.getHost(), url.getPath());
        if (projectName.equals(identifier)) {
          return Optional.of(url);
        }
      }
    } catch (IOException | RuntimeException e) {
      return Optional.empty();
    }

    return Optional.empty();
  }

  private static Optional<URI> fromWorkingCopyDatabase(File project) {
    var database = project.toPath().resolve(".svn").resolve("wc.db");
    if (!Files.isRegularFile(database)) return Optional.empty();

    byte[] bytes;
    try {
      bytes = Files.readAllBytes(database);
    } catch (IOException e) {
      return Optional.empty();
    }

    var text = new String(bytes, StandardCharsets.ISO_8859_1);
    var matcher =
        java.util.regex.Pattern.compile("https?://[\\w.-]+(?:/[\\w.~!$&'()*+,;=:@%-]+)+")
            .matcher(text);

    var candidates = new ArrayList<String>();
    while (matcher.find()) {
      candidates.add(matcher.group());
    }
    if (candidates.isEmpty()) return Optional.empty();

    candidates.sort(Comparator.comparingInt(String::length).reversed());
    return Optional.of(URI.create(candidates.getFirst()));
  }

  public static void removeLegacyMetadata(File project) throws IOException {
    var svn = project.toPath().resolve(".svn");
    if (!Files.isDirectory(svn)) return;

    try (Stream<Path> walk = Files.walk(svn)) {
      for (var path : walk.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(path);
      }
    }
  }

  public static void report(List<File> legacy) {
    if (legacy.isEmpty()) return;

    RequestLogger.printLine(
        "KoLmafia no longer uses Subversion working copies to track installed scripts.");

    for (var project : legacy) {
      var url = urlFor(project);
      RequestLogger.printLine(
          url.map(u -> project.getName() + " will be re-fetched from " + u)
              .orElse(
                  project.getName()
                      + " could not be matched to a repository; reinstall it with \"svn checkout\"."));
    }
  }
}
