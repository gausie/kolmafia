package net.sourceforge.kolmafia.scripts.svn.dav;

import java.io.IOException;

public class SubversionException extends IOException {
  public SubversionException(String message) {
    super(message);
  }

  public SubversionException(String message, Throwable cause) {
    super(message, cause);
  }
}
