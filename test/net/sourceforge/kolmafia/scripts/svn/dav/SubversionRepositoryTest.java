package net.sourceforge.kolmafia.scripts.svn.dav;

import static internal.helpers.Networking.html;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import internal.network.FakeHttpClientBuilder;
import internal.network.FakeHttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import net.sourceforge.kolmafia.scripts.svn.dav.SubversionRepository.Kind;
import net.sourceforge.kolmafia.utilities.HttpUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SubversionRepositoryTest {
  private static final URI TRUNK =
      URI.create("https://svn.code.sf.net/p/rlbond86-mafia-scripts/code/auto_mushroom/trunk/");

  private static final String BC = "/p/rlbond86-mafia-scripts/code/!svn/bc/38/auto_mushroom/trunk";

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
  }

  private void respond(Function<HttpRequest, FakeHttpResponse<String>> func) {
    builder.client.setResponseFunc(func);
  }

  private static FakeHttpResponse<String> ok(int code, String body) {
    return new FakeHttpResponse<>(code, new HashMap<>(), body);
  }

  private static FakeHttpResponse<String> routed(HttpRequest request) {
    var path = request.uri().getPath();
    return switch (request.method()) {
      case "PROPFIND" ->
          path.endsWith("/planting")
              ? ok(207, html("request/svn/test_svn_list_planting.xml"))
              : path.contains("/!svn/bc/")
                  ? ok(207, html("request/svn/test_svn_list_root.xml"))
                  : ok(207, html("request/svn/test_svn_location.xml"));
      case "REPORT" -> ok(200, html("request/svn/test_svn_log_report.xml"));
      case "GET" -> ok(200, "// a script");
      default -> ok(405, "");
    };
  }

  private SubversionRepository trunk() throws SubversionException {
    respond(SubversionRepositoryTest::routed);
    return SubversionRepository.at(TRUNK);
  }

  @Nested
  class Location {
    @Test
    public void readsRevisionAndUuidFromServer() throws SubversionException {
      var repo = trunk();

      assertThat(repo.getRevision(), is(38L));
      assertThat(repo.getUUID(), is("a778aa86-501e-4e97-ba74-52c8422b1e88"));
    }

    @Test
    public void sendsDepthZeroPropfind() throws SubversionException {
      trunk();

      var request = lastRequest();
      assertThat(request.method(), is("PROPFIND"));
      assertThat(request.headers().firstValue("Depth").orElse(null), is("0"));
    }

    @Test
    public void rejectsUnsupportedProtocol() {
      var e =
          assertThrows(
              SubversionException.class,
              () -> SubversionRepository.at(URI.create("svn://example.com/repo")));
      assertThat(e.getMessage(), containsString("Only http and https"));
    }

    @Test
    public void reportsMissingPath() {
      respond(request -> ok(404, "<html>not found</html>"));
      var e = assertThrows(SubversionException.class, () -> SubversionRepository.at(TRUNK));
      assertThat(e.getMessage(), containsString("404"));
    }

    @Test
    public void findsRepositoryRootWhenTheCheckoutPathIsEscaped() throws SubversionException {
      respond(request -> ok(207, html("request/svn/test_svn_location_encoded.xml")));
      var repo =
          SubversionRepository.at(
              URI.create(
                  "https://svn.code.sf.net/p/eodscascension/code-0/scripts/EoD%20SC%20Ascension"));

      respond(request -> ok(207, html("request/svn/test_svn_list_encoded.xml")));
      repo.list("", 58);

      assertThat(
          lastRequest().uri().getRawPath(),
          is("/p/eodscascension/code-0/!svn/bc/58/scripts/EoD%20SC%20Ascension"));
    }
  }

  @Nested
  class Listing {
    @Test
    public void listsDirectoryWithoutTheDirectoryItself() throws SubversionException {
      var entries = trunk().list("", 38);

      assertThat(
          entries.stream().map(SubversionRepository.Entry::name).toList(),
          contains("dependencies.txt", "planting"));
    }

    @Test
    public void distinguishesFilesFromDirectories() throws SubversionException {
      var entries = trunk().list("", 38);

      var file =
          entries.stream().filter(e -> e.name().equals("dependencies.txt")).findFirst().get();
      var directory = entries.stream().filter(e -> e.name().equals("planting")).findFirst().get();

      assertThat(file.kind(), is(Kind.FILE));
      assertThat(file.revision(), is(1L));
      assertThat(directory.kind(), is(Kind.DIRECTORY));
      assertThat(directory.isDirectory(), is(true));
    }

    @Test
    public void pinsListingToRequestedRevision() throws SubversionException {
      trunk().list("", 38);

      assertThat(lastRequest().uri().getPath(), is(BC));
    }

    @Test
    public void decodesEscapedPathSegments() throws SubversionException {
      respond(request -> ok(207, html("request/svn/test_svn_location_repo_root.xml")));
      var repo =
          SubversionRepository.at(URI.create("https://svn.code.sf.net/p/eodscascension/code-0/"));

      respond(request -> ok(207, html("request/svn/test_svn_list_encoded.xml")));
      var entries = repo.list("scripts/EoD SC Ascension", 58);

      assertThat(
          entries.stream().map(SubversionRepository.Entry::path).toList(),
          contains(
              "scripts/EoD SC Ascension/EoDSCDay1.ash",
              "scripts/EoD SC Ascension/EoDSCDay2.ash",
              "scripts/EoD SC Ascension/EoDSCDay3.ash",
              "scripts/EoD SC Ascension/EoDSCDay4.ash",
              "scripts/EoD SC Ascension/Passive Scripts",
              "scripts/EoD SC Ascension/EoDSCJustQuests.ash"));
    }

    @Test
    public void listsEntriesWhenTheServerLeavesPunctuationUnescaped() throws SubversionException {
      respond(request -> ok(207, html("request/svn/test_svn_location_repo_root.xml")));
      var repo =
          SubversionRepository.at(URI.create("https://svn.code.sf.net/p/eodscascension/code-0/"));

      respond(request -> ok(207, html("request/svn/test_svn_list_punctuation.xml")));
      var entries = repo.list("scripts/Bob's Scripts (v2)", 58);

      assertThat(
          entries.stream().map(SubversionRepository.Entry::path).toList(),
          contains("scripts/Bob's Scripts (v2)/run.ash", "scripts/Bob's Scripts (v2)/a+b.ash"));
    }

    @Test
    public void keepsALiteralPlusInAFileName() throws SubversionException {
      respond(request -> ok(207, html("request/svn/test_svn_location_repo_root.xml")));
      var repo =
          SubversionRepository.at(URI.create("https://svn.code.sf.net/p/eodscascension/code-0/"));

      respond(request -> ok(207, html("request/svn/test_svn_list_punctuation.xml")));
      var entries = repo.list("scripts/Bob's Scripts (v2)", 58);

      assertThat(entries.getLast().name(), is("a+b.ash"));
    }

    @Test
    public void descendsIntoSubdirectories() throws SubversionException {
      var entries = trunk().listRecursively("", 38);

      assertThat(
          entries.stream().map(SubversionRepository.Entry::path).sorted().toList(),
          contains(
              "dependencies.txt",
              "planting",
              "planting/auto_mushroom.ash",
              "planting/auto_mushroom.html",
              "planting/auto_mushroom.txt"));
    }
  }

  @Nested
  class Fetching {
    @Test
    public void readsFileAtPinnedRevision() throws SubversionException {
      var body = trunk().fetch("planting/auto_mushroom.ash", 38);

      assertThat(new String(body, StandardCharsets.UTF_8), is("// a script"));
      assertThat(lastRequest().uri().getPath(), is(BC + "/planting/auto_mushroom.ash"));
    }

    @Test
    public void escapesAwkwardFileNames() throws SubversionException {
      trunk().fetch("some dir/a+b.ash", 38);

      assertThat(lastRequest().uri().getRawPath(), is(BC + "/some%20dir/a%2Bb.ash"));
    }

    @Test
    public void reportsMissingFile() throws SubversionException {
      var repo = trunk();
      respond(request -> request.method().equals("GET") ? ok(404, "") : routed(request));

      var e = assertThrows(SubversionException.class, () -> repo.fetch("gone.ash", 38));
      assertThat(e.getMessage(), containsString("404"));
    }
  }

  @Nested
  class Log {
    @Test
    public void readsCommitMessages() throws SubversionException {
      List<SubversionRepository.LogEntry> entries = trunk().log(30, 38);

      assertThat(entries, hasSize(9));
      assertThat(entries.getFirst().revision(), is(30L));
      assertThat(entries.getFirst().author(), is("rlbond86"));
      assertThat(entries.getFirst().message(), containsString("skeleton bone"));
    }

    @Test
    public void asksForAuthorAndMessageOnly() throws SubversionException {
      trunk().log(30, 38);

      var request = lastRequest();
      assertThat(request.method(), is("REPORT"));
      assertThat(request.uri().getPath(), equalTo(BC));
    }
  }
}
