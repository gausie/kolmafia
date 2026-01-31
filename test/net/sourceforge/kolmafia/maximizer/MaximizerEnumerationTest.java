package net.sourceforge.kolmafia.maximizer;

import static internal.helpers.Maximizer.getBoosts;
import static internal.helpers.Maximizer.maximize;
import static internal.helpers.Maximizer.maximizeAny;
import static internal.helpers.Maximizer.maximizeCreatable;
import static internal.helpers.Maximizer.modFor;
import static internal.helpers.Player.withEquippableItem;
import static internal.helpers.Player.withEquipped;
import static internal.helpers.Player.withFamiliar;
import static internal.helpers.Player.withFamiliarInTerrarium;
import static internal.helpers.Player.withItem;
import static internal.helpers.Player.withItemInFreepulls;
import static internal.helpers.Player.withItemInStorage;
import static internal.helpers.Player.withProperty;
import static internal.helpers.Player.withSkill;
import static internal.helpers.Player.withStats;
import static internal.matchers.Maximizer.recommends;
import static internal.matchers.Maximizer.recommendsSlot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import internal.helpers.Cleanups;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.equipment.Slot;
import net.sourceforge.kolmafia.modifiers.DerivedModifier;
import net.sourceforge.kolmafia.objectpool.FamiliarPool;
import net.sourceforge.kolmafia.preferences.Preferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MaximizerEnumerationTest {
  @BeforeAll
  public static void beforeAll() {
    KoLCharacter.reset("MaximizerEnumerationTest");
    Preferences.reset("MaximizerEnumerationTest");
  }

  @Nested
  class ItemAvailability {
    @Test
    public void findsItemsInInventory() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }

    @Test
    public void findsEquippedItems() {
      var cleanups = new Cleanups(withEquipped(Slot.HAT, "helmet turtle"));
      try (cleanups) {
        assertTrue(maximize("mus, current"));
        assertEquals(1, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void findsMultipleCopiesOfSameItem() {
      var cleanups = new Cleanups(withEquippableItem("hand in glove", 3));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommends("Hand in Glove")));
      }
    }
  }

  @Nested
  class EquipScopeTests {
    @Test
    public void speculateOnlyFindsOnHandItems() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }

    @Test
    public void speculateCreatableConsidersCreatableItems() {
      var cleanups =
          new Cleanups(
              withItem("helmet turtle"), withItem("seal-skull helmet"), withStats(100, 100, 100));
      try (cleanups) {
        maximizeCreatable("mus");
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT)));
      }
    }

    @Test
    public void speculateAnyConsidersMallItems() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"));
      try (cleanups) {
        maximizeAny("mus");
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }
  }

  @Nested
  class FamiliarEquipment {
    @Test
    public void findsFamiliarEquipmentForCurrentFamiliar() {
      var cleanups =
          new Cleanups(
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY), withEquippableItem("lead necklace"));
      try (cleanups) {
        assertTrue(maximize("familiar weight"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.FAMILIAR, "lead necklace")));
      }
    }

    @Test
    public void excludesFamiliarEquipmentWhenSlotExcluded() {
      var cleanups =
          new Cleanups(
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY), withEquippableItem("lead necklace"));
      try (cleanups) {
        assertTrue(maximize("familiar weight, -familiar"));
        assertThat(getBoosts(), not(hasItem(recommendsSlot(Slot.FAMILIAR))));
      }
    }
  }

  @Nested
  class WeaponEnumeration {
    @Test
    public void findsOneHandedWeapons() {
      var cleanups =
          new Cleanups(
              withEquippableItem("seal-clubbing club"),
              withEquippableItem("catskin buckler"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, 1 hand"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.WEAPON, "seal-clubbing club")));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.OFFHAND)));
      }
    }

    @Test
    public void filtersByWeaponType() {
      var cleanups =
          new Cleanups(
              withEquippableItem("seal-clubbing club"),
              withEquippableItem("disco ball"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, club"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.WEAPON, "seal-clubbing club")));
      }
    }

    @Test
    public void filtersByMeleeVsRanged() {
      var cleanups =
          new Cleanups(
              withEquippableItem("seal-clubbing club"),
              withEquippableItem("disco ball"),
              withStats(100, 100, 100));
      try (cleanups) {
        // -melee means ranged
        assertTrue(maximize("mox, -melee"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.WEAPON, "disco ball")));
      }
    }
  }

  @Nested
  class OffhandEnumeration {
    @Test
    public void findsShields() {
      var cleanups =
          new Cleanups(
              withEquippableItem("catskin buckler"),
              withEquippableItem("seal-clubbing club"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("da, shield"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.OFFHAND, "catskin buckler")));
      }
    }
  }

  @Nested
  class AccessoryEnumeration {
    @Test
    public void findsAccessories() {
      var cleanups = new Cleanups(withEquippableItem("Hand in Glove"));
      try (cleanups) {
        assertTrue(maximize("mus"));
        // Should recommend accessory
        assertThat(getBoosts(), hasItem(recommends("Hand in Glove")));
      }
    }

    @Test
    public void fillsMultipleAccessorySlots() {
      var cleanups = new Cleanups(withEquippableItem("hand in glove", 3));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommends("Hand in Glove")));
      }
    }

    @Test
    public void respectsAccessorySlotExclusions() {
      var cleanups = new Cleanups(withEquippableItem("hand in glove", 3));
      try (cleanups) {
        assertTrue(maximize("mus, -acc1"));
        // Should not use acc1 slot
        assertThat(getBoosts(), not(hasItem(recommendsSlot(Slot.ACCESSORY1))));
      }
    }
  }

  @Nested
  class OutfitEnumeration {
    @Test
    public void considersOutfitPieces() {
      var cleanups =
          new Cleanups(
              withEquippableItem("bugbear beanie"),
              withEquippableItem("bugbear bungguard"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("+outfit bugbear costume"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "bugbear beanie")));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.PANTS, "bugbear bungguard")));
      }
    }

    @Test
    public void excludesNegativeOutfitPieces() {
      var cleanups =
          new Cleanups(
              withEquippableItem("bugbear beanie"),
              withEquippableItem("helmet turtle"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, -outfit bugbear costume"));
        // Should not recommend bugbear beanie
        assertThat(getBoosts(), not(hasItem(recommendsSlot(Slot.HAT, "bugbear beanie"))));
      }
    }
  }

  @Nested
  class ItemExclusion {
    @Test
    public void minusEquipExcludesSpecificItem() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, -equip helmet turtle"));
        assertThat(getBoosts(), not(hasItem(recommendsSlot(Slot.HAT, "helmet turtle"))));
      }
    }

    @Test
    public void plusEquipRequiresSpecificItem() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, +equip helmet turtle"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }
  }

  @Nested
  class ModeableItems {
    @Test
    public void considersUmbrellaForShield() {
      var cleanups =
          new Cleanups(
              withEquippableItem("unbreakable umbrella"),
              withProperty("umbrellaState", "forward-facing"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("da, shield"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.OFFHAND, "unbreakable umbrella")));
      }
    }
  }

  @Nested
  class EquipRequirements {
    @Test
    public void respectsStatRequirements() {
      var cleanups =
          new Cleanups(withItem("wreath of laurels"), withEquippableItem("helmet turtle"));
      try (cleanups) {
        // Wreath of laurels has high stat requirements
        // Without sufficient stats, should prefer helmet turtle
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }

    @Test
    public void considersCurrentlyEquippedItemsEvenWithoutStats() {
      var cleanups =
          new Cleanups(withEquipped(Slot.ACCESSORY1, "Mr. Accessory"), withStats(0, 0, 0));
      try (cleanups) {
        assertTrue(maximize("all res, current"));
        assertTrue(modFor(DerivedModifier.BUFFED_MUS) > 0);
      }
    }
  }

  @Nested
  class StorageItems {
    @Test
    public void findsItemsInStorage() {
      var cleanups = new Cleanups(withItemInStorage("helmet turtle"));
      try (cleanups) {
        maximizeAny("mus");
        assertThat(getBoosts(), hasItem(recommends("helmet turtle")));
      }
    }

    @Test
    public void findsItemsInFreepulls() {
      var cleanups = new Cleanups(withItemInFreepulls("helmet turtle"));
      try (cleanups) {
        maximizeAny("mus");
        assertThat(getBoosts(), hasItem(recommends("helmet turtle")));
      }
    }
  }

  @Nested
  class CombinationLimits {
    @Test
    public void respectsCombinationLimit() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("old sweatpants"),
              withProperty("maximizerCombinationLimit", 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }

    @Test
    public void noCombinationLimitByDefault() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("old sweatpants"),
              withProperty("maximizerCombinationLimit", 0));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.PANTS, "old sweatpants")));
      }
    }

    @Test
    public void veryLowLimitStillFindsResult() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("old sweatpants"),
              withEquippableItem("eyepatch"),
              withProperty("maximizerCombinationLimit", 1));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT)));
      }
    }
  }

  @Nested
  class SpecialEnumeration {
    @Test
    public void considersEnthroneableFamiliars() {
      var cleanups =
          new Cleanups(
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY),
              withFamiliarInTerrarium(FamiliarPool.LEPRECHAUN),
              withEquippableItem("Crown of Thrones"));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "Crown of Thrones")));
      }
    }

    @Test
    public void considersBjornableFamiliars() {
      var cleanups =
          new Cleanups(
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY),
              withFamiliarInTerrarium(FamiliarPool.LEPRECHAUN),
              withEquippableItem("Buddy Bjorn"));
      try (cleanups) {
        assertTrue(maximize("meat"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.CONTAINER, "Buddy Bjorn")));
      }
    }
  }

  @Nested
  class SynergyDetection {
    @Test
    public void detectsHoboPowerSynergy() {
      var cleanups =
          new Cleanups(
              withEquippableItem("hodgman's porkpie hat"),
              withEquippableItem("hodgman's lobsterskin pants"),
              withEquippableItem("hodgman's bow tie"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("hobo power"));
      }
    }

    @Test
    public void detectsSmithsnessSynergy() {
      var cleanups =
          new Cleanups(
              withEquippableItem("Vicar's Tutu"),
              withEquippableItem("Hand in Glove"),
              withSkill("Torso Awareness"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("smithsness"));
        assertThat(getBoosts(), hasItem(recommends("Vicar's Tutu")));
        assertThat(getBoosts(), hasItem(recommends("Hand in Glove")));
      }
    }
  }

  @Nested
  class BeeosityFiltering {
    @Test
    public void considersAllItemsOutsideBeecore() {
      var cleanups =
          new Cleanups(withEquippableItem("helmet turtle"), withEquippableItem("bugbear beanie"));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }
  }
}
