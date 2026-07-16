package com.bikininjas.corelib.unit;

import com.bikininjas.corelib.stats.PlayerStats;
import com.bikininjas.corelib.stats.PlayerStatsManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit5 unit tests for {@link PlayerStatsManager} and {@link PlayerStats}.
 * <p>
 * These run without a Minecraft runtime ({@code ./gradlew test}). Only the
 * static, side-effect-free logic is exercised here; methods that require a live
 * {@code ServerPlayer} are validated structurally (final class, private
 * constructor) and via the immutable {@link PlayerStats} value type. Event-driven
 * accumulation cannot be tested without a Minecraft runtime and is intentionally
 * out of scope for these pure tests.
 */
class PlayerStatsManagerTests {

    // ──────────────────────────────────────────────
    //  Structure
    // ──────────────────────────────────────────────

    @Test
    void classIsFinal() {
        assertTrue(Modifier.isFinal(PlayerStatsManager.class.getModifiers()),
                "PlayerStatsManager must be a final utility class");
    }

    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<PlayerStatsManager> ctor = PlayerStatsManager.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(ctor.getModifiers()),
                "PlayerStatsManager constructor must be private");
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance(),
                "Private constructor should be invokable (no side effects)");
    }

    // ──────────────────────────────────────────────
    //  PlayerStats value type
    // ──────────────────────────────────────────────

    @Test
    void emptyRecordIsAllZeros() {
        PlayerStats empty = PlayerStats.EMPTY;
        assertEquals(0, empty.deaths(), "EMPTY deaths must be 0");
        assertEquals(0, empty.kills(), "EMPTY kills must be 0");
        assertEquals(0, empty.blocksBroken(), "EMPTY blocksBroken must be 0");
        assertEquals(0, empty.crafts(), "EMPTY crafts must be 0");
    }

    @Test
    void recordHoldsValues() {
        PlayerStats stats = new PlayerStats(1, 2, 3, 4);
        assertEquals(1, stats.deaths());
        assertEquals(2, stats.kills());
        assertEquals(3, stats.blocksBroken());
        assertEquals(4, stats.crafts());
    }

    // ──────────────────────────────────────────────
    //  Pure method guards (no Minecraft runtime needed)
    // ──────────────────────────────────────────────

    @Test
    void getStatsRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> PlayerStatsManager.getStats(null),
                "getStats(null) must throw NullPointerException");
    }

    @Test
    void resetStatsRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> PlayerStatsManager.resetStats(null),
                "resetStats(null) must throw NullPointerException");
    }

    @Test
    void gettersRejectNull() {
        assertAll("all getters reject null",
                () -> assertThrows(NullPointerException.class,
                        () -> PlayerStatsManager.getDeaths(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> PlayerStatsManager.getKills(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> PlayerStatsManager.getBlocksBroken(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> PlayerStatsManager.getCrafts(null)));
    }
}
