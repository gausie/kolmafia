package net.sourceforge.kolmafia.scripts.svn.dav;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import internal.network.FakeHttpClientBuilder;
import internal.network.FakeHttpResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionWorkingCopy.ChangeType;
import net.sourceforge.kolmafia.utilities.HttpUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SubversionWorkingCopyTest {
  private static final URI TRUNK =
      URI.create("https://svn.code.sf.net/p/rlbond86-mafia-scripts/code/auto_mushroom/trunk/");

  @TempDir Path temp;

  private record Entry(String body, long revision) {}

  private final Map<String, Entry> contents = new HashMap<>();
  private long headRevision = 38;

  private final FakeHttpClientBuilder builder = new FakeHttpClientBuilder();
  private HttpClient.Builder previous;

  private void useOwnClient() {
    previous = HttpUtilities.getClientBuilder();
    HttpUtilities.setClientBuilder(() -> builder);
    SubversionRepository.resetClient();
  }

  @AfterEach
  public void restoreClient() {
    builder.client.clear();
    if (previous != null) HttpUtilities.setClientBuilder(() -> previous);
    SubversionRepository.resetClient();
  }

  private List<HttpRequest> requests() {
    return builder.client.getRequests();
  }

  private HttpRequest lastRequest() {
    return builder.client.getLastRequest();
  }

  @BeforeEach
  public void setup() {
    useOwnClient();
    contents.clear();
    contents.put("dependencies.txt", new Entry("https://svn.code.sf.net/p/zlib/code", 1));
    contents.put("planting/auto_mushroom.ash", new Entry("// day one", 38));
    headRevision = 38;
    builder.client.setResponseFunc(this::respond);
  }

  private FakeHttpResponse<String> respond(HttpRequest request) {
    var path = request.uri().getPath();
    return switch (request.method()) {
      case "PROPFIND" ->
          path.endsWith("/planting")
              ? new FakeHttpResponse<>(207, new HashMap<>(), listing("planting"))
              : path.contains("/!svn/bc/")
                  ? new FakeHttpResponse<>(207, new HashMap<>(), listing(""))
                  : new FakeHttpResponse<>(207, new HashMap<>(), location());
      case "GET" -> {
        var name = path.substring(path.indexOf("/trunk/") + "/trunk/".length());
        var entry = contents.get(name);
        yield entry == null
            ? new FakeHttpResponse<>(404, new HashMap<>(), "")
            : new FakeHttpResponse<>(200, new HashMap<>(), entry.body());
      }
      default -> new FakeHttpResponse<>(405, new HashMap<>(), "");
    };
  }

  private String location() {
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<D:multistatus xmlns:D=\"DAV:\" xmlns:S=\"http://subversion.tigris.org/xmlns/dav/\">\n"
        + "<D:response><D:href>/p/rlbond86-mafia-scripts/code/auto_mushroom/trunk/</D:href>"
        + "<D:propstat><D:prop><D:version-name>"
        + headRevision
        + "</D:version-name>"
        + "<S:baseline-relative-path>auto_mushroom/trunk</S:baseline-relative-path>"
        + "<S:repository-uuid>a778aa86-501e-4e97-ba74-52c8422b1e88</S:repository-uuid></D:prop>"
        + "<D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response>\n"
        + "</D:multistatus>";
  }

  private String listing(String dir) {
    var base = "/p/rlbond86-mafia-scripts/code/!svn/bc/" + headRevision + "/auto_mushroom/trunk";
    var href = dir.isEmpty() ? base : base + "/" + dir;

    var xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    xml.append("<D:multistatus xmlns:D=\"DAV:\">\n");
    xml.append(collection(href));
    for (var name : contents.keySet()) {
      var parent = name.contains("/") ? name.substring(0, name.lastIndexOf('/')) : "";
      if (!parent.equals(dir)) continue;
      var entry = contents.get(name);
      xml.append(file(base + "/" + name, entry.body().length(), entry.revision()));
    }
    if (dir.isEmpty()) {
      for (var name : contents.keySet()) {
        if (!name.contains("/")) continue;
        xml.append(collection(base + "/" + name.substring(0, name.indexOf('/'))));
      }
    }
    xml.append("</D:multistatus>");
    return xml.toString();
  }

  private String collection(String href) {
    return "<D:response><D:href>"
        + href
        + "/</D:href><D:propstat><D:prop><D:version-name>"
        + headRevision
        + "</D:version-name><D:resourcetype><D:collection/></D:resourcetype></D:prop>"
        + "<D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response>\n";
  }

  private String file(String href, int length, long revision) {
    return "<D:response><D:href>"
        + href
        + "</D:href><D:propstat><D:prop><D:version-name>"
        + revision
        + "</D:version-name><D:resourcetype/><D:getcontentlength>"
        + length
        + "</D:getcontentlength></D:prop>"
        + "<D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response>\n";
  }

  private SubversionRepository repository() throws SubversionException {
    return SubversionRepository.at(TRUNK);
  }

  private java.util.List<String> fetchedSince(int index) {
    return requests().subList(index, requests().size()).stream()
        .filter(r -> r.method().equals("GET"))
        .map(r -> r.uri().getPath())
        .map(p -> p.substring(p.indexOf("/trunk/") + "/trunk/".length()))
        .toList();
  }

  @Nested
  class Checkout {
    @Test
    public void writesEveryFileAndRecordsMetadata() throws SubversionException, IOException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      var changes = copy.checkout(repository());

      assertThat(
          changes.stream().map(SubversionWorkingCopy.Change::path).sorted().toList(),
          contains("dependencies.txt", "planting/auto_mushroom.ash"));
      assertThat(
          Files.readString(temp.resolve("project/planting/auto_mushroom.ash")), is("// day one"));
      assertThat(copy.getRevision(), is(38L));
      assertThat(copy.getUrl(), is(TRUNK));
    }

    @Test
    public void everythingIsAnAdditionOnCheckout() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      var changes = copy.checkout(repository());

      assertThat(
          changes.stream().map(SubversionWorkingCopy.Change::type).distinct().toList(),
          contains(ChangeType.ADDED));
    }

    @Test
    public void metadataSurvivesReopening() throws SubversionException {
      SubversionWorkingCopy.at(temp.resolve("project")).checkout(repository());

      var reopened = SubversionWorkingCopy.at(temp.resolve("project"));

      assertThat(reopened.exists(), is(true));
      assertThat(reopened.getRevision(), is(38L));
      assertThat(reopened.getUrl(), is(TRUNK));
      assertThat(
          reopened.getFiles(),
          containsInAnyOrder("dependencies.txt", "planting/auto_mushroom.ash"));
    }

    @Test
    public void absentWorkingCopyDoesNotExist() throws SubversionException {
      assertThat(SubversionWorkingCopy.at(temp.resolve("nothing")).exists(), is(false));
    }
  }

  @Nested
  class Update {
    @Test
    public void doesNothingWhenNothingChanged() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      copy.checkout(repository());

      assertThat(copy.update(repository()), is(empty()));
    }

    @Test
    public void fetchesOnlyFilesWhoseRevisionMoved() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      copy.checkout(repository());

      headRevision = 39;
      contents.put("planting/auto_mushroom.ash", new Entry("// day two", 39));
      var before = requests().size();
      var changes = copy.update(repository());

      assertThat(
          changes,
          contains(
              new SubversionWorkingCopy.Change("planting/auto_mushroom.ash", ChangeType.UPDATED)));
      assertThat(copy.getRevision(), is(39L));
      assertThat(fetchedSince(before), contains("planting/auto_mushroom.ash"));
    }

    @Test
    public void addsNewFiles() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      copy.checkout(repository());

      headRevision = 39;
      contents.put("planting/extra.ash", new Entry("// extra", 39));
      var changes = copy.update(repository());

      assertThat(
          changes.stream()
              .filter(c -> c.type() == ChangeType.ADDED)
              .map(SubversionWorkingCopy.Change::path)
              .toList(),
          contains("planting/extra.ash"));
    }

    @Test
    public void removesDeletedFilesFromDisk() throws SubversionException, IOException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      copy.checkout(repository());

      headRevision = 39;
      contents.remove("planting/auto_mushroom.ash");
      var changes = copy.update(repository());

      assertThat(
          changes.stream()
              .filter(c -> c.type() == ChangeType.DELETED)
              .map(SubversionWorkingCopy.Change::path)
              .toList(),
          contains("planting/auto_mushroom.ash"));
      assertThat(Files.exists(temp.resolve("project/planting/auto_mushroom.ash")), is(false));
    }

    @Test
    public void restoresAFileTheUserDeleted() throws SubversionException, IOException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      copy.checkout(repository());
      Files.delete(temp.resolve("project/planting/auto_mushroom.ash"));

      var changes = copy.update(repository());

      assertThat(
          changes,
          contains(
              new SubversionWorkingCopy.Change("planting/auto_mushroom.ash", ChangeType.ADDED)));
      assertThat(Files.exists(temp.resolve("project/planting/auto_mushroom.ash")), is(true));
    }

    @Test
    public void refusesToUpdateAnAbsentWorkingCopy() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("nothing"));
      var repository = repository();

      var e = assertThrows(SubversionException.class, () -> copy.update(repository));
      assertThat(e.getMessage(), containsString("No working copy"));
    }
  }

  @Nested
  class Head {
    @Test
    public void knowsWhenItIsCurrent() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));
      copy.checkout(repository());

      assertThat(copy.isAtHead(repository()), is(true));

      headRevision = 40;
      assertThat(copy.isAtHead(repository()), is(false));
    }
  }

  @Nested
  class Safety {
    @Test
    public void refusesPathsEscapingTheWorkingCopy() throws SubversionException {
      var copy = SubversionWorkingCopy.at(temp.resolve("project"));

      var e = assertThrows(SubversionException.class, () -> copy.resolve("../../evil.ash"));
      assertThat(e.getMessage(), containsString("outside the working copy"));
    }

    @Test
    public void rejectsCorruptMetadata() throws IOException {
      var project = temp.resolve("project");
      Files.createDirectories(project);
      Files.writeString(
          project.resolve(SubversionWorkingCopy.METADATA), "{}", StandardCharsets.UTF_8);

      var e = assertThrows(SubversionException.class, () -> SubversionWorkingCopy.at(project));
      assertThat(e.getMessage(), containsString("Malformed working copy metadata"));
    }
  }
}
