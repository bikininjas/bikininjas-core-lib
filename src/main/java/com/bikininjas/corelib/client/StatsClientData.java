package com.bikininjas.corelib.client;

import com.bikininjas.corelib.network.StatsSyncPayload;

import java.util.Set;

/**
 * Client-side singleton cache of the latest synced stats.
 * <p>
 * Updated by {@link StatsSyncPayload} via the network handler.
 * All fields are {@code volatile} so the render thread always
 * sees the latest value written by the Netty thread.
 */
public final class StatsClientData {

    private static volatile boolean visible = false;
    private static volatile Set<String> visibleFields = Set.of();
    private static volatile int deaths = 0;
    private static volatile int kills = 0;
    private static volatile int blocksBroken = 0;
    private static volatile int crafts = 0;

    private StatsClientData() {}

    /**
     * Atomically replace all cached state with the values from a payload.
     */
    public static void update(StatsSyncPayload payload) {
        visible = payload.visible();
        visibleFields = payload.fields();
        deaths = payload.deaths();
        kills = payload.kills();
        blocksBroken = payload.blocksBroken();
        crafts = payload.crafts();
    }

    // ──────────────────────────────────────────────
    //  Queries
    // ──────────────────────────────────────────────

    public static boolean isVisible() {
        return visible;
    }

    public static Set<String> getVisibleFields() {
        return visibleFields;
    }

    public static int getDeaths() {
        return deaths;
    }

    public static int getKills() {
        return kills;
    }

    public static int getBlocksBroken() {
        return blocksBroken;
    }

    public static int getCrafts() {
        return crafts;
    }
}
