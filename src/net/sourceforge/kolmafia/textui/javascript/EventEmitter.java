package net.sourceforge.kolmafia.textui.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class EventEmitter {
  Map<String, List<Function>> listeners = new HashMap<>();

  EventEmitter() {}

  public void addEventListener(final String event, final Function listener) {
    listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
  }

  public void removeEventListener(final String event, final Function listener) {
    listeners.getOrDefault(event, new ArrayList<>()).remove(listener);
  }

  public void dispatchEvent(final Scriptable scope, final String event) {
    var eventListeners = listeners.getOrDefault(event, new ArrayList<>());
    for (var listener : eventListeners) {
      Context cx = Context.getCurrentContext();
      Object[] args = new Object[] {event};
      listener.call(cx, scope, scope, args);
    }
  }
}
