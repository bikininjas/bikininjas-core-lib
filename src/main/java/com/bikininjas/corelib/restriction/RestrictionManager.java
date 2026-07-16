package com.bikininjas.corelib.restriction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global, resource-based restriction API for NeoForge mods.
 * <p>
 * Mods register a {@link ResourceLocation} (a block, item, entity type or
 * dimension) against a {@link RestrictionType} to block the corresponding
 * player action whenever that resource is involved. Restrictions are global
 * (not player-specific) and are enforced by a static event handler subscribed
 * to the NeoForge event bus.
 * <p>
 * The registry is thread-safe ({@link ConcurrentHashMap}) and the public
 * accessor returns an unmodifiable view, so callers cannot mutate the internal
 * state.
 * <p>
 * Example usage:
 * <pre>{@code
 * RestrictionManager.register(
 *     ResourceLocation.fromNamespaceAndPath("mymod", "forbidden_block"),
 *     RestrictionType.PLACE_BLOCK);
 * }</pre>
 */
public final class RestrictionManager {

    private RestrictionManager() {}

    /**
     * Registry of restricted resources keyed by their {@link ResourceLocation}.
     * The value is the {@link RestrictionType} that the resource is restricted for.
     */
    private static final Map<ResourceLocation, RestrictionType> RESTRICTIONS =
            new ConcurrentHashMap<>();

    static {
        // Register the restriction enforcer once on the NeoForge event bus.
        NeoForge.EVENT_BUS.register(RestrictionHandler.class);
    }

    /**
     * Force-load this class to trigger the static initialiser.
     * Idempotent — safe to call multiple times.
     */
    public static void init() {
        // static block does the work
    }

    // ──────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────

    /**
     * Register a resource as restricted for the given type.
     *
     * @param id   the registry name of the resource to restrict (block, item,
     *             entity type or dimension)
     * @param type the category of action to block for that resource
     * @return {@code true} if the restriction was added, {@code false} if the
     *         resource was already registered (with any type)
     */
    public static boolean register(ResourceLocation id, RestrictionType type) {
        return RESTRICTIONS.putIfAbsent(id, type) == null;
    }

    /**
     * Remove a previously registered restriction.
     *
     * @param id the registry name of the resource to unregister
     * @return {@code true} if a restriction was removed, {@code false} if no
     *         restriction existed for that resource
     */
    public static boolean unregister(ResourceLocation id) {
        return RESTRICTIONS.remove(id) != null;
    }

    /**
     * Check whether a specific resource is restricted for a specific type.
     *
     * @param id   the registry name of the resource to query
     * @param type the category of action to check
     * @return {@code true} if the resource is registered and its restriction
     *         type matches the given type
     */
    public static boolean isRestricted(ResourceLocation id, RestrictionType type) {
        return RESTRICTIONS.get(id) == type;
    }

    /**
     * Remove all registered restrictions.
     */
    public static void clear() {
        RESTRICTIONS.clear();
    }

    /**
     * Return an unmodifiable snapshot of all registered restrictions.
     *
     * @return an immutable copy of the resource-to-type mapping
     */
    public static Map<ResourceLocation, RestrictionType> getAll() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(RESTRICTIONS));
    }

    // ──────────────────────────────────────────────
    //  Event handler
    // ──────────────────────────────────────────────

    /**
     * Static event handler that enforces restrictions on the NeoForge event bus.
     * Registered once in the static initializer. Every handler first verifies
     * that the event originates on a {@link ServerLevel} before cancelling, so
     * client-side or logical-side mismatches never trigger a cancellation.
     */
    private static final class RestrictionHandler {

        private RestrictionHandler() {}

        /**
         * Cancel block placement when the placed block's registry name matches a
         * {@link RestrictionType#PLACE_BLOCK} restriction.
         */
        @SubscribeEvent
        public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
            if (!(event.getLevel() instanceof ServerLevel)) {
                return;
            }
            BlockState placed = event.getPlacedBlock();
            ResourceLocation id = getBlockId(placed.getBlock());
            if (id != null && isRestricted(id, RestrictionType.PLACE_BLOCK)) {
                event.setCanceled(true);
            }
        }

        /**
         * Cancel block breaking when the broken block's registry name matches a
         * {@link RestrictionType#BREAK_BLOCK} restriction.
         */
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            if (!(event.getLevel() instanceof ServerLevel)) {
                return;
            }
            BlockState state = event.getState();
            ResourceLocation id = getBlockId(state.getBlock());
            if (id != null && isRestricted(id, RestrictionType.BREAK_BLOCK)) {
                event.setCanceled(true);
            }
        }

        /**
         * Cancel item usage (right-click) when the held item's registry name
         * matches a {@link RestrictionType#USE_ITEM} restriction. Only applies to
         * server-side players.
         */
        @SubscribeEvent
        public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
            if (!(event.getLevel() instanceof ServerLevel)) {
                return;
            }
            if (!(event.getEntity() instanceof Player)) {
                return;
            }
            ItemStack stack = event.getItemStack();
            ResourceLocation id = getItemId(stack.getItem());
            if (id != null && isRestricted(id, RestrictionType.USE_ITEM)) {
                event.setCanceled(true);
            }
        }

        /**
         * Cancel entity spawning when the entity type's registry name matches a
         * {@link RestrictionType#SPAWN_ENTITY} restriction. Player entities are
         * never blocked by this restriction.
         */
        @SubscribeEvent
        public static void onEntityJoin(EntityJoinLevelEvent event) {
            if (!(event.getLevel() instanceof ServerLevel)) {
                return;
            }
            Entity entity = event.getEntity();
            if (entity instanceof Player) {
                return;
            }
            ResourceLocation id = getEntityTypeId(entity.getType());
            if (id != null && isRestricted(id, RestrictionType.SPAWN_ENTITY)) {
                event.setCanceled(true);
            }
        }

        /**
         * Cancel dimension travel when the target dimension's registry name
         * matches an {@link RestrictionType#ENTER_DIMENSION} restriction.
         */
        @SubscribeEvent
        public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
            Level level = event.getEntity().level();
            if (!(level instanceof ServerLevel)) {
                return;
            }
            ResourceLocation id = event.getDimension().location();
            if (id != null && isRestricted(id, RestrictionType.ENTER_DIMENSION)) {
                event.setCanceled(true);
            }
        }

        // ── Registry-name helpers ──

        /**
         * Resolve a block's registry name via {@link BuiltInRegistries}.
         */
        private static ResourceLocation getBlockId(Block block) {
            return BuiltInRegistries.BLOCK.getKey(block);
        }

        /**
         * Resolve an item's registry name via {@link BuiltInRegistries}.
         */
        private static ResourceLocation getItemId(Item item) {
            return BuiltInRegistries.ITEM.getKey(item);
        }

        /**
         * Resolve an entity type's registry name via {@link BuiltInRegistries}.
         */
        private static ResourceLocation getEntityTypeId(EntityType<?> type) {
            return BuiltInRegistries.ENTITY_TYPE.getKey(type);
        }
    }
}
