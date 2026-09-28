package net.sourceforge.kolmafia.scripts.svn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SVNManagerTest {
  @Nested
  class ProjectIdentifiers {
    @Test
    public void namesAProjectAfterItsPath() {
      assertThat(
          SVNManager.getFolderUUIDNoRemote(URI.create("https://example.com/some/repo")),
          is("some-repo"));
    }

    @Test
    public void hasNoNameForAnOpaqueUrl() {
      assertThat(SVNManager.getFolderUUIDNoRemote(URI.create("svn:whatever")), is(nullValue()));
    }

    @Test
    public void hasNoNameForAUrlWithoutAPath() {
      assertThat(
          SVNManager.getFolderUUIDNoRemote(URI.create("https://example.com")), is(nullValue()));
    }

    @Test
    public void reportsAMalformedRepositoryUrl() {
      var e = assertThrows(SubversionException.class, () -> SVNManager.getRepoId("not a url"));
      assertThat(e.getMessage(), containsString("not a url"));
    }
  }
}
