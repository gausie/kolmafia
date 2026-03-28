package net.sourceforge.kolmafia.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class VersionManagerTest {

  @Test
  public void getStoredVersionsIgnoresNonJarFiles(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("KoLmafia-r28970.jar"));
    Files.createFile(tempDir.resolve("unrelated.txt"));
    Files.createFile(tempDir.resolve("KoLmafia-r28970.json"));

    List<VersionInfo> versions = VersionManager.getStoredVersions(tempDir.toFile());

    assertEquals(1, versions.size());
    assertEquals(28970, versions.get(0).revision());
  }

  @Test
  public void getStoredVersionsReturnsSortedDescending(@TempDir Path tempDir) throws IOException {
    Files.write(tempDir.resolve("KoLmafia-r100.jar"), new byte[] {0x50, 0x4B, 0, 0});
    Files.write(tempDir.resolve("KoLmafia-r300.jar"), new byte[] {0x50, 0x4B, 0, 0});
    Files.write(tempDir.resolve("KoLmafia-r200.jar"), new byte[] {0x50, 0x4B, 0, 0});

    List<VersionInfo> versions = VersionManager.getStoredVersions(tempDir.toFile());

    assertEquals(List.of(300, 200, 100), versions.stream().map(VersionInfo::revision).toList());
  }

  @Test
  public void getStoredVersionsReturnsEmptyForNonexistentDir(@TempDir Path tempDir) {
    List<VersionInfo> versions =
        VersionManager.getStoredVersions(tempDir.resolve("nonexistent").toFile());

    assertTrue(versions.isEmpty());
  }

  @Test
  public void jarPatternMatchesExpectedFormat(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("KoLmafia-r28970.jar"));
    Files.createFile(tempDir.resolve("KoLmafia-r1.jar"));
    Files.createFile(tempDir.resolve("KoLmafia-r28970.jar.tmp"));
    Files.createFile(tempDir.resolve("KoLmafia-28970.jar"));
    Files.createFile(tempDir.resolve("KoLmafia-r28970.json"));
    Files.createFile(tempDir.resolve("something-else.jar"));

    List<VersionInfo> versions = VersionManager.getStoredVersions(tempDir.toFile());

    assertEquals(2, versions.size());
    assertFalse(versions.stream().anyMatch(v -> v.revision() == 0));
  }
}
