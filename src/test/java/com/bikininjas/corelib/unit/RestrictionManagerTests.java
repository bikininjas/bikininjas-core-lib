package com.bikininjas.corelib.unit;

import com.bikininjas.corelib.restriction.RestrictionManager;
import com.bikininjas.corelib.restriction.RestrictionType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit5 unit tests for {@link RestrictionManager}.
 * <p>
 * These run without a Minecraft runtime ({@code ./gradlew test}). Only the
 * static, side-effect-free registry logic is exercised here; the event-driven
 * enforcement is validated structurally (final class, private constructor) and
 * via the public registry API. The NeoForge event bus subscription happens in a
 * static initializer but does not require a live game to load the class.
 */
class RestrictionManagerTests {

    /** Ensure each test starts and ends with a clean registry. */
    @AfterEach
    void tearDown() {
        RestrictionManager.clear();
    }

    // ──────────────────────────────────────────────
    //  Structure
    // ──────────────────────────────────────────────

    @Test
    void classIsFinal() {
        assertTrue(Modifier.isFinal(RestrictionManager.class.getModifiers()),
                "RestrictionManager must be a final utility class");
    }

    @Test
    void constructorIsPrivateAndCallable() throws Exception {
        Constructor<RestrictionManager> ctor = RestrictionManager.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(ctor.getModifiers()),
                "RestrictionManager constructor must be private");
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance(),
                "Private constructor should be invokable (no side effects)");
    }

    // ──────────────────────────────────────────────
    //  RestrictionType enum
    // ──────────────────────────────────────────────

    @Test
    void restrictionTypeHasAllRequiredValues() {
        RestrictionType[] values = RestrictionType.values();
        assertEquals(5, values.length, "RestrictionType must declare exactly 5 values");
        assertDoesNotThrow(() -> RestrictionType.valueOf("PLACE_BLOCK"));
        assertDoesNotThrow(() -> RestrictionType.valueOf("BREAK_BLOCK"));
        assertDoesNotThrow(() -> RestrictionType.valueOf("USE_ITEM"));
        assertDoesNotThrow(() -> RestrictionType.valueOf("SPAWN_ENTITY"));
        assertDoesNotThrow(() -> RestrictionType.valueOf("ENTER_DIMENSION"));
    }

    // ──────────────────────────────────────────────
    //  Registry lifecycle (no Minecraft runtime needed)
    // ──────────────────────────────────────────────

    @Test
    void registerReturnsTrueWhenNew() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "block_a");
        assertTrue(RestrictionManager.register(id, RestrictionType.PLACE_BLOCK),
                "register must return true for a new restriction");
    }

    @Test
    void registerReturnsFalseWhenAlreadyRegistered() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "block_a");
        RestrictionManager.register(id, RestrictionType.PLACE_BLOCK);
        assertFalse(RestrictionManager.register(id, RestrictionType.BREAK_BLOCK),
                "register must return false when the resource is already registered");
        // The original type must be preserved, not overwritten.
        assertTrue(RestrictionManager.isRestricted(id, RestrictionType.PLACE_BLOCK),
                "Original restriction type must be preserved on duplicate register");
    }

    @Test
    void unregisterReturnsTrueWhenPresent() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "block_a");
        RestrictionManager.register(id, RestrictionType.PLACE_BLOCK);
        assertTrue(RestrictionManager.unregister(id),
                "unregister must return true when the restriction exists");
    }

    @Test
    void unregisterReturnsFalseWhenAbsent() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "missing");
        assertFalse(RestrictionManager.unregister(id),
                "unregister must return false when no restriction exists");
    }

    @Test
    void isRestrictedMatchesExactType() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "block_a");
        RestrictionManager.register(id, RestrictionType.PLACE_BLOCK);
        assertTrue(RestrictionManager.isRestricted(id, RestrictionType.PLACE_BLOCK),
                "isRestricted must be true for the registered type");
        assertFalse(RestrictionManager.isRestricted(id, RestrictionType.BREAK_BLOCK),
                "isRestricted must be false for a different type");
    }

    @Test
    void isRestrictedFalseForUnknownResource() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "unknown");
        assertFalse(RestrictionManager.isRestricted(id, RestrictionType.USE_ITEM),
                "isRestricted must be false for an unregistered resource");
    }

    @Test
    void clearRemovesAllRestrictions() {
        RestrictionManager.register(
                ResourceLocation.fromNamespaceAndPath("mymod", "a"), RestrictionType.PLACE_BLOCK);
        RestrictionManager.register(
                ResourceLocation.fromNamespaceAndPath("mymod", "b"), RestrictionType.SPAWN_ENTITY);
        RestrictionManager.clear();
        assertTrue(RestrictionManager.getAll().isEmpty(),
                "clear must remove every registered restriction");
    }

    @Test
    void getAllReturnsUnmodifiableSnapshot() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "block_a");
        RestrictionManager.register(id, RestrictionType.PLACE_BLOCK);
        Map<ResourceLocation, RestrictionType> snapshot = RestrictionManager.getAll();
        assertEquals(1, snapshot.size(), "snapshot must contain the registered restriction");
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put(id, RestrictionType.USE_ITEM),
                "getAll must return an unmodifiable view");
    }

    @Test
    void getAllSnapshotIsIndependentOfLaterChanges() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mymod", "block_a");
        RestrictionManager.register(id, RestrictionType.PLACE_BLOCK);
        Map<ResourceLocation, RestrictionType> snapshot = RestrictionManager.getAll();
        RestrictionManager.clear();
        assertEquals(1, snapshot.size(),
                "snapshot must be a copy and unaffected by later clear()");
    }
}
