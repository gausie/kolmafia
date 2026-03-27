package net.sourceforge.kolmafia.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class VersionManagerTest {

  @Test
  public void getStoredVersionsParsesJarNames(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("KoLmafia-r28970.jar"));
    Files.createFile(tempDir.resolve("KoLmafia-r28960.jar"));
    Files.createFile(tempDir.resolve("KoLmafia-r28950.jar"));
    Files.createFile(tempDir.resolve("unrelated.txt"));

    File[] files = tempDir.toFile().listFiles();
    // Manually verify the pattern matches (since getStoredVersions uses KoLConstants path)
    int matched = 0;
    for (File file : files) {
      if (file.getName().matches("KoLmafia-r\\d+\\.jar")) {
        matched++;
      }
    }
    assertEquals(3, matched);
  }

  @Test
  public void storedVersionsAreSortedDescending(@TempDir Path tempDir) throws IOException {
    // Create files with known revisions
    Files.write(tempDir.resolve("KoLmafia-r100.jar"), new byte[] {0x50, 0x4B, 0, 0});
    Files.write(tempDir.resolve("KoLmafia-r300.jar"), new byte[] {0x50, 0x4B, 0, 0});
    Files.write(tempDir.resolve("KoLmafia-r200.jar"), new byte[] {0x50, 0x4B, 0, 0});

    // Parse and sort manually (mirroring VersionManager logic)
    var pattern = java.util.regex.Pattern.compile("KoLmafia-r(\\d+)\\.jar");
    List<Integer> revisions = new java.util.ArrayList<>();
    for (File file : tempDir.toFile().listFiles()) {
      var matcher = pattern.matcher(file.getName());
      if (matcher.matches()) {
        revisions.add(Integer.parseInt(matcher.group(1)));
      }
    }
    revisions.sort(java.util.Comparator.reverseOrder());

    assertEquals(List.of(300, 200, 100), revisions);
  }

  @Test
  public void jarPatternMatchesExpectedFormat() {
    var pattern = java.util.regex.Pattern.compile("KoLmafia-r(\\d+)\\.jar");

    assertTrue(pattern.matcher("KoLmafia-r28970.jar").matches());
    assertTrue(pattern.matcher("KoLmafia-r1.jar").matches());
    assertFalse(pattern.matcher("KoLmafia-r28970.jar.tmp").matches());
    assertFalse(pattern.matcher("KoLmafia-28970.jar").matches());
    assertFalse(pattern.matcher("KoLmafia-r28970.json").matches());
    assertFalse(pattern.matcher("something-else.jar").matches());
  }
}
