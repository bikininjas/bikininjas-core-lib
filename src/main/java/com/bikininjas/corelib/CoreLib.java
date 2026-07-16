package com.bikininjas.corelib;

import com.bikininjas.corelib.command.CommandRegister;
import com.bikininjas.corelib.network.NetworkHandler;
import com.bikininjas.corelib.objective.ObjectiveTracker;
import com.bikininjas.corelib.randomevent.RandomEventManager;
import com.bikininjas.corelib.registry.Registers;
import com.bikininjas.corelib.restriction.RestrictionManager;
import com.bikininjas.corelib.stats.PlayerStatsManager;
import com.bikininjas.corelib.time.TimeManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CoreLib.MODID)
public final class CoreLib {

    public static final String MODID = "core_lib";

    public CoreLib(IEventBus modBus) {
        Registers.ITEMS.register(modBus);
        Registers.BLOCKS.register(modBus);
        Registers.BLOCK_ENTITY_TYPES.register(modBus);
        Registers.ENTITY_TYPES.register(modBus);

        initModules();
        NetworkHandler.register(modBus);

        modBus.addListener(FMLClientSetupEvent.class, event ->
                NeoForge.EVENT_BUS.register(
                        com.bikininjas.corelib.client.StatsOverlayRenderer.class));
    }

    private static void initModules() {
        TimeManager.init();
        ObjectiveTracker.init();
        CommandRegister.init();
        PlayerStatsManager.init();
        RestrictionManager.init();

        RandomEventManager.getInstance();
    }
}
