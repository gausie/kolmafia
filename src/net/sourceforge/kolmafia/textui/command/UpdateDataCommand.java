package net.sourceforge.kolmafia.textui.command;

import java.util.List;
import net.sourceforge.kolmafia.KoLConstants.MafiaState;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.KoLmafiaCLI.ParameterHandling;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.StaticEntity;
import net.sourceforge.kolmafia.persistence.MallPriceDatabase;
import net.sourceforge.kolmafia.update.GitHubRelease;
import net.sourceforge.kolmafia.update.VersionInfo;
import net.sourceforge.kolmafia.update.VersionManager;

public class UpdateDataCommand extends AbstractCommand {
  public UpdateDataCommand() {
    this.usage =
        " [check | download [<revision>] | list | switch <revision> | rollback | clean | clear | save | prices <URL>] - manage KoLmafia versions and data overrides.";
    this.flags = ParameterHandling.FULL_LINE;
  }

  @Override
  public void run(final String cmd, final String parameters) {
    // Existing data override subcommands
    if (parameters.equalsIgnoreCase("clear")) {
      KoLmafia.deleteAdventureOverride();
      return;
    }

    if (parameters.equalsIgnoreCase("save")) {
      KoLmafia.saveDataOverride();
      return;
    }

    if (parameters.startsWith("prices")) {
      MallPriceDatabase.updatePrices(parameters.substring(6).trim());
      return;
    }

    // Version management subcommands
    String[] params = parameters.trim().split("\\s+");
    String subcommand = params[0];

    if (subcommand.isEmpty()) {
      subcommand = "check";
    }

    switch (subcommand) {
      case "check" -> check();
      case "download" -> download(params);
      case "list" -> list();
      case "switch" -> switchVersion(params);
      case "rollback" -> rollback();
      case "clean" -> clean();
      default -> KoLmafia.updateDisplay(MafiaState.ERROR, "update" + this.usage);
    }
  }

  private void check() {
    RequestLogger.printLine("Current version: KoLmafia r" + StaticEntity.getRevision());
    RequestLogger.printLine("Checking for updates...");

    GitHubRelease latest = GitHubRelease.fetchLatest();
    if (latest == null) {
      KoLmafia.updateDisplay(MafiaState.ERROR, "Failed to check for updates.");
      return;
    }

    int current = StaticEntity.getRevision();
    if (latest.revision() > current) {
      RequestLogger.printLine(
          "Update available: r" + latest.revision() + " (you have r" + current + ")");
      RequestLogger.printLine("Use 'update download' to download it.");
    } else {
      RequestLogger.printLine("You are running the latest version.");
    }
  }

  private void download(String[] params) {
    if (VersionManager.isJpackageInstall()) {
      RequestLogger.printLine(
          "You are using a platform installer. Please update via your package manager.");
      return;
    }

    GitHubRelease target;

    if (params.length >= 2) {
      String revStr = params[1].startsWith("r") ? params[1].substring(1) : params[1];
      int revision;
      try {
        revision = Integer.parseInt(revStr);
      } catch (NumberFormatException e) {
        KoLmafia.updateDisplay(MafiaState.ERROR, "Invalid revision number: " + params[1]);
        return;
      }

      target = GitHubRelease.fetchByTag("r" + revision);
      if (target == null) {
        KoLmafia.updateDisplay(MafiaState.ERROR, "Release r" + revision + " not found on GitHub.");
        return;
      }
    } else {
      target = GitHubRelease.fetchLatest();
      if (target == null) {
        KoLmafia.updateDisplay(MafiaState.ERROR, "Failed to fetch latest release.");
        return;
      }
    }

    if (VersionManager.hasVersion(target.revision())) {
      RequestLogger.printLine("Version r" + target.revision() + " is already downloaded.");
      RequestLogger.printLine("Use 'update switch " + target.revision() + "' to switch to it.");
      return;
    }

    if (VersionManager.downloadVersion(target)) {
      RequestLogger.printLine("Use 'update switch " + target.revision() + "' to switch to it.");
    }
  }

  private void list() {
    List<VersionInfo> versions = VersionManager.getStoredVersions();
    int currentRevision = StaticEntity.getRevision();

    RequestLogger.printLine("Current running version: r" + currentRevision);
    RequestLogger.printLine("");

    if (versions.isEmpty()) {
      RequestLogger.printLine("No versions stored in versions directory.");
      return;
    }

    RequestLogger.printLine("Stored versions:");
    for (VersionInfo version : versions) {
      String marker = version.revision() == currentRevision ? " (current)" : "";
      long sizeMB = version.fileSize() / 1024 / 1024;
      RequestLogger.printLine("  r" + version.revision() + " - " + sizeMB + " MB" + marker);
    }
  }

  private void switchVersion(String[] params) {
    if (params.length < 2) {
      KoLmafia.updateDisplay(MafiaState.ERROR, "Usage: update switch <revision>");
      return;
    }

    if (VersionManager.isJpackageInstall()) {
      RequestLogger.printLine("Version switching is not available for platform installer builds.");
      return;
    }

    String revStr = params[1].startsWith("r") ? params[1].substring(1) : params[1];
    int revision;
    try {
      revision = Integer.parseInt(revStr);
    } catch (NumberFormatException e) {
      KoLmafia.updateDisplay(MafiaState.ERROR, "Invalid revision number: " + params[1]);
      return;
    }

    if (revision == StaticEntity.getRevision()) {
      RequestLogger.printLine("Already running r" + revision + ".");
      return;
    }

    VersionManager.restartInto(revision);
  }

  private void rollback() {
    if (VersionManager.isJpackageInstall()) {
      RequestLogger.printLine("Rollback is not available for platform installer builds.");
      return;
    }

    int currentRevision = StaticEntity.getRevision();
    List<VersionInfo> versions = VersionManager.getStoredVersions();

    // Find the highest revision that is less than current
    VersionInfo previous = null;
    for (VersionInfo version : versions) {
      if (version.revision() < currentRevision) {
        previous = version;
        break;
      }
    }

    if (previous == null) {
      KoLmafia.updateDisplay(MafiaState.ERROR, "No previous version available to roll back to.");
      return;
    }

    RequestLogger.printLine(
        "Rolling back from r" + currentRevision + " to r" + previous.revision() + "...");
    VersionManager.restartInto(previous.revision());
  }

  private void clean() {
    VersionManager.pruneVersions();
    RequestLogger.printLine("Version cleanup complete.");
  }
}
