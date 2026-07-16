package com.bikininjas.corelib.restriction;

/**
 * Enumerates the categories of player actions that can be globally restricted
 * through the {@link RestrictionManager}.
 * <p>
 * Each value represents a distinct NeoForge event surface. A mod registers a
 * {@link net.minecraft.resources.ResourceLocation} (a block, item, entity type
 * or dimension) against one of these types to block the corresponding action
 * whenever that resource is involved.
 */
public enum RestrictionType {

    /**
     * Blocks placement of a specific block.
     * <p>
     * Matched against {@code BlockEvent.EntityPlaceEvent} using the placed
     * block's registry name.
     */
    PLACE_BLOCK,

    /**
     * Blocks breaking of a specific block.
     * <p>
     * Matched against {@code BlockEvent.BreakEvent} using the broken block's
     * registry name.
     */
    BREAK_BLOCK,

    /**
     * Blocks usage (right-click) of a specific item.
     * <p>
     * Matched against {@code PlayerInteractEvent.RightClickItem} using the held
     * item's registry name.
     */
    USE_ITEM,

    /**
     * Blocks spawning of a specific entity type.
     * <p>
     * Matched against {@code EntityJoinLevelEvent} using the entity type's
     * registry name. Player entities are never blocked by this type.
     */
    SPAWN_ENTITY,

    /**
     * Blocks travel to a specific dimension.
     * <p>
     * Matched against {@code EntityTravelToDimensionEvent} using the target
     * dimension's registry name.
     */
    ENTER_DIMENSION
}
