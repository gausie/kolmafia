package net.sourceforge.kolmafia.scripts.svn.dav;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.kolmafia.request.GenericRequest;
import net.sourceforge.kolmafia.utilities.HttpUtilities;
import net.sourceforge.kolmafia.utilities.ResettingHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SubversionRepository {
  public enum Kind {
    FILE,
    DIRECTORY
  }

  public record Entry(String path, Kind kind, long revision) {
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
      <D:prop><D:version-name/><D:resourcetype/></D:prop>
      </D:propfind>\
      """;

  private static final String PROPFIND_LOCATION =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <D:propfind xmlns:D="DAV:" xmlns:S="http://subversion.tigris.org/xmlns/dav/">
      <D:prop><D:version-name/><S:baseline-relative-path/><S:repository-uuid/></D:prop>
      </D:propfind>\
      """;

  private static ResettingHttpClient client;

  private static synchronized ResettingHttpClient getClient() {
    if (client == null) {
      client = new ResettingHttpClient(() -> HttpUtilities.getClientBuilder().build());
    }
    return client;
  }

  public static synchronized void resetClient() {
    client = null;
  }

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

    var document = parse(request(location, "PROPFIND", PROPFIND_LOCATION, "0"));
    var responses = descendants(document, DAV_NS, "response");
    if (responses.isEmpty()) {
      throw new SubversionException("No such Subversion path: " + location);
    }

    var prop = successfulProp(responses.getFirst());
    if (prop == null) {
      throw new SubversionException("No such Subversion path: " + location);
    }

    var href = text(responses.getFirst(), DAV_NS, "href");
    var relative = text(prop, SVN_DAV_NS, "baseline-relative-path");
    var uuid = text(prop, SVN_DAV_NS, "repository-uuid");
    var versionName = text(prop, DAV_NS, "version-name");
    if (href == null || relative == null || versionName == null) {
      throw new SubversionException("Server at " + location + " is not a Subversion repository");
    }

    return new SubversionRepository(
        location,
        repositoryPath(href, relative),
        trimSlashes(relative),
        uuid,
        parseRevision(versionName));
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

  public List<Entry> list(String path, long revision) throws SubversionException {
    var target = pinned(path, revision);
    var document = parse(request(target, "PROPFIND", PROPFIND_ENTRIES, "1"));

    var entries = new ArrayList<Entry>();
    var base = trimSlashes(decode(target.getRawPath()));
    for (var response : descendants(document, DAV_NS, "response")) {
      var href = text(response, DAV_NS, "href");
      var prop = successfulProp(response);
      if (href == null || prop == null) continue;

      var trimmed = trimSlashes(decode(href));
      if (trimmed.equals(base)) continue;
      if (!trimmed.startsWith(base + "/")) continue;

      var versionName = text(prop, DAV_NS, "version-name");
      if (versionName == null) continue;

      var collection = child(prop, DAV_NS, "resourcetype");
      var kind =
          collection != null && child(collection, DAV_NS, "collection") != null
              ? Kind.DIRECTORY
              : Kind.FILE;
      entries.add(
          new Entry(
              join(path, trimSlashes(trimmed.substring(base.length()))),
              kind,
              parseRevision(versionName)));
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

    var document = parse(request(pinned("", to), "REPORT", body, null));

    var entries = new ArrayList<LogEntry>();
    for (var item : descendants(document, "svn:", "log-item")) {
      var versionName = text(item, DAV_NS, "version-name");
      if (versionName == null) continue;
      entries.add(
          new LogEntry(
              parseRevision(versionName),
              text(item, DAV_NS, "creator-displayname"),
              text(item, "svn:", "date"),
              text(item, DAV_NS, "comment")));
    }
    return entries;
  }

  private URI pinned(String path, long revision) {
    var suffix = join(relativePath, trimSlashes(path));
    var root = encode(repositoryPath);
    var target =
        (root.isEmpty() ? "" : "/" + root)
            + "/!svn/bc/"
            + revision
            + (suffix.isEmpty() ? "" : "/" + encode(suffix));
    return URI.create(location.getScheme() + "://" + location.getRawAuthority() + target);
  }

  private static String repositoryPath(String href, String relative) {
    var full = trimSlashes(decode(href));
    var tail = trimSlashes(relative);
    if (tail.isEmpty() || !full.endsWith(tail)) {
      return full;
    }
    return trimSlashes(full.substring(0, full.length() - tail.length()));
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
    if (path.indexOf('%') == -1) return path;

    var bytes = new ByteArrayOutputStream();
    var i = 0;
    while (i < path.length()) {
      if (path.charAt(i) == '%' && i + 2 < path.length()) {
        var high = Character.digit(path.charAt(i + 1), 16);
        var low = Character.digit(path.charAt(i + 2), 16);
        if (high >= 0 && low >= 0) {
          bytes.write(high << 4 | low);
          i += 3;
          continue;
        }
      }

      var codePoint = path.codePointAt(i);
      bytes.writeBytes(new String(Character.toChars(codePoint)).getBytes(StandardCharsets.UTF_8));
      i += Character.charCount(codePoint);
    }
    return bytes.toString(StandardCharsets.UTF_8);
  }

  private static long parseRevision(String value) throws SubversionException {
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
    builder.header("User-Agent", GenericRequest.getUserAgent());
    return getClient().send(builder.build(), handler);
  }

  static final String DAV_NS = "DAV:";
  private static final String SVN_DAV_NS = "http://subversion.tigris.org/xmlns/dav/";

  static Document parse(String body) throws SubversionException {
    try {
      var factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      var builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    } catch (ParserConfigurationException e) {
      throw new SubversionException("Could not configure XML parser", e);
    } catch (Exception e) {
      throw new SubversionException("Malformed response from Subversion server", e);
    }
  }

  private static List<Element> children(Element parent, String namespace, String localName) {
    var found = new ArrayList<Element>();
    var nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      var node = nodes.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) continue;
      var element = (Element) node;
      if (!localName.equals(element.getLocalName())) continue;
      if (!namespace.equals(element.getNamespaceURI())) continue;
      found.add(element);
    }
    return found;
  }

  private static Element child(Element parent, String namespace, String localName) {
    var found = children(parent, namespace, localName);
    return found.isEmpty() ? null : found.getFirst();
  }

  private static List<Element> descendants(Document document, String namespace, String localName) {
    var found = new ArrayList<Element>();
    var nodes = document.getElementsByTagNameNS(namespace, localName);
    for (int i = 0; i < nodes.getLength(); i++) {
      found.add((Element) nodes.item(i));
    }
    return found;
  }

  private static String text(Element parent, String namespace, String localName) {
    var element = child(parent, namespace, localName);
    return element == null ? null : element.getTextContent();
  }

  private static Element successfulProp(Element response) {
    for (var propstat : children(response, DAV_NS, "propstat")) {
      var status = text(propstat, DAV_NS, "status");
      if (status == null || !status.contains(" 200 ")) continue;
      return child(propstat, DAV_NS, "prop");
    }
    return null;
  }
}
