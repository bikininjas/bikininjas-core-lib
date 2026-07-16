package com.bikininjas.corelib.network;

import com.bikininjas.corelib.CoreLib;
import com.bikininjas.corelib.client.StatsClientData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Registers all core-lib network payloads on the mod event bus.
 * <p>
 * Auto-subscribed via {@code @EventBusSubscriber(bus = MOD)} so no
 * explicit registration in {@code CoreLib} is needed.
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = CoreLib.MODID)
public final class NetworkHandler {

    private NetworkHandler() {}

    @SubscribeEvent
    static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.0");

        registrar.playToClient(
                StatsSyncPayload.TYPE,
                StatsSyncPayload.STREAM_CODEC,
                (payload, context) ->
                        StatsClientData.update(payload)
        );
    }
}
