package com.bikininjas.corelib.stats;

/**
 * Immutable snapshot of a player's lifetime statistics.
 * <p>
 * This record is the value type stored and mutated by {@link PlayerStatsManager}.
 * Because it is immutable, every update produces a brand-new instance, which keeps
 * the manager's {@link java.util.concurrent.ConcurrentHashMap} reads lock-free and
 * thread-safe.
 *
 * @param deaths       number of times the player has died
 * @param kills        number of entities the player has killed
 * @param blocksBroken number of blocks the player has broken
 * @param crafts       number of items the player has crafted
 */
public record PlayerStats(int deaths, int kills, int blocksBroken, int crafts) {

    /**
     * The canonical zeroed-out stats instance, returned for players that have no
     * recorded activity yet. Safe to share because records are immutable.
     */
    public static final PlayerStats EMPTY = new PlayerStats(0, 0, 0, 0);
}
