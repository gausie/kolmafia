package net.sourceforge.kolmafia.update;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.StaticEntity;

public record GitHubRelease(
    int revision, String tagName, String jarDownloadUrl, String htmlUrl, String body) {

  private static final String API_BASE = "https://api.github.com/repos/kolmafia/kolmafia/releases";
  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  private static final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(TIMEOUT)
          .followRedirects(HttpClient.Redirect.NORMAL)
          .build();

  public static GitHubRelease fetchLatest() {
    try {
      var request =
          HttpRequest.newBuilder()
              .uri(URI.create(API_BASE + "/latest"))
              .header("Accept", "application/vnd.github+json")
              .header("User-Agent", "KoLmafia")
              .timeout(TIMEOUT)
              .GET()
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        return null;
      }

      return parseRelease(JSON.parseObject(response.body()));
    } catch (Exception e) {
      StaticEntity.printStackTrace(e, "Error checking for updates");
      return null;
    }
  }

  public static GitHubRelease fetchByTag(String tag) {
    try {
      var request =
          HttpRequest.newBuilder()
              .uri(URI.create(API_BASE + "/tags/" + tag))
              .header("Accept", "application/vnd.github+json")
              .header("User-Agent", "KoLmafia")
              .timeout(TIMEOUT)
              .GET()
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        return null;
      }

      return parseRelease(JSON.parseObject(response.body()));
    } catch (Exception e) {
      StaticEntity.printStackTrace(e, "Error fetching release by tag");
      return null;
    }
  }

  public boolean downloadJar(File target) throws IOException {
    if (jarDownloadUrl() == null) {
      return false;
    }

    File tempFile = new File(target.getParent(), target.getName() + ".tmp");

    try {
      var request =
          HttpRequest.newBuilder()
              .uri(URI.create(jarDownloadUrl()))
              .timeout(Duration.ofMinutes(5))
              .GET()
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

      if (response.statusCode() != 200) {
        return false;
      }

      long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);

      try (InputStream in = response.body();
          var out = Files.newOutputStream(tempFile.toPath())) {
        byte[] buffer = new byte[8192];
        long totalRead = 0;
        long lastReportedMB = -1;
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
          totalRead += bytesRead;
          long currentMB = totalRead / (1024 * 1024);
          if (currentMB > lastReportedMB) {
            lastReportedMB = currentMB;
            if (contentLength > 0) {
              long totalMB = contentLength / (1024 * 1024);
              RequestLogger.printLine("  " + currentMB + " / " + totalMB + " MB");
            } else {
              RequestLogger.printLine("  " + currentMB + " MB downloaded");
            }
          }
        }
      }

      // Verify the file looks like a JAR (starts with PK zip signature)
      byte[] header;
      try (var is = Files.newInputStream(tempFile.toPath())) {
        header = is.readNBytes(4);
      }
      if (header.length < 4 || header[0] != 0x50 || header[1] != 0x4B) {
        tempFile.delete();
        return false;
      }

      Files.move(tempFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException e) {
      tempFile.delete();
      throw e;
    } catch (InterruptedException e) {
      tempFile.delete();
      Thread.currentThread().interrupt();
      return false;
    }
  }

  static GitHubRelease parseRelease(JSONObject obj) {
    if (obj == null) {
      return null;
    }

    String tagName = obj.getString("tag_name");
    if (tagName == null || !tagName.startsWith("r")) {
      return null;
    }

    int revision;
    try {
      revision = Integer.parseInt(tagName.substring(1));
    } catch (NumberFormatException e) {
      return null;
    }

    String htmlUrl = obj.getString("html_url");

    // Find the .jar asset
    String jarUrl = null;
    JSONArray assets = obj.getJSONArray("assets");
    if (assets != null) {
      for (int i = 0; i < assets.size(); i++) {
        JSONObject asset = assets.getJSONObject(i);
        String name = asset.getString("name");
        if (name != null && name.endsWith(".jar")) {
          jarUrl = asset.getString("browser_download_url");
          break;
        }
      }
    }

    String body = obj.getString("body");

    return new GitHubRelease(revision, tagName, jarUrl, htmlUrl, body);
  }
}
