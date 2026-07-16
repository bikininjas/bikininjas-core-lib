package com.bikininjas.corelib.unit;

import com.bikininjas.corelib.objective.ChallengeDefinition;
import com.bikininjas.corelib.objective.ChallengeRegistry;
import com.bikininjas.corelib.objective.SurvivalObjective;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for {@link ChallengeRegistry}.
 * <p>
 * {@link ChallengeRegistry#areModsLoaded(ChallengeDefinition)} uses
 * {@code ModList.get()} which returns {@code null} outside a Minecraft runtime;
 * the registry handles this by assuming all mods are loaded.
 */
class ChallengeRegistryTests {

    private static final ChallengeDefinition DEF_A = new ChallengeDefinition(
            "survive", "Survive", List.of(new SurvivalObjective("Live", 100)), 0);
    private static final ChallengeDefinition DEF_B = new ChallengeDefinition(
            "rush", "Rush", List.of(), 300);
    private static final ChallengeDefinition DEF_WITH_VANILLA_MOD = new ChallengeDefinition(
            "vanilla", "Vanilla", List.of(), 0, List.of("minecraft"));
    private static final ChallengeDefinition DEF_WITH_UNKNOWN_MOD = new ChallengeDefinition(
            "unknown", "Unknown", List.of(), 0, List.of("some_unloaded_mod"));

    @BeforeEach
    void setUp() {
        ChallengeRegistry.clear();
    }

    @Test
    void registerAddsDefinition() {
        ChallengeRegistry.register(DEF_A);
        assertEquals(1, ChallengeRegistry.count());
        assertSame(DEF_A, ChallengeRegistry.get("survive"));
    }

    @Test
    void registerOverwritesDuplicate() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.register(DEF_A);
        assertEquals(1, ChallengeRegistry.count());
    }

    @Test
    void getReturnsNullForUnknown() {
        assertNull(ChallengeRegistry.get("does_not_exist"));
    }

    @Test
    void getAllReturnsAllDefinitions() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.register(DEF_B);
        assertEquals(2, ChallengeRegistry.getAll().size());
    }

    @Test
    void getAllReturnsImmutableCopy() {
        ChallengeRegistry.register(DEF_A);
        var list = ChallengeRegistry.getAll();
        assertThrows(UnsupportedOperationException.class, () -> list.add(DEF_B));
    }

    @Test
    void getAvailableIncludesAllWhenNoModsRequired() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.register(DEF_B);
        assertEquals(2, ChallengeRegistry.getAvailable().size());
    }

    @Test
    void getAvailableIncludesModdedWhenModIsLoaded() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.register(DEF_WITH_VANILLA_MOD);
        // 'minecraft' is always loaded in a NeoForge test environment
        assertEquals(2, ChallengeRegistry.getAvailable().size());
    }

    @Test
    void getAvailableFiltersUnknownMod() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.register(DEF_WITH_UNKNOWN_MOD);
        // 'some_unloaded_mod' is not loaded → DEF_WITH_UNKNOWN_MOD filtered out
        assertEquals(1, ChallengeRegistry.getAvailable().size());
    }

    @Test
    void removeDeletesDefinition() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.remove("survive");
        assertNull(ChallengeRegistry.get("survive"));
        assertEquals(0, ChallengeRegistry.count());
    }

    @Test
    void clearEmptiesRegistry() {
        ChallengeRegistry.register(DEF_A);
        ChallengeRegistry.register(DEF_B);
        ChallengeRegistry.clear();
        assertEquals(0, ChallengeRegistry.count());
    }

    @Test
    void areModsLoadedReturnsTrueForLoadedMod() {
        assertTrue(ChallengeRegistry.areModsLoaded(DEF_WITH_VANILLA_MOD));
    }

    @Test
    void areModsLoadedReturnsFalseForUnknownMod() {
        assertFalse(ChallengeRegistry.areModsLoaded(DEF_WITH_UNKNOWN_MOD));
    }
}
