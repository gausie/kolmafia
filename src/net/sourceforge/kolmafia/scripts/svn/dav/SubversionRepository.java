package net.sourceforge.kolmafia.scripts.svn.dav;

import static net.sourceforge.kolmafia.scripts.svn.dav.DavXml.DAV_NS;
import static net.sourceforge.kolmafia.scripts.svn.dav.DavXml.SVN_DAV_NS;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.kolmafia.utilities.HttpUtilities;

public class SubversionRepository {
  public enum Kind {
    FILE,
    DIRECTORY
  }

  public record Entry(String path, Kind kind, long revision, long size) {
    public String name() {
      int slash = path.lastIndexOf('/');
      return slash == -1 ? path : path.substring(slash + 1);
    }

    public boolean isDirectory() {
      return kind == Kind.DIRECTORY;
    }
  }

  public record LogEntry(long revision, String author, String date, String message) {}

  private static final String PROPFIND_ENTRIES =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <D:propfind xmlns:D="DAV:">
      <D:prop><D:version-name/><D:resourcetype/><D:getcontentlength/></D:prop>
      </D:propfind>\
      """;

  private static final String PROPFIND_LOCATION =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <D:propfind xmlns:D="DAV:" xmlns:S="http://subversion.tigris.org/xmlns/dav/">
      <D:prop><D:version-name/><S:baseline-relative-path/><S:repository-uuid/></D:prop>
      </D:propfind>\
      """;

  private final URI location;
  private final String repositoryPath;
  private final String relativePath;
  private final String uuid;
  private final long revision;

  private SubversionRepository(
      URI location, String repositoryPath, String relativePath, String uuid, long revision) {
    this.location = location;
    this.repositoryPath = repositoryPath;
    this.relativePath = relativePath;
    this.uuid = uuid;
    this.revision = revision;
  }

  public static SubversionRepository at(URI location) throws SubversionException {
    var scheme = location.getScheme();
    if (scheme == null || !scheme.equals("http") && !scheme.equals("https")) {
      throw new SubversionException(
          "Only http and https Subversion URLs are supported: " + location);
    }

    var document = DavXml.parse(request(location, "PROPFIND", PROPFIND_LOCATION, "0"));
    var responses = DavXml.descendants(document, DAV_NS, "response");
    if (responses.isEmpty()) {
      throw new SubversionException("No such Subversion path: " + location);
    }

    var prop = DavXml.successfulProp(responses.getFirst());
    if (prop == null) {
      throw new SubversionException("No such Subversion path: " + location);
    }

    var href = DavXml.text(responses.getFirst(), DAV_NS, "href");
    var relative = DavXml.text(prop, SVN_DAV_NS, "baseline-relative-path");
    var uuid = DavXml.text(prop, SVN_DAV_NS, "repository-uuid");
    var versionName = DavXml.text(prop, DAV_NS, "version-name");
    if (href == null || relative == null || versionName == null) {
      throw new SubversionException("Server at " + location + " is not a Subversion repository");
    }

    return new SubversionRepository(
        location, repositoryPath(href, relative), trimSlashes(relative), uuid, parse(versionName));
  }

  public URI getLocation() {
    return location;
  }

  public String getUUID() {
    return uuid;
  }

  public long getRevision() {
    return revision;
  }

  public long getLatestRevision() throws SubversionException {
    return at(location).getRevision();
  }

  public List<Entry> list(String path, long revision) throws SubversionException {
    var target = pinned(path, revision);
    var document = DavXml.parse(request(target, "PROPFIND", PROPFIND_ENTRIES, "1"));

    var entries = new ArrayList<Entry>();
    var base = trimSlashes(target.getRawPath());
    for (var response : DavXml.descendants(document, DAV_NS, "response")) {
      var href = DavXml.text(response, DAV_NS, "href");
      var prop = DavXml.successfulProp(response);
      if (href == null || prop == null) continue;

      var trimmed = trimSlashes(href);
      if (trimmed.equals(base)) continue;
      if (!trimmed.startsWith(base + "/")) continue;

      var versionName = DavXml.text(prop, DAV_NS, "version-name");
      if (versionName == null) continue;

      var collection = DavXml.child(prop, DAV_NS, "resourcetype");
      var kind =
          collection != null && DavXml.child(collection, DAV_NS, "collection") != null
              ? Kind.DIRECTORY
              : Kind.FILE;
      var length = DavXml.text(prop, DAV_NS, "getcontentlength");

      entries.add(
          new Entry(
              join(path, decode(trimSlashes(trimmed.substring(base.length())))),
              kind,
              parse(versionName),
              length == null || length.isEmpty() ? 0 : parse(length)));
    }
    return entries;
  }

