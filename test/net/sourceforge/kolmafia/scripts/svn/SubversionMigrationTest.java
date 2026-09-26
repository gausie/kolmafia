package net.sourceforge.kolmafia.scripts.svn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SubversionMigrationTest {
  @TempDir Path temp;

  private Path legacyProject(String name) throws IOException {
    var project = temp.resolve(name);
    Files.createDirectories(project.resolve(".svn"));
    Files.writeString(project.resolve(".svn/wc.db"), "not a real database");
    return project;
  }

  @Nested
  class Detection {
    @Test
    public void recognisesAnOldWorkingCopy() throws IOException {
      var project = legacyProject("old-project");

      assertThat(SubversionMigration.isLegacyWorkingCopy(project.toFile()), is(true));
    }

    @Test
    public void ignoresAMigratedWorkingCopy() throws IOException {
      var project = legacyProject("migrated");
      Files.writeString(
          project.resolve(SubversionWorkingCopy.METADATA),
          "{\"url\":\"https://example.com/repo\"}",
          StandardCharsets.UTF_8);

      assertThat(SubversionMigration.isLegacyWorkingCopy(project.toFile()), is(false));
    }

    @Test
    public void ignoresADirectoryWithNoSvnMetadata() {
      assertThat(
          SubversionMigration.isLegacyWorkingCopy(temp.resolve("plain").toFile()), is(false));
    }
  }

  @Nested
  class RemovingMetadata {
    @Test
    public void deletesTheSvnDirectoryOnly() throws IOException {
      var project = legacyProject("project");
      Files.createDirectories(project.resolve("scripts"));
      Files.writeString(project.resolve("scripts/keep.ash"), "// keep me");

      SubversionMigration.removeLegacyMetadata(project.toFile());

      assertThat(Files.exists(project.resolve(".svn")), is(false));
      assertThat(Files.readString(project.resolve("scripts/keep.ash")), is("// keep me"));
    }

    @Test
    public void toleratesAnAlreadyMigratedProject() throws IOException {
      var project = temp.resolve("clean");
      Files.createDirectories(project);

      SubversionMigration.removeLegacyMetadata(project.toFile());

      assertThat(Files.exists(project), is(true));
    }
  }

  @Nested
  class RecoveringTheUrl {
    @Test
    public void readsTheUrlOutOfAWorkingCopyDatabase() throws IOException {
      var project = legacyProject("rlbond86-mafia-scripts-auto_mushroom-trunk");
      Files.write(
          project.resolve(".svn/wc.db"),
          ("SQLite format 3\u0000"
                  + "https://svn.code.sf.net/p/rlbond86-mafia-scripts/code"
                  + "\u0000auto_mushroom/trunk\u0000")
              .getBytes(StandardCharsets.ISO_8859_1));

      var url = SubversionMigration.urlFor(project.toFile());

      assertThat(
          url.orElse(null),
          is(URI.create("https://svn.code.sf.net/p/rlbond86-mafia-scripts/code")));
    }

    @Test
    public void givesUpWhenThereIsNoUrlToFind() throws IOException {
      var project = legacyProject("mystery");

      assertThat(SubversionMigration.urlFor(project.toFile()).orElse(null), is(nullValue()));
    }
  }
}
