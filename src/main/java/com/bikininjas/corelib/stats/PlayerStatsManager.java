package com.bikininjas.corelib.stats;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event-driven, per-player statistics manager.
 * <p>
 * This is a stateless utility: there is no singleton. All state lives in a single
 * {@link ConcurrentHashMap} keyed by player {@link UUID}. The manager is wired to
 * the NeoForge event bus through the nested {@link StatsHandler} class, registered
 * once via the static initialiser block.
 * <p>
 * Stats are accumulated atomically via {@link Map#compute(Object, java.util.function.BiFunction)}
 * so concurrent event delivery (e.g. a death and a craft on the same tick) cannot
 * race. Unknown players transparently start from {@link PlayerStats#EMPTY}.
 */
public final class PlayerStatsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerStatsManager.class);

    /** Player UUID → accumulated stats. Concurrent for safe multi-thread access. */
    private static final Map<UUID, PlayerStats> STATS = new ConcurrentHashMap<>();

    /** Utility class — never instantiated. */
    private PlayerStatsManager() {
        // Static-only utility. No instances.
    }

    /**
     * Force-load this class (triggers {@code static} block).
     * Idempotent — safe to call multiple times.
     */
    public static void init() {
        // static block does the work
    }

    static {
        // Register the stats collector on the NeoForge event bus.
        NeoForge.EVENT_BUS.register(StatsHandler.class);
    }

    // ──────────────────────────────────────────────
    //  Queries
    // ──────────────────────────────────────────────

    /**
     * Get the current accumulated stats for a player.
     * <p>
     * Players with no recorded activity return {@link PlayerStats#EMPTY} rather than
     * {@code null}, so callers can read fields without a null check.
     *
     * @param player the target player (non-null)
     * @return the player's stats, or {@link PlayerStats#EMPTY} if none recorded
     * @throws NullPointerException if {@code player} is {@code null}
     */
    public static PlayerStats getStats(ServerPlayer player) {
        Objects.requireNonNull(player, "player must not be null");
        return STATS.getOrDefault(player.getUUID(), PlayerStats.EMPTY);
    }

    /**
     * Reset every statistic for a player back to zero.
     * <p>
     * The player's entry is removed entirely; the next read transparently falls back
     * to {@link PlayerStats#EMPTY}.
     *
     * @param player the target player (non-null)
     * @throws NullPointerException if {@code player} is {@code null}
     */
    public static void resetStats(ServerPlayer player) {
        Objects.requireNonNull(player, "player must not be null");
        STATS.remove(player.getUUID());
        LOGGER.debug("Reset stats for player {}", player.getUUID());
    }

    /**
     * @param player the target player (non-null)
     * @return the number of times the player has died
     * @throws NullPointerException if {@code player} is {@code null}
     */
    public static int getDeaths(ServerPlayer player) {
        return getStats(player).deaths();
    }

    /**
     * @param player the target player (non-null)
     * @return the number of entities the player has killed
     * @throws NullPointerException if {@code player} is {@code null}
     */
    public static int getKills(ServerPlayer player) {
        return getStats(player).kills();
    }

    /**
     * @param player the target player (non-null)
     * @return the number of blocks the player has broken
     * @throws NullPointerException if {@code player} is {@code null}
     */
    public static int getBlocksBroken(ServerPlayer player) {
        return getStats(player).blocksBroken();
    }

    /**
     * @param player the target player (non-null)
     * @return the number of items the player has crafted
     * @throws NullPointerException if {@code player} is {@code null}
     */
    public static int getCrafts(ServerPlayer player) {
        return getStats(player).crafts();
    }

    // ──────────────────────────────────────────────
    //  Event handler
    // ──────────────────────────────────────────────

    /**
     * Static event handler that collects per-player statistics. Registered once on
     * the NeoForge event bus. All updates go through {@link #STATS} with atomic
     * {@code compute} calls so concurrent events cannot corrupt the counters.
     */
    private static final class StatsHandler {

        private StatsHandler() {
            // Static-only handler. No instances.
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity victim = event.getEntity();
            UUID victimId = victim.getUUID();

            // Victim died → increment their death count.
            if (victim instanceof ServerPlayer) {
                STATS.compute(victimId, (k, v) -> {
                    PlayerStats cur = (v == null) ? PlayerStats.EMPTY : v;
                    return new PlayerStats(cur.deaths() + 1, cur.kills(),
                            cur.blocksBroken(), cur.crafts());
                });
            }

            // Killer credited → increment their kill count.
            if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                STATS.compute(killer.getUUID(), (k, v) -> {
                    PlayerStats cur = (v == null) ? PlayerStats.EMPTY : v;
                    return new PlayerStats(cur.deaths(), cur.kills() + 1,
                            cur.blocksBroken(), cur.crafts());
                });
            }
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            BlockState broken = event.getState();
            STATS.compute(serverPlayer.getUUID(), (k, v) -> {
                PlayerStats cur = (v == null) ? PlayerStats.EMPTY : v;
                return new PlayerStats(cur.deaths(), cur.kills(),
                        cur.blocksBroken() + 1, cur.crafts());
            });
            LOGGER.debug("Player {} broke block {}", serverPlayer.getUUID(), broken.getBlock());
        }

        @SubscribeEvent
        public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            STATS.compute(serverPlayer.getUUID(), (k, v) -> {
                PlayerStats cur = (v == null) ? PlayerStats.EMPTY : v;
                return new PlayerStats(cur.deaths(), cur.kills(),
                        cur.blocksBroken(), cur.crafts() + 1);
            });
            LOGGER.debug("Player {} crafted {}", serverPlayer.getUUID(), event.getCrafting().getDisplayName());
        }
    }
}
