package net.sourceforge.kolmafia.maximizer;

import static internal.helpers.Maximizer.getBoosts;
import static internal.helpers.Maximizer.maximize;
import static internal.helpers.Maximizer.modFor;
import static internal.helpers.Player.withEquippableItem;
import static internal.helpers.Player.withFamiliar;
import static internal.helpers.Player.withSkill;
import static internal.helpers.Player.withStats;
import static internal.matchers.Maximizer.recommendsSlot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import internal.helpers.Cleanups;
import net.sourceforge.kolmafia.KoLCharacter;
import net.sourceforge.kolmafia.equipment.Slot;
import net.sourceforge.kolmafia.modifiers.DerivedModifier;
import net.sourceforge.kolmafia.modifiers.DoubleModifier;
import net.sourceforge.kolmafia.objectpool.FamiliarPool;
import net.sourceforge.kolmafia.preferences.Preferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the Maximizer that test real-world scenarios involving multiple components
 * working together. Tests here should simulate actual user workflows (e.g., setting up for meat
 * farming) and verify the maximizer produces sensible results.
 *
 * <p>For unit tests of specific maximizer behaviors (e.g., slot handling, path restrictions,
 * familiar switching), use {@link MaximizerTest} instead.
 */
public class MaximizerIntegrationTest {
  @BeforeAll
  public static void beforeAll() {
    KoLCharacter.reset("MaximizerIntegrationTest");
    Preferences.reset("MaximizerIntegrationTest");
  }

  @Nested
  class RealWorldScenarios {
    @Test
    public void meatFarmingSetup() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withFamiliar(FamiliarPool.LEPRECHAUN),
              withSkill("Disco Leer"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        // Should recommend meat-boosting equipment and effects
        assertThat(modFor(DoubleModifier.MEATDROP), greaterThan(0.0));
      }
    }

    @Test
    public void itemFarmingSetup() {
      var cleanups =
          new Cleanups(
              withEquippableItem("bounty-hunting helmet"),
              withFamiliar(FamiliarPool.BABY_GRAVY_FAIRY),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("item"));
        // Should recommend item-boosting equipment
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "bounty-hunting helmet")));
      }
    }
  }

  @Nested
  class ComplexExpressions {
    @Test
    public void multipleConstraintsWithMinMax() {
      var cleanups =
          new Cleanups(
              withEquippableItem("helmet turtle"),
              withEquippableItem("hardened slime hat"),
              withSkill("Refusal to Freeze"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus 100 min, cold res 5 max"));
        // Should find gear that meets all constraints
        assertThat(modFor(DerivedModifier.BUFFED_MUS), greaterThan(99.0));
      }
    }

    @Test
    public void outfitWithModifierCombination() {
      var cleanups =
          new Cleanups(
              withEquippableItem("bugbear beanie"),
              withEquippableItem("bugbear bungguard"),
              withEquippableItem("helmet turtle"),
              withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("mus, +outfit bugbear costume"));
        // Should equip the outfit pieces
        assertThat(getBoosts(), hasItem(recommendsSlot(Slot.HAT, "bugbear beanie")));
      }
    }
  }

  @Nested
  class ExecutionResults {
    @Test
    public void effectCastingSuggested() {
      var cleanups = new Cleanups(withSkill("Disco Leer"), withStats(100, 100, 100));
      try (cleanups) {
        assertTrue(maximize("meat"));
        var boosts = getBoosts();
        // Should suggest casting Disco Leer for meat drop
        assertThat(boosts, hasItem(hasProperty("cmd", startsWith("cast"))));
      }
    }
  }

  @Nested
  class ErrorRecovery {
    @Test
    public void invalidExpressionHandledGracefully() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"));
      try (cleanups) {
        assertFalse(maximize("xyzzy invalid keyword"));
        // Invalid expression should return false, not throw
      }
    }

    @Test
    public void impossibleConstraintsReturnFalse() {
      var cleanups = new Cleanups(withEquippableItem("helmet turtle"));
      try (cleanups) {
        assertFalse(maximize("mus 10000 min"));
        // Impossible min constraint should return false
      }
    }
  }
}
