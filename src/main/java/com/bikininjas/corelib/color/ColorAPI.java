package com.bikininjas.corelib.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Simplifies NeoForge's {@link RegisterColorHandlersEvent} for tinting items and blocks.
 * <p>
 * Usage from a child mod's {@code @Mod} constructor:
 * <pre>{@code
 * ColorAPI.tintItem(modBus, ModItems.MY_ITEM, 0xFF6666);       // red tint
 * ColorAPI.tintItems(modBus, 0x6666FF, item1, item2, item3);    // blue tint on multiple
 * ColorAPI.tintItem(modBus, ModItems.CUSTOM_ITEM, stack -> {    // dynamic tint
 *     return DyeColor.byId(stack.getOrDefault(DataComponents.DYED_COLOR, 0)).getTextureDiffuseColor();
 * });
 * ColorAPI.tintBlock(modBus, ModBlocks.MY_BLOCK, (state, level, pos, tintIndex) -> 0x88FF88);
 * }</pre>
 */
public final class ColorAPI {

    private ColorAPI() {
    }

    // ================================================================
    // Item tint — single item
    // ================================================================

    /**
     * Register a constant tint for a single layer-0 item.
     *
     * @param modBus the mod event bus
     * @param item   the item to tint
     * @param color  ARGB color (e.g. {@code 0xFF6666} for red)
     */
    public static void tintItem(@NotNull IEventBus modBus, @NotNull ItemLike item, int color) {
        tintItem(modBus, item, color, 0);
    }

    /**
     * Register a constant tint for a specific layer of an item.
     *
     * @param modBus the mod event bus
     * @param item   the item to tint
     * @param color  ARGB color
     * @param layer  tint index layer (0 = main layer, 1 = overlay)
     */
    public static void tintItem(@NotNull IEventBus modBus, @NotNull ItemLike item, int color, int layer) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(item, "item must not be null");
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event ->
                event.register((stack, tintIndex) ->
                        tintIndex == layer ? color : 0xFFFFFFFF, item.asItem()));
    }

    /**
     * Register a dynamic color handler for a single item.
     *
     * @param modBus  the mod event bus
     * @param item    the item to tint
     * @param handler the {@link ItemColor} callback ({@code (stack, tintIndex) -> ARGB})
     */
    public static void tintItem(@NotNull IEventBus modBus, @NotNull ItemLike item,
                                @NotNull ItemColor handler) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(item, "item must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event ->
                event.register(handler, item.asItem()));
    }

    // ================================================================
    // Item tint — multiple items (same color)
    // ================================================================

    /**
     * Register the same constant tint for multiple items (layer 0).
     *
     * @param modBus the mod event bus
     * @param color  ARGB color
     * @param items  one or more items to tint
     */
    public static void tintItems(@NotNull IEventBus modBus, int color, @NotNull ItemLike... items) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(items, "items must not be null");
        for (var item : items) {
            tintItem(modBus, item, color);
        }
    }

    // ================================================================
    // Block tint
    // ================================================================

    /**
     * Register a dynamic color handler for a block.
     *
     * @param modBus  the mod event bus
     * @param block   the block to tint
     * @param handler the {@link BlockColor} callback
     *                ({@code (state, level, pos, tintIndex) -> ARGB})
     */
    public static void tintBlock(@NotNull IEventBus modBus, @NotNull Block block,
                                 @NotNull BlockColor handler) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(block, "block must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        modBus.addListener(RegisterColorHandlersEvent.Block.class, event ->
                event.register(handler, block));
    }

    /**
     * Register the same dynamic color handler for multiple blocks.
     *
     * @param modBus  the mod event bus
     * @param handler the {@link BlockColor} callback
     * @param blocks  one or more blocks to tint
     */
    public static void tintBlocks(@NotNull IEventBus modBus, @NotNull BlockColor handler,
                                  @NotNull Block... blocks) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(blocks, "blocks must not be null");
        for (var block : blocks) {
            tintBlock(modBus, block, handler);
        }
    }
}
