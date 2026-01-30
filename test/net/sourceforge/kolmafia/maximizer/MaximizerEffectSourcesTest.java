package net.sourceforge.kolmafia.maximizer;

import static internal.helpers.Maximizer.getBoosts;
import static internal.helpers.Maximizer.maximize;
import static internal.helpers.Player.withAsdonMartinFuel;
import static internal.helpers.Player.withCampgroundItem;
import static internal.helpers.Player.withClass;
import static internal.helpers.Player.withEffect;
import static internal.helpers.Player.withEquippableItem;
import static internal.helpers.Player.withEquipped;
import static internal.helpers.Player.withFamiliar;
import static internal.helpers.Player.withFamiliarInTerrarium;
import static internal.helpers.Player.withInteractivity;
import static internal.helpers.Player.withItem;
import static internal.helpers.Player.withMeat;
import static internal.helpers.Player.withProperty;
import static internal.helpers.Player.withRestricted;
import static internal.helpers.Player.withSign;
import static internal.helpers.Player.withSkill;
import static internal.helpers.Player.withStats;
import static internal.helpers.Player.withWorkshedItem;
import static internal.matchers.Maximizer.recommendsSlot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

import internal.helpers.Cleanups;
import net.sourceforge.kolmafia.AscensionClass;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.ZodiacSign;
import net.sourceforge.kolmafia.equipment.Slot;
import net.sourceforge.kolmafia.objectpool.FamiliarPool;
import net.sourceforge.kolmafia.objectpool.ItemPool;
import net.sourceforge.kolmafia.preferences.Preferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for the various effect sources in Maximizer.java. */
public class MaximizerEffectSourcesTest {
  @BeforeAll
  public static void beforeAll() {
    KoLCharacter.reset("MaximizerEffectSourcesTest");
    Preferences.reset("MaximizerEffectSourcesTest");
  }

  @BeforeEach
  public void beforeEach() {
    KoLCharacter.reset("MaximizerEffectSourcesTest");
    Preferences.reset("MaximizerEffectSourcesTest");
  }

