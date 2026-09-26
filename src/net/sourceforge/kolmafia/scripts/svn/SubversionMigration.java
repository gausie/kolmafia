package net.sourceforge.kolmafia.scripts.svn;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.scripts.ScriptManager;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy;

public class SubversionMigration {
  private static final Pattern URL_IN_DATABASE =
      Pattern.compile("https?://[\\w.-]+(?:/[\\w.~!$&'()*+,;=:@%-]+)+");

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

    var matcher = URL_IN_DATABASE.matcher(new String(bytes, StandardCharsets.ISO_8859_1));

    var longest = "";
    while (matcher.find()) {
      if (matcher.group().length() > longest.length()) longest = matcher.group();
    }

    return longest.isEmpty() ? Optional.empty() : Optional.of(URI.create(longest));
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
