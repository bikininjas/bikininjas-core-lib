package com.bikininjas.corelib.unit;

import com.bikininjas.corelib.log.LogManager;
import com.bikininjas.corelib.log.ModLogger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit5 unit tests for {@link ModLogger} and {@link LogManager}.
 * <p>
 * These tests verify the API contract (construction, chaining, no-throw
 * guarantees). Actual log output is governed by SLF4J/Logback and is not
 * verified here.
 */
class ModLoggerTests {

    private static final String TEST_MOD = "test_mod";
    private final ModLogger logger = LogManager.getLogger(TEST_MOD, ModLoggerTests.class);

    // ──────────────────────────────────────────────
    //  Construction & accessors
    // ──────────────────────────────────────────────

    @Test
    void getLoggerReturnsNonNull() {
        assertNotNull(logger, "ModLogger must not be null");
    }

    @Test
    void modIdMatchesInput() {
        assertEquals(TEST_MOD, logger.modId());
    }

    @Test
    void classNameMatchesSimpleName() {
        assertEquals("ModLoggerTests", logger.className());
    }

    // ──────────────────────────────────────────────
    //  Level methods — no-throw
    // ──────────────────────────────────────────────

    @Test
    void infoDoesNotThrow() {
        assertDoesNotThrow(() -> logger.info("Test info message"));
    }

    @Test
    void infoWithArgsDoesNotThrow() {
        assertDoesNotThrow(() -> logger.info("Test {} arg {}", "foo", 42));
    }

    @Test
    void debugDoesNotThrow() {
        assertDoesNotThrow(() -> logger.debug("Test debug message"));
    }

    @Test
    void debugWithArgsDoesNotThrow() {
        assertDoesNotThrow(() -> logger.debug("Player {} joined", "player_1"));
    }

    @Test
    void warnDoesNotThrow() {
        assertDoesNotThrow(() -> logger.warn("Test warn message"));
    }

    @Test
    void warnWithArgsDoesNotThrow() {
        assertDoesNotThrow(() -> logger.warn("Resource {} not found", "minecraft:diamond"));
    }

    @Test
    void traceDoesNotThrow() {
        assertDoesNotThrow(() -> logger.trace("Test trace message"));
    }

    // ──────────────────────────────────────────────
    //  ErrorBuilder — chaining & no-throw
    // ──────────────────────────────────────────────

    @Test
    void errorBuilderIsNotNull() {
        assertNotNull(logger.error("Test error"));
    }

    @Test
    void errorBuilderChainsCtx() {
        var builder = logger.error("Error")
                .ctx("key1", "value1")
                .ctx("key2", 42);
        assertNotNull(builder);
    }

    @Test
    void errorBuilderReportDoesNotThrow() {
        assertDoesNotThrow(() ->
                logger.error("Something broke")
                        .ctx("item", "super_sword")
                        .ctx("tier", "IRON_PLUS")
                        .cause(new RuntimeException("test exception"))
                        .report()
        );
    }

    @Test
    void errorBuilderReportWithoutCauseDoesNotThrow() {
        assertDoesNotThrow(() ->
                logger.error("Something broke")
                        .ctx("item", "super_sword")
                        .report()
        );
    }

    @Test
    void errorBuilderReportWithModOverrideDoesNotThrow() {
        assertDoesNotThrow(() ->
                logger.error("Cross-mod error")
                        .mod("other_mod")
                        .ctx("source", TEST_MOD)
                        .cause(new IllegalStateException("cross-mod failure"))
                        .report()
        );
    }

    @Test
    void errorBuilderEmptyContextDoesNotThrow() {
        assertDoesNotThrow(() -> logger.error("Minimal error").report());
    }

    @Test
    void errorBuilderMultipleCtxEntries() {
        var builder = logger.error("Multi context test");
        for (int i = 0; i < 10; i++) {
            builder.ctx("key" + i, "val" + i);
        }
        assertDoesNotThrow(builder::report);
    }

    // ──────────────────────────────────────────────
    //  Null-safety
    // ──────────────────────────────────────────────

    @Test
    void nullModIdThrows() {
        assertThrows(NullPointerException.class, () -> LogManager.getLogger(null, getClass()));
    }

    @Test
    void nullClassThrows() {
        assertThrows(NullPointerException.class, () -> LogManager.getLogger(TEST_MOD, null));
    }

    @Test
    void errorWithDisabledLevelDoesNotThrow() {
        // Just verify the no-op path never blows up
        assertDoesNotThrow(() -> logger.error("disabled test").report());
    }
}