  @Nested
  class Horsery {
    @Test
    public void suggestsDarkHorseForNegativeCombat() {
      var cleanups =
          new Cleanups(
              withProperty("horseryAvailable", true),
              withProperty("_horsery", ""),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("-combat"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("horsery dark horse"))));
      }
    }

    @Test
    public void doesNotSuggestDarkHorseWhenNoMeatToSwitch() {
      var cleanups =
          new Cleanups(
              withProperty("horseryAvailable", true),
              withProperty("_horsery", "normal horse"),
              withMeat(0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("-combat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("horsery dark horse")))));
      }
    }

    @Test
    public void doesNotSuggestHorseryWhenUnavailable() {
      var cleanups =
          new Cleanups(withProperty("horseryAvailable", false), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("-combat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("horsery")))));
      }
    }
  }

  @Nested
  class BoomBox {
    @Test
    public void suggestsBoomBoxWhenAvailableWithUses() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.BOOMBOX),
              withProperty("_boomBoxSongsLeft", 11),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(
            getBoosts(),
            hasItem(hasProperty("cmd", equalTo("boombox total eclipse of your meat"))));
      }
    }

    @Test
    public void doesNotSuggestBoomBoxWhenNoUsesLeft() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.BOOMBOX),
              withProperty("_boomBoxSongsLeft", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("boombox")))));
      }
    }

    @Test
    public void doesNotSuggestBoomBoxWhenNotOwned() {
      var cleanups = new Cleanups(withProperty("_boomBoxSongsLeft", 11), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("boombox")))));
      }
    }
  }

  @Nested
  class MindControlDevice {
    @Test
    public void suggestsMCDWhenAvailable() {
      var cleanups = new Cleanups(withSign(ZodiacSign.MONGOOSE), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("ml"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("mcd 10"))));
      }
    }
  }

  @Nested
  class VIPLounge {
    @Test
    public void suggestsPoolWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_poolGames", 0),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("weapon damage percent"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("pool 1"))));
      }
    }

    @Test
    public void doesNotSuggestPoolWhenUsedUp() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_poolGames", 3),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("weapon damage percent"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("pool")))));
      }
    }

    @Test
    public void suggestsShowerWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_aprilShower", false),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        // Shower effects give Experience Percent bonuses (mys exp percent, not mys exp)
        assertTrue(maximize("mys exp percent"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("shower lukewarm"))));
      }
    }

    @Test
    public void doesNotSuggestShowerWhenUsed() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_aprilShower", true),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mys exp percent"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("shower")))));
      }
    }

    @Test
    public void suggestsSwimWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_olympicSwimmingPool", false),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("init"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("swim laps"))));
      }
    }

    @Test
    public void suggestsFortuneBuffWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_clanFortuneBuffUsed", false),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("fortune buff gunther"))));
      }
    }

    @Test
    public void suggestsPhotoBoothWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_photoBoothEffects", 0),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        // Wild and Westy provides +50 initiative
        assertTrue(maximize("init"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("photobooth effect wild"))));
      }
    }

    @Test
    public void doesNotSuggestPhotoBoothWhenUsedUp() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.VIP_LOUNGE_KEY),
              withProperty("_photoBoothEffects", 3),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("init"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("photobooth")))));
      }
    }

    @Test
    public void doesNotSuggestPoolWithoutVIPKey() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("_poolGames", 0),
              withInteractivity(true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("weapon damage percent"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("pool")))));
      }
    }
  }

  @Nested
  class SourceTerminal {
    @Test
    public void suggestsTerminalEnhanceWhenAvailable() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.SOURCE_TERMINAL),
              withProperty("sourceTerminalChips", ""),
              withProperty(
                  "sourceTerminalEnhanceKnown",
                  "items.enh,meat.enh,init.enh,critical.enh,damage.enh,substats.enh"),
              withProperty("_sourceTerminalEnhanceUses", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("terminal enhance items.enh"))));
      }
    }

    @Test
    public void suggestsTerminalWithCRAMChipAllowsExtraUse() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.SOURCE_TERMINAL),
              withProperty("sourceTerminalChips", "CRAM"),
              withProperty(
                  "sourceTerminalEnhanceKnown",
                  "items.enh,meat.enh,init.enh,critical.enh,damage.enh,substats.enh"),
              withProperty("_sourceTerminalEnhanceUses", 1),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("terminal enhance items.enh"))));
      }
    }

    @Test
    public void suggestsTerminalWithSCRAMChipAllowsThirdUse() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.SOURCE_TERMINAL),
              withProperty("sourceTerminalChips", "CRAM,SCRAM"),
              withProperty("sourceTerminalEnhanceKnown", "items.enh"),
              withProperty("_sourceTerminalEnhanceUses", 2),
              withProperty("sourceTerminalPram", 3),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("terminal enhance items.enh"))));
      }
    }

    @Test
    public void doesNotSuggestTerminalWhenUsedUp() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.SOURCE_TERMINAL),
              withProperty("sourceTerminalChips", ""),
              withProperty("sourceTerminalEnhanceKnown", "items.enh"),
              withProperty("_sourceTerminalEnhanceUses", 1),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(
            getBoosts(), not(hasItem(hasProperty("cmd", equalTo("terminal enhance items.enh")))));
      }
    }

    @Test
    public void doesNotSuggestTerminalWhenNotInstalled() {
      var cleanups =
          new Cleanups(withProperty("_sourceTerminalEnhanceUses", 0), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(
            getBoosts(), not(hasItem(hasProperty("cmd", equalTo("terminal enhance items.enh")))));
      }
    }
  }

  @Nested
  class AsdonMartin {
    @Test
    public void suggestsAsdonDriveWhenHasFuel() {
      var cleanups =
          new Cleanups(
              withWorkshedItem(ItemPool.ASDON_MARTIN),
              withAsdonMartinFuel(100),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(
            getBoosts(), hasItem(hasProperty("cmd", equalTo("asdonmartin drive observantly"))));
      }
    }

    @Test
    public void doesNotSuggestAsdonDriveWhenLowFuel() {
      var cleanups =
          new Cleanups(
              withWorkshedItem(ItemPool.ASDON_MARTIN),
              withAsdonMartinFuel(10),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("asdonmartin drive")))));
      }
    }
  }

  @Nested
  class MayoClinic {
    @Test
    public void suggestsMayoSoakWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withWorkshedItem(ItemPool.MAYO_CLINIC),
              withProperty("_mayoTankSoaked", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Force of Mayo Be With You provides resistances
        assertTrue(maximize("hot res"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("mayosoak"))));
      }
    }

    @Test
    public void doesNotSuggestMayoSoakWhenUsed() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withWorkshedItem(ItemPool.MAYO_CLINIC),
              withProperty("_mayoTankSoaked", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("hot res"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("mayosoak")))));
      }
    }
  }

  @Nested
  class Witchess {
    @Test
    public void suggestsWitchessWhenAvailableWithPuzzleBonus() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.WITCHESS_SET),
              withProperty("_witchessBuff", false),
              withProperty("puzzleChampBonus", 20),
              withStats(100, 100, 100));
      try (cleanups) {
        // Puzzle Champ provides Familiar Weight
        assertTrue(maximize("familiar weight"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("witchess"))));
      }
    }

    @Test
    public void doesNotSuggestWitchessWhenUsed() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.WITCHESS_SET),
              withProperty("_witchessBuff", true),
              withProperty("puzzleChampBonus", 20),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("familiar weight"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("witchess")))));
      }
    }

    @Test
    public void doesNotSuggestWitchessWhenNoPuzzleBonus() {
      var cleanups =
          new Cleanups(
              withCampgroundItem(ItemPool.WITCHESS_SET),
              withProperty("_witchessBuff", false),
              withProperty("puzzleChampBonus", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("familiar weight"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("witchess")))));
      }
    }
  }

  @Nested
  class BeachComb {
    @Test
    public void suggestsBeachHeadWhenAvailable() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.BEACH_COMB),
              withProperty("_beachHeadsUsed", ""),
              withStats(100, 100, 100));
      try (cleanups) {
        // Use "cold res" to target beach head effect rather than comb as equip
        assertTrue(maximize("cold res"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("beach head Cold as Nice"))));
      }
    }

    @Test
    public void suggestsBeachHeadWithDriftwoodComb() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.DRIFTWOOD_BEACH_COMB),
              withProperty("_beachHeadsUsed", ""),
              withStats(100, 100, 100));
      try (cleanups) {
        // Use "cold res" to target beach head effect rather than comb as equip
        assertTrue(maximize("cold res"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("beach head Cold as Nice"))));
      }
    }

    @Test
    public void doesNotSuggestBeachHeadWhenAllUsed() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.BEACH_COMB),
              withProperty("_beachHeadsUsed", "1,2,3,4,5,6,7,8,9,10,11"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("cold res"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("beach head")))));
      }
    }
  }

  @Nested
  class GrimBrother {
    @Test
    public void suggestsGrimWhenAvailable() {
      var cleanups =
          new Cleanups(
              withFamiliarInTerrarium(FamiliarPool.GRIM_BROTHER),
              withProperty("_grimBuff", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Grim effects: init, hpmp, damage - use init
        assertTrue(maximize("init"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("grim init"))));
      }
    }

    @Test
    public void doesNotSuggestGrimWhenUsed() {
      var cleanups =
          new Cleanups(
              withFamiliarInTerrarium(FamiliarPool.GRIM_BROTHER),
              withProperty("_grimBuff", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("init"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("grim")))));
      }
    }
  }

  @Nested
  class DeckOfEveryCard {
    @Test
    public void suggestsDeckPlayWhenAvailable() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.DECK_OF_EVERY_CARD),
              withProperty("_deckCardsDrawn", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("play XI - Strength"))));
      }
    }

    @Test
    public void doesNotSuggestDeckPlayWhenUsedUp() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.DECK_OF_EVERY_CARD),
              withProperty("_deckCardsDrawn", 15),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("play")))));
      }
    }
  }

  @Nested
  class ProtonAccelerator {
    @Test
    public void suggestsCrossStreamsWhenAvailable() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.PROTON_ACCELERATOR),
              withProperty("_streamsCrossed", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Total Protonic Reversal gives stat percents, not resistances
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("crossstreams"))));
      }
    }

    @Test
    public void doesNotSuggestCrossStreamsWhenUsed() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.PROTON_ACCELERATOR),
              withProperty("_streamsCrossed", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("crossstreams")))));
      }
    }
  }

  @Nested
  class Monorail {
    @Test
    public void suggestsMonorailBuffWhenAvailable() {
      var cleanups = new Cleanups(withProperty("_lyleFavored", false), withStats(100, 100, 100));
      try (cleanups) {
        // Favored by Lyle provides Muscle/Mysticality/Moxie Percent, not init
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("monorail buff"))));
      }
    }

    @Test
    public void doesNotSuggestMonorailBuffWhenUsed() {
      var cleanups = new Cleanups(withProperty("_lyleFavored", true), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("monorail buff")))));
      }
    }
  }

  @Nested
  class BoxingDaycare {
    @Test
    public void suggestsDaycareWhenOpen() {
      var cleanups =
          new Cleanups(
              withProperty("daycareOpen", true),
              withProperty("_daycareSpa", false),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("daycare muscle"))));
      }
    }

    @Test
    public void suggestsDaycareWhenOpenToday() {
      var cleanups =
          new Cleanups(
              withProperty("daycareOpen", false),
              withProperty("_daycareToday", true),
              withProperty("_daycareSpa", false),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("daycare muscle"))));
      }
    }

    @Test
    public void doesNotSuggestDaycareWhenSpaUsed() {
      var cleanups =
          new Cleanups(
              withProperty("daycareOpen", true),
              withProperty("_daycareSpa", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("daycare muscle")))));
      }
    }
  }

  @Nested
  class GreatestAmericanPants {
    @Test
    public void suggestsGAPWhenEquippedWithUses() {
      var cleanups =
          new Cleanups(
              withEquipped(Slot.PANTS, "Greatest American Pants"),
              withProperty("_gapBuffs", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        // Super Speed gives Moxie Percent +100
        assertTrue(maximize("mox"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("gap speed"))));
      }
    }

    @Test
    public void doesNotSuggestGAPWhenUsedUp() {
      var cleanups =
          new Cleanups(
              withEquipped(Slot.PANTS, "Greatest American Pants"),
              withProperty("_gapBuffs", 5),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mox"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("gap speed")))));
      }
    }

    @Test
    public void doesNotSuggestGAPCommandWhenNotEquipped() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.GREAT_PANTS),
              withProperty("_gapBuffs", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mox"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("gap speed")))));
      }
    }
  }

  @Nested
  class CampAway {
    @Test
    public void suggestsCampAwayCloudWhenAvailable() {
      var cleanups =
          new Cleanups(
              withProperty("_campAwayCloudBuffs", 0),
              withProperty("getawayCampsiteUnlocked", true),
              withStats(100, 100, 100));
      try (cleanups) {
        // Cloud-Talk gives Experience Percent, not Adventures
        assertTrue(maximize("exp"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("campaway cloud"))));
      }
    }

    @Test
    public void doesNotSuggestCampAwayCloudWhenUsed() {
      var cleanups =
          new Cleanups(
              withProperty("_campAwayCloudBuffs", 1),
              withProperty("getawayCampsiteUnlocked", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("exp"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("campaway cloud")))));
      }
    }
  }

  @Nested
  class MonkeyPaw {
    @Test
    public void suggestsMonkeyPawWhenAvailable() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.CURSED_MONKEY_PAW),
              withProperty("_monkeyPawWishesUsed", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", startsWith("monkeypaw effect"))));
      }
    }

    @Test
    public void doesNotSuggestMonkeyPawWhenUsedUp() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.CURSED_MONKEY_PAW),
              withProperty("_monkeyPawWishesUsed", 5),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("monkeypaw effect")))));
      }
    }
  }

  @Nested
  class GenieBottle {
    @Test
    public void suggestsGenieWhenBottleAvailable() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.GENIE_BOTTLE),
              withProperty("_genieWishesUsed", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", startsWith("genie effect"))));
      }
    }

    @Test
    public void doesNotSuggestGenieWhenBottleUsedUp() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.GENIE_BOTTLE),
              withProperty("_genieWishesUsed", 3),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("genie effect")))));
      }
    }

    @Test
    public void suggestsGenieWithPocketWish() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.POCKET_WISH),
              withProperty("_genieWishesUsed", 3),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", startsWith("genie effect"))));
      }
    }
  }

  @Nested
  class Telescope {
    @Test
    public void suggestsTelescopeWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("telescopeUpgrades", 5),
              withProperty("telescopeLookedHigh", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Also set the field since modifier expression [+5*U] reads from the field
        KoLCharacter.setTelescopeUpgrades(5);
        // Starry-Eyed provides Muscle/Mys/Moxie Percent, not flat stats
        assertTrue(maximize("mus percent"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("telescope look high"))));
      }
    }

    @Test
    public void doesNotSuggestTelescopeWhenUsed() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("telescopeUpgrades", 5),
              withProperty("telescopeLookedHigh", true),
              withStats(100, 100, 100));
      try (cleanups) {
        KoLCharacter.setTelescopeUpgrades(5);
        assertTrue(maximize("mus percent"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("telescope look high")))));
      }
    }

    @Test
    public void doesNotSuggestTelescopeWhenNoUpgrades() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("telescopeUpgrades", 0),
              withProperty("telescopeLookedHigh", false),
              withStats(100, 100, 100));
      try (cleanups) {
        KoLCharacter.setTelescopeUpgrades(0);
        assertTrue(maximize("mus percent"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("telescope look high")))));
      }
    }
  }

  @Nested
  class Ballpit {
    @Test
    public void suggestsBallpitWhenAvailableAndCanInteract() {
      var cleanups =
          new Cleanups(
              withProperty("_ballpit", false), withInteractivity(true), withStats(100, 100, 100));
      try (cleanups) {
        // Having a Ball! provides stat percent bonuses
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("ballpit"))));
      }
    }

    @Test
    public void doesNotSuggestBallpitWhenUsed() {
      var cleanups =
          new Cleanups(
              withProperty("_ballpit", true), withInteractivity(true), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("ballpit")))));
      }
    }
  }

  @Nested
  class Jukebox {
    @Test
    public void suggestsJukeboxMeatWhenAvailableAndCanInteract() {
      var cleanups =
          new Cleanups(
              withProperty("_jukebox", false), withInteractivity(true), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("jukebox meat"))));
      }
    }

    @Test
    public void doesNotSuggestJukeboxMeatWhenUsed() {
      var cleanups =
          new Cleanups(
              withProperty("_jukebox", true), withInteractivity(true), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("jukebox meat")))));
      }
    }
  }

  @Nested
  class Friars {
    @Test
    public void suggestsFriarsFoodWhenUnlockedAndAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("lastFriarCeremonyAscension", 1),
              withProperty("knownAscensions", 1),
              withProperty("friarsBlessingReceived", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Brother Flying Burrito's Blessing provides Food Drop +30
        assertTrue(maximize("food drop"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("friars food"))));
      }
    }

    @Test
    public void doesNotSuggestFriarsFoodWhenUsed() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("lastFriarCeremonyAscension", 1),
              withProperty("knownAscensions", 1),
              withProperty("friarsBlessingReceived", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("food drop"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("friars food")))));
      }
    }

    @Test
    public void doesNotSuggestFriarsFoodWhenNotUnlocked() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withProperty("lastFriarCeremonyAscension", 0),
              withProperty("knownAscensions", 1),
              withProperty("friarsBlessingReceived", false),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("food drop"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("friars food")))));
      }
    }
  }

  @Nested
  class BarrelShrine {
    @Test
    public void suggestsBarrelPrayerBuffWhenAvailable() {
      var cleanups =
          new Cleanups(
              withClass(AscensionClass.SEAL_CLUBBER),
              withRestricted(false),
              withProperty("barrelShrineUnlocked", true),
              withProperty("_barrelPrayer", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Barrel Chested provides Weapon Damage Percent +150 (only for Seal Clubbers)
        assertTrue(maximize("weapon damage percent"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("barrelprayer buff"))));
      }
    }

    @Test
    public void doesNotSuggestBarrelPrayerBuffWhenUsed() {
      var cleanups =
          new Cleanups(
              withClass(AscensionClass.SEAL_CLUBBER),
              withRestricted(false),
              withProperty("barrelShrineUnlocked", true),
              withProperty("_barrelPrayer", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("weapon damage percent"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", equalTo("barrelprayer buff")))));
      }
    }
  }

  @Nested
  class PillKeeper {
    @Test
    public void suggestsPillKeeperWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.PILL_KEEPER),
              withProperty("_freePillKeeperUsed", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Hulkien provides stat percents, Rainbowolin provides resistances
        assertTrue(maximize("mus percent"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("pillkeeper stat"))));
      }
    }

    @Test
    public void suggestsPillKeeperAfterFreeUseIfSpleenAvailable() {
      var cleanups =
          new Cleanups(
              withClass(AscensionClass.TURTLE_TAMER),
              withRestricted(false),
              withItem(ItemPool.PILL_KEEPER),
              withProperty("_freePillKeeperUsed", true),
              withStats(100, 100, 100));
      try (cleanups) {
        // Hulkien provides stat percents. After free use, pillkeeper costs 3 spleen,
        // so we need a class that has spleen capacity.
        assertTrue(maximize("mus percent"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("pillkeeper stat"))));
      }
    }
  }

  @Nested
  class CargoShorts {
    @Test
    public void suggestsCargoEffectWhenAvailable() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.CARGO_CULTIST_SHORTS),
              withProperty("_cargoPocketEmptied", false),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", startsWith("cargo effect"))));
      }
    }

    @Test
    public void doesNotSuggestCargoEffectWhenUsed() {
      var cleanups =
          new Cleanups(
              withItem(ItemPool.CARGO_CULTIST_SHORTS),
              withProperty("_cargoPocketEmptied", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("cargo effect")))));
      }
    }
  }

  @Nested
  class AlliedRadio {
    @Test
    public void suggestsAlliedRadioWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.HANDHELD_ALLIED_RADIO),
              withProperty("_alliedRadioDropsUsed", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        // Materiel Intel provides Item Drop +100
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("alliedradio effect intel"))));
      }
    }

    @Test
    public void suggestsAlliedRadioWithBackpack() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withEquipped(Slot.CONTAINER, ItemPool.ALLIED_RADIO_BACKPACK),
              withProperty("_alliedRadioDropsUsed", 0),
              withStats(100, 100, 100));
      try (cleanups) {
        // Materiel Intel provides Item Drop +100
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("alliedradio effect intel"))));
      }
    }
  }

  @Nested
  class SweetSynthesis {
    @Test
    public void suggestsSynthesizeWhenSkillKnown() {
      var cleanups =
          new Cleanups(
              withClass(AscensionClass.TURTLE_TAMER),
              withSkill("Sweet Synthesis"),
              // Two complex candies (candy2) for tier 3 synthesis (Synthesis: Learning)
              withItem(ItemPool.FANCY_CHOCOLATE, 2),
              withStats(100, 100, 100));
      try (cleanups) {
        // Synthesis: Learning provides +50% mys experience
        assertTrue(maximize("mys exp percent"));
        assertThat(
            getBoosts(), hasItem(hasProperty("cmd", equalTo("synthesize Synthesis: Learning"))));
      }
    }
  }

  @Nested
  class AprilingBand {
    @Test
    public void suggestsAprilBandWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false),
              withItem(ItemPool.APRILING_BAND_HELMET),
              withStats(100, 100, 100));
      try (cleanups) {
        // Apriling Band Celebration Bop provides Food Drop +50
        assertTrue(maximize("food drop"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("aprilband effect drop"))));
      }
    }
  }

  @Nested
  class MayamCalendar {
    @Test
    public void suggestsMayamWhenAvailable() {
      var cleanups =
          new Cleanups(
              withRestricted(false), withItem(ItemPool.MAYAM_CALENDAR), withStats(100, 100, 100));
      try (cleanups) {
        // Memories of Cheesier Age provides Food Drop +100
        assertTrue(maximize("food drop"));
        assertThat(
            getBoosts(),
            hasItem(hasProperty("cmd", equalTo("mayam resonance memories of cheesier age"))));
      }
    }
  }

  @Nested
  class ToggleEffects {
    @Test
    public void suggestsToggleWhenIntenselyInterested() {
      var cleanups =
          new Cleanups(withEffect("Become Intensely interested"), withStats(100, 100, 100));
      try (cleanups) {
        // Intensely interested gives +5 combat, toggle to get -5 combat
        assertTrue(maximize("-combat"));
        assertThat(
            getBoosts(),
            hasItem(hasProperty("cmd", equalTo("toggle Become Intensely interested"))));
      }
    }

    @Test
    public void suggestsToggleWhenSuperficiallyInterested() {
      var cleanups =
          new Cleanups(withEffect("Become Superficially interested"), withStats(100, 100, 100));
      try (cleanups) {
        // Superficially interested gives -5 combat, toggle to get +5 combat
        assertTrue(maximize("combat"));
        assertThat(
            getBoosts(),
            hasItem(hasProperty("cmd", equalTo("toggle Become Superficially interested"))));
      }
    }

    @Test
    public void doesNotSuggestToggleWithoutEffect() {
      var cleanups = new Cleanups(withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("toggle")))));
      }
    }
  }

  @Nested
  class EmitSlot {
    @Test
    public void suggestsEnthroningFamiliar() {
      var cleanups =
          new Cleanups(
              withEquippableItem("Crown of Thrones"),
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY),
              withFamiliarInTerrarium(FamiliarPool.LEPRECHAUN),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "Crown of Thrones")));
      }
    }

    @Test
    public void suggestsBjornifyingFamiliar() {
      var cleanups =
          new Cleanups(
              withEquippableItem("Buddy Bjorn"),
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY),
              withFamiliarInTerrarium(FamiliarPool.LEPRECHAUN),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.CONTAINER, "Buddy Bjorn")));
      }
    }

    @Test
    public void suggestsModeableItems() {
      var cleanups = new Cleanups(withEquippableItem("backup camera"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("init"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.ACCESSORY1, "backup camera")));
      }
    }
  }

  @Nested
  class Spacegate {
    @Test
    public void suggestsSpacegateWhenAlwaysAvailable() {
      var cleanups =
          new Cleanups(
              withProperty("spacegateAlways", true),
              withProperty("spacegateVaccine1", true),
              withProperty("_spacegateVaccine", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Rainbow Vaccine provides elemental resistances
        assertTrue(maximize("cold res"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("spacegate vaccine 1"))));
      }
    }

    @Test
    public void suggestsSpacegateWhenAvailableToday() {
      var cleanups =
          new Cleanups(
              withProperty("spacegateAlways", false),
              withProperty("_spacegateToday", true),
              withProperty("spacegateVaccine2", true),
              withProperty("_spacegateVaccine", false),
              withStats(100, 100, 100));
      try (cleanups) {
        // Broad-Spectrum Vaccine provides stat percents
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("spacegate vaccine 2"))));
      }
    }

    @Test
    public void doesNotSuggestSpacegateWhenUsed() {
      var cleanups =
          new Cleanups(
              withProperty("spacegateAlways", true),
              withProperty("spacegateVaccine1", true),
              withProperty("_spacegateVaccine", true),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("cold res"));
        assertThat(getBoosts(), not(hasItem(hasProperty("cmd", startsWith("spacegate")))));
      }
    }
  }

  @Nested
  class LoathingIdol {
    @Test
    public void suggestsLoathingIdolWhenAvailable() {
      var cleanups =
          new Cleanups(withItem(ItemPool.LOATHING_IDOL_MICROPHONE), withStats(100, 100, 100));
      try (cleanups) {
        // Spitting Rhymes provides Item Drop: +50
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("loathingidol rhyme"))));
      }
    }
  }

  @Nested
  class Skeleton {
    @Test
    public void suggestsSkeletonWhenAvailable() {
      var cleanups = new Cleanups(withItem(ItemPool.SKELETON), withStats(100, 100, 100));
      try (cleanups) {
        // Skeletal Buddy provides Experience: +2
        assertTrue(maximize("exp"));
        assertThat(getBoosts(), hasItem(hasProperty("cmd", equalTo("skeleton buddy"))));
      }
    }
  }
}
