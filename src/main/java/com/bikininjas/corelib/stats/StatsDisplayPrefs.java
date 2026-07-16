package com.bikininjas.corelib.stats;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player display preferences for the stats HUD overlay.
 * <p>
 * Controls whether the overlay is visible for a player and which
 * stat fields are shown. All state is kept server-side and synced
 * to the client via {@code StatsSyncPayload}.
 */
public final class StatsDisplayPrefs {

    private static final Map<UUID, Prefs> PREFS = new ConcurrentHashMap<>();

    private static final Set<String> ALL_FIELDS = Set.of("deaths", "kills", "blocksBroken", "crafts");

    private StatsDisplayPrefs() {}

    /**
     * @param player the player to query; never {@code null}.
     * @return {@code true} if the overlay is enabled for this player.
     */
    public static boolean isEnabled(@NotNull ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return PREFS.getOrDefault(player.getUUID(), Prefs.DEFAULT).enabled;
    }

    /**
     * Toggle the overlay on/off for a player.
     *
     * @param player the player to toggle; never {@code null}.
     * @return the new enabled state.
     */
    public static boolean toggle(@NotNull ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        UUID id = player.getUUID();
        Prefs current = PREFS.getOrDefault(id, Prefs.DEFAULT);
        Prefs updated = new Prefs(!current.enabled, current.fields);
        PREFS.put(id, updated);
        return updated.enabled;
    }

    /**
     * @param player the player to query; never {@code null}.
     * @return the set of field names the player wants to see (never {@code null}).
     */
    @NotNull
    public static Set<String> getVisibleFields(@NotNull ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return PREFS.getOrDefault(player.getUUID(), Prefs.DEFAULT).fields;
    }

    /**
     * Set the visible fields for a player.
     *
     * @param player the player to update; never {@code null}.
     * @param fields the field names to show; if empty or {@code null}, all are shown.
     */
    public static void setVisibleFields(@NotNull ServerPlayer player, Set<String> fields) {
        Objects.requireNonNull(player, "player");
        UUID id = player.getUUID();
        Prefs current = PREFS.getOrDefault(id, Prefs.DEFAULT);
        Set<String> sanitised;
        if (fields == null || fields.isEmpty()) {
            sanitised = ALL_FIELDS;
        } else {
            sanitised = new HashSet<>(fields);
            sanitised.retainAll(ALL_FIELDS);
            if (sanitised.isEmpty()) {
                sanitised = ALL_FIELDS;
            }
        }
        PREFS.put(id, new Prefs(current.enabled, Set.copyOf(sanitised)));
    }

    /**
     * @return the immutable set of all valid field names.
     */
    @NotNull
    public static Set<String> getAllFields() {
        return ALL_FIELDS;
    }

    private record Prefs(boolean enabled, Set<String> fields) {
        static final Prefs DEFAULT = new Prefs(false, ALL_FIELDS);
    }
}
