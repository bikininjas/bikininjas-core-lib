package com.bikininjas.corelib.gametest;

import com.bikininjas.corelib.CoreLib;
import com.bikininjas.corelib.registry.Registers;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CoreLib.MODID)
@PrefixGameTestTemplate(false)
public final class CoreLibGameTests {

    /**
     * Verify that the core-lib mod registers are populated at game init.
     * This uses the empty10 debug structure as a minimal template.
     */
    @GameTest(template = "empty10")
    public static void coreLibRegistersExist(GameTestHelper helper) {
        // Assert that the DeferredRegisters are attached to the registry
        var itemKey = Registers.ITEMS.getRegistryKey();
        if (itemKey == null) {
            helper.fail("ITEMS DeferredRegister not registered");
            return;
        }
        helper.succeed();
    }

    /**
     * Verify that the core-lib mod class can be loaded.
     */
    @GameTest(template = "empty10")
    public static void coreLibModIdIsCorrect(GameTestHelper helper) {
        if (!"core_lib".equals(CoreLib.MODID)) {
            helper.fail("MODID mismatch");
            return;
        }
        helper.succeed();
    }
}