  public List<Entry> listRecursively(String path, long revision) throws SubversionException {
    var found = new ArrayList<Entry>();
    var pending = new ArrayList<String>();
    pending.add(path);

    while (!pending.isEmpty()) {
      var next = pending.removeLast();
      for (var entry : list(next, revision)) {
        found.add(entry);
        if (entry.isDirectory()) {
          pending.add(entry.path());
        }
      }
    }
    return found;
  }

  public byte[] fetch(String path, long revision) throws SubversionException {
    var target = pinned(path, revision);

    HttpResponse<byte[]> response;
    try {
      response = send(target, "GET", null, null, BodyHandlers.ofByteArray());
    } catch (IOException | InterruptedException e) {
      throw new SubversionException("Could not read " + target, e);
    }

    if (response.statusCode() != 200) {
      throw new SubversionException(
          "Subversion server returned " + response.statusCode() + " for " + target);
    }

    return response.body();
  }

  public List<LogEntry> log(long from, long to) throws SubversionException {
    var body =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <S:log-report xmlns:S="svn:">
        <S:start-revision>%d</S:start-revision>
        <S:end-revision>%d</S:end-revision>
        <S:revprop>svn:author</S:revprop>
        <S:revprop>svn:date</S:revprop>
        <S:revprop>svn:log</S:revprop>
        </S:log-report>\
        """
            .formatted(from, to);

    var document = DavXml.parse(request(pinned("", to), "REPORT", body, null));

    var entries = new ArrayList<LogEntry>();
    for (var item : DavXml.descendants(document, "svn:", "log-item")) {
      var versionName = DavXml.text(item, DAV_NS, "version-name");
      if (versionName == null) continue;
      entries.add(
          new LogEntry(
              parse(versionName),
              DavXml.text(item, DAV_NS, "creator-displayname"),
              DavXml.text(item, "svn:", "date"),
              DavXml.text(item, DAV_NS, "comment")));
    }
    return entries;
  }

  private URI pinned(String path, long revision) {
    var suffix = join(relativePath, trimSlashes(path));
    var target =
        repositoryPath + "/!svn/bc/" + revision + (suffix.isEmpty() ? "" : "/" + encode(suffix));
    return URI.create(location.getScheme() + "://" + location.getRawAuthority() + target);
  }

  private static String repositoryPath(String href, String relative) {
    var full = trimSlashes(href);
    var tail = trimSlashes(relative);
    if (tail.isEmpty() || !full.endsWith(tail)) {
      return "/" + full;
    }
    return "/" + trimSlashes(full.substring(0, full.length() - tail.length()));
  }

  private static String join(String left, String right) {
    if (left == null || left.isEmpty()) return right;
    if (right == null || right.isEmpty()) return left;
    return trimSlashes(left) + "/" + trimSlashes(right);
  }

  private static String trimSlashes(String value) {
    int start = 0;
    int end = value.length();
    while (start < end && value.charAt(start) == '/') start++;
    while (end > start && value.charAt(end - 1) == '/') end--;
    return value.substring(start, end);
  }

  private static String encode(String path) {
    var parts = path.split("/");
    var encoded = new StringBuilder();
    for (var part : parts) {
      if (part.isEmpty()) continue;
      if (!encoded.isEmpty()) encoded.append('/');
      encoded.append(URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20"));
    }
    return encoded.toString();
  }

  private static String decode(String path) {
    return URLDecoder.decode(path, StandardCharsets.UTF_8);
  }

  private static long parse(String value) throws SubversionException {
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      throw new SubversionException("Unexpected revision \"" + value + "\"", e);
    }
  }

  private static String request(URI target, String method, String body, String depth)
      throws SubversionException {
    HttpResponse<String> response;
    try {
      response = send(target, method, body, depth, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new SubversionException("Could not reach " + target, e);
    }

    if (response.statusCode() != 200 && response.statusCode() != 207) {
      throw new SubversionException(
          "Subversion server returned " + response.statusCode() + " for " + target);
    }

    return response.body();
  }

  private static <T> HttpResponse<T> send(
      URI target, String method, String body, String depth, HttpResponse.BodyHandler<T> handler)
      throws IOException, InterruptedException {
    var builder = HttpRequest.newBuilder(target).timeout(Duration.ofSeconds(60));
    if (depth != null) {
      builder.header("Depth", depth);
    }
    if (body == null) {
      builder.method(method, BodyPublishers.noBody());
    } else {
      builder.header("Content-Type", "text/xml; charset=utf-8");
      builder.method(method, BodyPublishers.ofString(body, StandardCharsets.UTF_8));
    }
    return HttpUtilities.getClientBuilder().build().send(builder.build(), handler);
  }
}
