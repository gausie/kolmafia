package net.sourceforge.kolmafia.swingui.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.sourceforge.kolmafia.StaticEntity;
import net.sourceforge.kolmafia.update.GitHubRelease;
import net.sourceforge.kolmafia.update.VersionInfo;
import net.sourceforge.kolmafia.update.VersionManager;

public class UpdatePanel extends JPanel {
  private final JLabel currentVersionLabel;
  private final JLabel latestVersionLabel;
  private final JButton checkButton;
  private final JButton downloadButton;
  private final JButton switchButton;
  private final JButton deleteButton;
  private final JTable versionTable;
  private final VersionTableModel tableModel;
  private final JTextArea detailArea;

  private GitHubRelease latestRelease;

  public UpdatePanel() {
    this.setLayout(new BorderLayout(10, 10));
    this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Top: version info
    JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
    this.currentVersionLabel = new JLabel("Current version: r" + StaticEntity.getRevision());
    this.latestVersionLabel = new JLabel("Latest version: checking...");
    this.checkButton = new JButton("Check Now");

    JPanel labelPanel = new JPanel(new BorderLayout());
    labelPanel.add(this.currentVersionLabel, BorderLayout.NORTH);
    labelPanel.add(this.latestVersionLabel, BorderLayout.SOUTH);
    infoPanel.add(labelPanel, BorderLayout.CENTER);
    infoPanel.add(this.checkButton, BorderLayout.EAST);
    this.add(infoPanel, BorderLayout.NORTH);

    // Center: split pane with table on left, detail on right
    this.tableModel = new VersionTableModel();
    this.versionTable = new JTable(this.tableModel);
    this.versionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.versionTable.setPreferredScrollableViewportSize(null);
    this.versionTable.setDefaultRenderer(Object.class, new BoldCurrentRenderer());
    this.versionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
    this.versionTable.getColumnModel().getColumn(1).setPreferredWidth(80);

    this.detailArea = new JTextArea();
    this.detailArea.setEditable(false);
    this.detailArea.setLineWrap(true);
    this.detailArea.setWrapStyleWord(true);

    JSplitPane splitPane =
        new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(this.versionTable),
            new JScrollPane(this.detailArea));
    splitPane.setResizeWeight(0.5);
    this.add(splitPane, BorderLayout.CENTER);

