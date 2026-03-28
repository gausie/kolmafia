package net.sourceforge.kolmafia.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

public class GitHubReleaseTest {

  private static GitHubRelease parseRelease(JSONObject obj) {
    return GitHubRelease.parseRelease(obj);
  }

  @Test
  public void parsesValidRelease() {
    String json =
        """
        {
          "tag_name": "r28970",
          "html_url": "https://github.com/kolmafia/kolmafia/releases/tag/r28970",
          "body": "## What's Changed\\n* Fix something",
          "assets": [
            {
              "name": "KoLmafia-28970.jar",
              "browser_download_url": "https://github.com/kolmafia/kolmafia/releases/download/r28970/KoLmafia-28970.jar"
            }
          ]
        }
        """;

    GitHubRelease release = parseRelease(JSON.parseObject(json));

    assertNotNull(release);
    assertEquals(28970, release.revision());
    assertEquals("r28970", release.tagName());
    assertNotNull(release.jarDownloadUrl());
    assertEquals("## What's Changed\n* Fix something", release.body());
  }

  @Test
  public void returnsNullForNullObject() {
    assertNull(parseRelease(null));
  }

  @Test
  public void returnsNullForMissingTagName() {
    String json =
        """
        {"html_url": "https://example.com"}
        """;
    assertNull(parseRelease(JSON.parseObject(json)));
  }

  @Test
  public void returnsNullForNonRevisionTag() {
    String json =
        """
        {"tag_name": "v1.0.0"}
        """;
    assertNull(parseRelease(JSON.parseObject(json)));
  }

  @Test
  public void returnsNullForNonNumericRevision() {
    String json =
        """
        {"tag_name": "rabc"}
        """;
    assertNull(parseRelease(JSON.parseObject(json)));
  }

  @Test
  public void handlesReleaseWithNoAssets() {
    String json =
        """
        {
          "tag_name": "r28970",
          "html_url": "https://example.com",
          "body": "some notes",
          "assets": []
        }
        """;

    GitHubRelease release = parseRelease(JSON.parseObject(json));

    assertNotNull(release);
    assertEquals(28970, release.revision());
    assertNull(release.jarDownloadUrl());
  }

  @Test
  public void picksJarFromMultipleAssets() {
    String json =
        """
        {
          "tag_name": "r28970",
          "html_url": "https://example.com",
          "assets": [
            {"name": "KoLmafia-28970.deb", "browser_download_url": "https://example.com/deb"},
            {"name": "KoLmafia-28970.jar", "browser_download_url": "https://example.com/jar"},
            {"name": "KoLmafia-28970.dmg", "browser_download_url": "https://example.com/dmg"}
          ]
        }
        """;

    GitHubRelease release = parseRelease(JSON.parseObject(json));

    assertNotNull(release);
    assertEquals("https://example.com/jar", release.jarDownloadUrl());
  }
}
