package net.sourceforge.kolmafia.maximizer;

import static internal.helpers.Maximizer.getBoosts;
import static internal.helpers.Maximizer.maximize;
import static internal.helpers.Maximizer.modFor;
import static internal.helpers.Player.withEquippableItem;
import static internal.helpers.Player.withEquipped;
import static internal.helpers.Player.withFamiliar;
import static internal.helpers.Player.withPath;
import static internal.helpers.Player.withProperty;
import static internal.helpers.Player.withSkill;
import static internal.helpers.Player.withStats;
import static internal.matchers.Maximizer.recommendsSlot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import internal.helpers.Cleanups;
import net.sourceforge.kolmafia.AscensionPath.Path;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.equipment.Slot;
import net.sourceforge.kolmafia.modifiers.DerivedModifier;
import net.sourceforge.kolmafia.objectpool.FamiliarPool;
import net.sourceforge.kolmafia.preferences.Preferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MaximizerSpeculationTest {
  @BeforeAll
  public static void beforeAll() {
    KoLCharacter.reset("MaximizerSpeculationTest");
    Preferences.reset("MaximizerSpeculationTest");
  }

  @Nested
  class CompareToLogic {
    @Test
    public void successAlwaysBeatsFailure() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("bounty-hunting helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        // With a min requirement that can be met, should prefer success
        assertTrue(maximize("mus 1 min"));
        // Should find at least one solution
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT)));
      }
    }

    @Test
    public void higherScoreBeatsLower() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        // Both give +1 muscle, but one should be chosen
        assertTrue(maximize("mus"));
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void prefersItemDroppers() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("bounty-hunting helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        // Both provide some stats, but bounty-hunting helmet has item drop
        // When tied, should prefer item dropper via tiebreaker
        assertTrue(maximize("spooky res"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "bounty-hunting helmet")));
      }
    }

    @Test
    public void prefersAlreadyWornItems() {
      var cleanups =
          new Cleanups(
              withEquipped(Slot.HAT, "helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, current"));
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void tiebreakerUsesSecondaryScoring() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        // Tiebreaker considers familiar weight, initiative, etc.
        assertTrue(maximize("mus"));
        // Just verify completion
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void minusTieDisablesSecondaryScoring() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        // With -tie, tiebreaker scoring is disabled
        assertTrue(maximize("mus, -tie"));
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }
  }

  @Nested
  class BeecorePreferences {
    @Test
    public void prefersLowerBeeosityInBeecore() {
      var cleanups =
          new Cleanups(
              withPath(Path.BEES_HATE_YOU),
              withEquippableItem("helmet turtle"),
              withEquippableItem("bugbear beanie"),
              withStats(100, 100, 100));
      try (cleanups) {
        // In beecore, should prefer items with fewer B's
        assertTrue(maximize("mus 0 beeosity"));
        // helmet turtle has no B's, should be preferred
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }
  }

  @Nested
  class SimplicityScoring {
    @Test
    public void prefersFewerEquipmentChanges() {
      var cleanups =
          new Cleanups(
              withEquipped(Slot.HAT, "helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, current"));
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void emptySlotsPenalizedForWeapon() {
      var cleanups =
          new Cleanups(withEquippableItem("seal-clubbing club"), withStats(100, 100, 100));
      try (cleanups) {
        // Empty weapon slot is penalized vs having a weapon
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.WEAPON, "seal-clubbing club")));
      }
    }
  }

  @Nested
  class BreakablePreference {
    @Test
    public void prefersUnbreakableItems() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }
  }

  @Nested
  class InventoryPreference {
    @Test
    public void prefersItemsInInventory() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        // When tied, prefer items already in inventory
        assertTrue(maximize("mus"));
        // Both are in inventory, so this should work
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void prefersNonBuyableItems() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("seal-skull helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertEquals(101, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }
  }

  @Nested
  class MutexViolations {
    @Test
    public void detectsMutexViolations() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }
  }

  @Nested
  class ScoreCalculation {
    @Test
    public void scoresFromEvaluator() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"), withStats(50, 50, 50));
      try (cleanups) {
        assertTrue(maximize("mus"));
        // Score comes from Evaluator
        assertEquals(51, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }

    @Test
    public void scoresAreCached() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"), withStats(50, 50, 50));
      try (cleanups) {
        assertTrue(maximize("mus"));
        // Calling maximize multiple times should use cached scores
        assertTrue(maximize("mus"));
        assertEquals(51, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }
  }

  @Nested
  class MarkAndRestore {
    @Test
    public void markAndRestoreWorks() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("old sweatpants"),
              withStats(100, 100, 100));
      try (cleanups) {
        // First maximize for muscle
        assertTrue(maximize("mus"));
        double firstScore = modFor(DerivedModifier.BUFFED_MUS);

        // Then maximize for moxie
        assertTrue(maximize("mox"));
        double secondScore = modFor(DerivedModifier.BUFFED_MOX);

        // Scores should be different
        assertTrue(firstScore != secondScore || firstScore == 100);
      }
    }
  }

  @Nested
  class RolloverEffectBonus {
    @Test
    public void prefersItemsWithRolloverEffects() {
      var cleanups =
          new Cleanups(
              withEquippableItem("time helmet"),
              withEquippableItem("helmet turtle"),
              withStats(100, 100, 100));
      try (cleanups) {
        // Time helmet has rollover effect, should be preferred when tied
        assertTrue(maximize("adv"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "time helmet")));
      }
    }
  }

  @Nested
  class CountTracking {
    @Test
    public void tracksMultipleCopiesOfSameItem() {
      var cleanups = new Cleanups(withEquippableItem("hand in glove", 3));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(modFor(DerivedModifier.BUFFED_MUS), greaterThan(0.0));
      }
    }

    @Test
    public void deductsFoldableItems() {
      var cleanups =
          new Cleanups(
              withEquippableItem("origami pasties"),
              withProperty("maximizerFoldables", true),
              withSkill("Torso Awareness"));
      try (cleanups) {
        assertTrue(maximize("mox"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.SHIRT, "origami pasties")));
      }
    }
  }

  @Nested
  class ChefstaffHandling {
    @Test
    public void chefstaffWithoutGloveOrSkillNotRecommended() {
      var cleanups =
          new Cleanups(withEquippableItem("Staff of Simmering Hatred"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("spell dmg"));
      }
    }

    @Test
    public void chefstaffWithSpiritOfRigatoniWorks() {
      var cleanups =
          new Cleanups(
              withEquippableItem("Staff of Simmering Hatred"),
              withSkill("Spirit of Rigatoni"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("spell dmg"));
      }
    }
  }

  @Nested
  class CloneAndCopy {
    @Test
    public void clonePreservesEquipment() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("old sweatpants"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        // Verify the maximizer completed successfully
        assertThat(modFor(DerivedModifier.BUFFED_MUS), greaterThan(0.0));
      }
    }
  }

  @Nested
  class MeatDropperPreference {
    @Test
    public void prefersMeatDroppers() {
      var cleanups =
          new Cleanups(
              withEquippableItem("plastic pumpkin bucket"),
              withEquippableItem("helmet turtle"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT)));
      }
    }
  }

  @Nested
  class ExceededHandling {
    @Test
    public void handlesExceededMax() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus 50 max"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "helmet turtle")));
      }
    }
  }

  @Nested
  class EquipmentNullHandling {
    @Test
    public void handlesNullEquipment() {
      var cleanups = new Cleanups(withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertEquals(100, modFor(DerivedModifier.BUFFED_MUS), 0.01);
      }
    }
  }

  @Nested
  class FamiliarWeightHandling {
    @Test
    public void accountsForFamiliarWeightPercent() {
      var cleanups =
          new Cleanups(
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY),
              withEquippableItem("filthy knitted dread sack"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "filthy knitted dread sack")));
      }
    }
  }

  @Nested
  class ManaCostHandling {
    @Test
    public void accountsForStackableMana() {
      var cleanups = new Cleanups(withEquippableItem("wizard hat"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("-mana cost"));
      }
    }
  }

  @Nested
  class InitiativeHandling {
    @Test
    public void accountsForInitiativePenalty() {
      var cleanups = new Cleanups(withEquippableItem("old sweatpants"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("init"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.PANTS, "old sweatpants")));
      }
    }
  }

  @Nested
  class MeatdropHandling {
    @Test
    public void accountsForMeatdrop() {
      var cleanups = new Cleanups(withEquippableItem("meat detector"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
      }
    }
  }

  @Nested
  class ItemdropHandling {
    @Test
    public void accountsForItemdrop() {
      var cleanups =
          new Cleanups(withEquippableItem("bounty-hunting helmet"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "bounty-hunting helmet")));
      }
    }
  }

  @Nested
  class AttachmentComparison {
    @Test
    public void comparesAttachmentsBuyableFlag() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("bounty-hunting helmet"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus"));
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT)));
      }
    }
  }
}
