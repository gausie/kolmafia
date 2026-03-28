package net.sourceforge.kolmafia.swingui;

import net.sourceforge.kolmafia.swingui.panel.UpdatePanel;

public class UpdateFrame extends GenericFrame {
  public UpdateFrame() {
    super("Update Manager");
    this.setCenterComponent(new UpdatePanel());
  }
}