    // Defer divider location until after layout
    SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.5));

    // Selection listener to show detail and update button state
    this.versionTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) {
                onSelectionChanged();
              }
            });

    // Bottom: action buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    this.downloadButton = new JButton("Download Latest");
    this.switchButton = new JButton("Switch To Selected");
    this.deleteButton = new JButton("Delete Selected");
    buttonPanel.add(this.downloadButton);
    buttonPanel.add(this.switchButton);
    buttonPanel.add(this.deleteButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    // Wire up buttons
    this.checkButton.addActionListener(e -> checkForUpdates());
    this.downloadButton.addActionListener(e -> downloadLatest());
    this.switchButton.addActionListener(e -> switchToSelected());
    this.deleteButton.addActionListener(e -> deleteSelected());

    // Disable switch/delete for jpackage installs
    if (VersionManager.isJpackageInstall()) {
      this.downloadButton.setEnabled(false);
      this.switchButton.setEnabled(false);
      this.deleteButton.setEnabled(false);
      this.downloadButton.setToolTipText("Not available for platform installer builds");
      this.switchButton.setToolTipText("Not available for platform installer builds");
      this.deleteButton.setToolTipText("Not available for platform installer builds");
    }

    // Initial load
    refreshVersionTable();
    checkForUpdates();
  }

  private void onSelectionChanged() {
    int row = this.versionTable.getSelectedRow();
    if (row < 0) {
      this.detailArea.setText("");
      this.deleteButton.setEnabled(false);
      this.switchButton.setEnabled(false);
      return;
    }

    int revision = this.tableModel.getRevisionAt(row);
    boolean isVirtual = this.tableModel.isVirtualEntry(row);
    boolean isCurrent = this.tableModel.isCurrentVersion(row);

    // Update detail area
    if (isVirtual) {
      this.detailArea.setText(
          "r"
              + revision
              + " is the version you launched directly.\n\n"
              + "It is not managed by the update system, so release notes "
              + "are not available and it cannot be deleted from this interface.");
    } else {
      String body = VersionManager.loadMetadataBody(revision);
      if (body != null) {
        this.detailArea.setText(body);
      } else {
        this.detailArea.setText(
            "No release notes available for r"
                + revision
                + ".\n\nThis version was downloaded before release notes were tracked.");
      }
    }
    this.detailArea.setCaretPosition(0);

    // Update button state
    if (!VersionManager.isJpackageInstall()) {
      this.deleteButton.setEnabled(!isCurrent && !isVirtual);
      this.switchButton.setEnabled(!isCurrent);
    }
  }

  private void checkForUpdates() {
    this.checkButton.setEnabled(false);
    this.latestVersionLabel.setText("Latest version: checking...");

    new Thread(
            () -> {
              GitHubRelease latest = GitHubRelease.fetchLatest();
              SwingUtilities.invokeLater(
                  () -> {
                    this.latestRelease = latest;
                    this.checkButton.setEnabled(true);
                    if (latest == null) {
                      this.latestVersionLabel.setText("Latest version: (check failed)");
                    } else {
                      boolean updateAvailable = latest.revision() > StaticEntity.getRevision();
                      this.latestVersionLabel.setText("Latest version: r" + latest.revision());
                      this.downloadButton.setEnabled(
                          !VersionManager.isJpackageInstall()
                              && updateAvailable
                              && !VersionManager.hasVersion(latest.revision()));
                      updateTabTitle(updateAvailable);
                    }
                  });
            },
            "UpdateCheck")
        .start();
  }

  private void downloadLatest() {
    if (this.latestRelease == null) {
      return;
    }

    this.downloadButton.setEnabled(false);
    this.downloadButton.setText("Downloading...");
    GitHubRelease release = this.latestRelease;

    new Thread(
            () -> {
              boolean success = VersionManager.downloadVersion(release);
              SwingUtilities.invokeLater(
                  () -> {
                    this.downloadButton.setText("Download Latest");
                    this.downloadButton.setEnabled(!success);
                    refreshVersionTable();
                  });
            },
            "VersionDownload")
        .start();
  }

  private void updateTabTitle(boolean updateAvailable) {
    var parent = this.getParent();
    if (parent instanceof JTabbedPane tabbedPane) {
      int index = tabbedPane.indexOfComponent(this);
      if (index >= 0) {
        tabbedPane.setTitleAt(index, updateAvailable ? "Updates *" : "Updates");
      }
    }
  }

  private void switchToSelected() {
    int row = this.versionTable.getSelectedRow();
    if (row < 0) {
      return;
    }

    int revision = this.tableModel.getRevisionAt(row);
    if (revision == StaticEntity.getRevision()) {
      return;
    }

    VersionManager.restartInto(revision);
  }

  private void deleteSelected() {
    int row = this.versionTable.getSelectedRow();
    if (row < 0) {
      return;
    }

    int revision = this.tableModel.getRevisionAt(row);
    int confirm =
        JOptionPane.showConfirmDialog(
            this,
            "Delete r" + revision + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }
    VersionManager.deleteVersion(revision);
    refreshVersionTable();
  }

  private void refreshVersionTable() {
    this.tableModel.refresh();
  }

  private static class VersionTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Revision", "Size (MB)"};
    private List<VersionInfo> versions = List.of();
    private int virtualIndex = -1; // index of the "virtual" current-jar entry, or -1

    public void refresh() {
      List<VersionInfo> stored = new ArrayList<>(VersionManager.getStoredVersions());
      this.virtualIndex = -1;

      // Include the currently running JAR if it's not already in the versions folder
      int currentRevision = StaticEntity.getRevision();
      if (currentRevision > 0 && stored.stream().noneMatch(v -> v.revision() == currentRevision)) {
        File currentJar = VersionManager.getCurrentJarPath();
        if (currentJar != null) {
          stored.add(new VersionInfo(currentRevision, currentJar, currentJar.lastModified()));
          stored.sort((a, b) -> Integer.compare(b.revision(), a.revision()));
          // Find where it ended up
          for (int i = 0; i < stored.size(); i++) {
            if (stored.get(i).revision() == currentRevision) {
              this.virtualIndex = i;
              break;
            }
          }
        }
      }

      this.versions = stored;
      fireTableDataChanged();
    }

    public int getRevisionAt(int row) {
      return this.versions.get(row).revision();
    }

    public boolean isCurrentVersion(int row) {
      return this.versions.get(row).revision() == StaticEntity.getRevision();
    }

    public boolean isVirtualEntry(int row) {
      return row == this.virtualIndex;
    }

    @Override
    public int getRowCount() {
      return this.versions.size();
    }

    @Override
    public int getColumnCount() {
      return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
      return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      VersionInfo info = this.versions.get(rowIndex);
      return switch (columnIndex) {
        case 0 -> "r" + info.revision() + (rowIndex == virtualIndex ? " *" : "");
        case 1 -> info.fileSize() / 1024 / 1024;
        default -> "";
      };
    }
  }

  private static class BoldCurrentRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      Component c =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      VersionTableModel model = (VersionTableModel) table.getModel();
      if (model.isCurrentVersion(row)) {
        c.setFont(c.getFont().deriveFont(Font.BOLD));
      } else {
        c.setFont(c.getFont().deriveFont(Font.PLAIN));
      }
      return c;
    }
  }
}
