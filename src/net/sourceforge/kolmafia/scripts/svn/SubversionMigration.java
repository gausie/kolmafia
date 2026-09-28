package net.sourceforge.kolmafia.scripts.svn;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy;

public class SubversionMigration {
  private static final Pattern URL_IN_DATABASE =
      Pattern.compile("https?://[\\w.-]+(?:/[\\w.~!$&'()*+,;=:@%-]+)+");

  private static final Pattern PATH_IN_DATABASE =
      Pattern.compile("[\\w.~!$&'()*+,;=@%-]+(?:/[\\w.~!$&'()*+,;=@%-]+)*");

  private SubversionMigration() {}

  public static boolean isLegacyWorkingCopy(File project) {
    return Files.isDirectory(project.toPath().resolve(".svn"))
        && !Files.isRegularFile(project.toPath().resolve(SubversionWorkingCopy.METADATA));
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

        if (identifies(repo, projectName)) {
          return Optional.of(URI.create(repo));
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

    var urls = URL_IN_DATABASE.matcher(text);
    var root = "";
    while (urls.find()) {
      if (urls.group().length() > root.length()) root = urls.group();
    }

    if (root.isEmpty()) return Optional.empty();
    if (identifies(root, project.getName())) return Optional.of(URI.create(root));

    var paths = PATH_IN_DATABASE.matcher(text);
    while (paths.find()) {
      var candidate = root + "/" + paths.group();
      if (identifies(candidate, project.getName())) return Optional.of(URI.create(candidate));
    }

    return Optional.empty();
  }

  private static boolean identifies(String url, String projectName) {
    try {
      return projectName.equals(SVNManager.getFolderUUIDNoRemote(new URI(url)));
    } catch (URISyntaxException e) {
      return false;
    }
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
}
