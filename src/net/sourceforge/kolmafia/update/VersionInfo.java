package net.sourceforge.kolmafia.update;

import java.io.File;

public record VersionInfo(int revision, File jarPath, long downloadedAt) {
  public String fileName() {
    return "KoLmafia-r" + revision + ".jar";
  }

  public long fileSize() {
    return jarPath.length();
  }
}
