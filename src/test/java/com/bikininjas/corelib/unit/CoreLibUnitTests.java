package com.bikininjas.corelib.unit;

import com.bikininjas.corelib.CoreLib;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit5 unit tests — no Minecraft runtime required.
 * These run with {@code ./gradlew test} (uses {@code unitTest} block).
 */
class CoreLibUnitTests {

    @Test
    void modIdIsLowerCase() {
        assertEquals("core_lib", CoreLib.MODID);
        assertTrue(CoreLib.MODID.matches("[a-z][a-z0-9_]{1,63}"),
                "MODID must match NeoForge convention [a-z][a-z0-9_]{1,63}");
    }

    @Test
    void modIdLengthIsWithinBounds() {
        int len = CoreLib.MODID.length();
        assertTrue(len >= 2 && len <= 64,
                "MODID length " + len + " is outside the allowed range 2–64");
    }
}
