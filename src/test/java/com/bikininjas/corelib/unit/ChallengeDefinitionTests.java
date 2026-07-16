package com.bikininjas.corelib.unit;

import com.bikininjas.corelib.objective.Challenge;
import com.bikininjas.corelib.objective.ChallengeDefinition;
import com.bikininjas.corelib.objective.Objective;
import com.bikininjas.corelib.objective.SurvivalObjective;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for {@link ChallengeDefinition}.
 */
class ChallengeDefinitionTests {

    @Test
    void compactConstructorDefensivelyCopiesLists() {
        var objectives = new java.util.ArrayList<Objective>(List.of(
                new SurvivalObjective("Survive", 100)));
        var mods = new java.util.ArrayList<>(List.of("mod_a"));
        var def = new ChallengeDefinition("test", "Test", objectives, 60, mods);
        objectives.add(new SurvivalObjective("Extra", 50));
        mods.add("mod_b");
        assertEquals(1, def.objectives().size());
        assertEquals(1, def.requiredMods().size());
    }

    @Test
    void convenienceConstructorDefaultsNoMods() {
        var obj = new SurvivalObjective("Survive", 100);
        var def = new ChallengeDefinition("no_mods", "No Mods", List.of(obj), 0);
        assertTrue(def.requiredMods().isEmpty());
    }

    @Test
    void toChallengeCreatesCorrectRuntimeChallenge() {
        var obj = new SurvivalObjective("Survive", 200);
        var def = new ChallengeDefinition("survival", "Survival", List.of(obj), 300);
        Challenge c = def.toChallenge();
        assertEquals("survival", c.name());
        assertEquals(1, c.objectives().size());
        assertEquals(300, c.timeLimitSeconds());
    }

    @Test
    void toChallengeDefensivelyCopiesObjectives() {
        var obj = new SurvivalObjective("Survive", 200);
        var def = new ChallengeDefinition("x", "X", List.of(obj), 0);
        Challenge c = def.toChallenge();
        assertEquals(1, c.objectives().size());
    }

    @Test
    void nameAndDisplayNameDiffer() {
        var def = new ChallengeDefinition("dragon_rush", "Dragon Rush",
                List.of(), 0);
        assertEquals("dragon_rush", def.name());
        assertEquals("Dragon Rush", def.displayName());
    }
}
