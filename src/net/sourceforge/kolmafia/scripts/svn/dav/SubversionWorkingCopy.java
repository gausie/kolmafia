package net.sourceforge.kolmafia.scripts.svn.dav;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SubversionWorkingCopy {
  public static final String METADATA = ".kolmafia-svn.json";

  public enum ChangeType {
    ADDED,
    UPDATED,
    DELETED
  }

  public record Change(String path, ChangeType type) {}

  private final Path root;
  private URI url;
  private String uuid;
  private long revision;
  private final Map<String, Long> files = new TreeMap<>();

  private SubversionWorkingCopy(Path root) {
    this.root = root;
  }

  public static SubversionWorkingCopy at(Path root) throws SubversionException {
    var copy = new SubversionWorkingCopy(root);
    var metadata = root.resolve(METADATA);
    if (!Files.isRegularFile(metadata)) {
      return copy;
    }

    JSONObject parsed;
    try {
      parsed = JSON.parseObject(Files.readString(metadata, StandardCharsets.UTF_8));
    } catch (IOException | RuntimeException e) {
      throw new SubversionException("Could not read " + metadata, e);
    }

    if (parsed == null || parsed.getString("url") == null) {
      throw new SubversionException("Malformed working copy metadata in " + metadata);
    }

    copy.url = URI.create(parsed.getString("url"));
    copy.uuid = parsed.getString("uuid");
    copy.revision = parsed.getLongValue("revision");
    var stored = parsed.getJSONObject("files");
    if (stored != null) {
      for (var name : stored.keySet()) {
        copy.files.put(name, stored.getLongValue(name));
      }
    }
    return copy;
  }

  public boolean exists() {
    return url != null;
  }

  public URI getUrl() {
    return url;
  }

  public long getRevision() {
    return revision;
  }

  public List<String> getFiles() {
    return List.copyOf(files.keySet());
  }

  public boolean isAtHead(SubversionRepository repository) {
    return exists() && revision == repository.getRevision();
  }

  public List<Change> checkout(SubversionRepository repository) throws SubversionException {
    files.clear();
    return sync(repository, repository.getRevision());
  }

  public List<Change> update(SubversionRepository repository) throws SubversionException {
    return update(repository, repository.getRevision());
  }

  public List<Change> update(SubversionRepository repository, long revision)
      throws SubversionException {
    if (!exists()) {
      throw new SubversionException("No working copy at " + root);
    }
    return sync(repository, revision);
  }

  private List<Change> sync(SubversionRepository repository, long target)
      throws SubversionException {
    var remote = new TreeMap<String, Long>();
    for (var entry : repository.listRecursively("", target)) {
      if (entry.isDirectory()) continue;
      remote.put(entry.path(), entry.revision());
    }

    var changes = new ArrayList<Change>();

    for (var entry : remote.entrySet()) {
      var path = entry.getKey();
      var known = files.get(path);
      var onDisk = Files.isRegularFile(resolve(path));

      if (known != null && known.equals(entry.getValue()) && onDisk) continue;

      write(path, repository.fetch(path, target));
      changes.add(
          new Change(path, known == null || !onDisk ? ChangeType.ADDED : ChangeType.UPDATED));
    }

    for (var path : files.keySet()) {
      if (remote.containsKey(path)) continue;
      delete(path);
      changes.add(new Change(path, ChangeType.DELETED));
    }

    files.clear();
    files.putAll(remote);
    url = repository.getLocation();
    uuid = repository.getUUID();
    revision = target;
    save();

    return changes;
  }

  public Path resolve(String path) throws SubversionException {
    var resolved = root.resolve(path).normalize();
    if (!resolved.startsWith(root.normalize())) {
      throw new SubversionException("Refusing to write outside the working copy: " + path);
    }
    return resolved;
  }

  private void write(String path, byte[] body) throws SubversionException {
    var file = resolve(path);
    try {
      Files.createDirectories(file.getParent());
      Files.write(file, body);
    } catch (IOException e) {
      throw new SubversionException("Could not write " + file, e);
    }
  }

  private void delete(String path) throws SubversionException {
    var file = resolve(path);
    try {
      Files.deleteIfExists(file);
    } catch (IOException e) {
      throw new SubversionException("Could not delete " + file, e);
    }
  }

  private void save() throws SubversionException {
    var payload = new JSONObject();
    payload.put("url", url.toString());
    payload.put("uuid", uuid);
    payload.put("revision", revision);
    payload.put("files", new JSONObject(files));

    try {
      Files.createDirectories(root);
      Files.writeString(root.resolve(METADATA), JSON.toJSONString(payload), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new SubversionException("Could not write working copy metadata to " + root, e);
    }
  }
}
