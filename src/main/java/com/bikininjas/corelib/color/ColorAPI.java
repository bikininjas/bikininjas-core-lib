package com.bikininjas.corelib.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Simplifies NeoForge's {@link RegisterColorHandlersEvent} for tinting items and blocks.
 * <p>
 * Uses {@link Supplier} for item/block references so {@code DeferredItem}/{@code DeferredBlock}
 * can be passed directly from a {@code @Mod} constructor without calling {@code .get()}
 * (which would fail because the registry hasn't fired yet).
 * <pre>{@code
 * ColorAPI.tintItem(modBus, ModItems.MY_ITEM, 0xFF6666);       // DeferredItem works!
 * ColorAPI.tintItems(modBus, 0x6666FF, item1, item2, item3);
 * ColorAPI.tintBlock(modBus, ModBlocks.MY_BLOCK, handler);
 * }</pre>
 */
public final class ColorAPI {

    private ColorAPI() {
    }

    // ================================================================
    // Item tint — single item
    // ================================================================

    public static void tintItem(@NotNull IEventBus modBus,
                                @NotNull Supplier<? extends ItemLike> item,
                                int color) {
        tintItem(modBus, item, color, 0);
    }

    public static void tintItem(@NotNull IEventBus modBus,
                                @NotNull Supplier<? extends ItemLike> item,
                                int color, int layer) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(item, "item must not be null");
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event ->
                event.register((stack, tintIndex) ->
                        tintIndex == layer ? color : 0xFFFFFFFF, item.get().asItem()));
    }

    public static void tintItem(@NotNull IEventBus modBus,
                                @NotNull Supplier<? extends ItemLike> item,
                                @NotNull ItemColor handler) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(item, "item must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event ->
                event.register(handler, item.get().asItem()));
    }

    // ================================================================
    // Item tint — multiple items (same color)
    // ================================================================

    @SafeVarargs
    public static void tintItems(@NotNull IEventBus modBus, int color,
                                  @NotNull Supplier<? extends ItemLike>... items) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(items, "items must not be null");
        for (var item : items) {
            tintItem(modBus, item, color);
        }
    }

    // ================================================================
    // Block tint
    // ================================================================

    public static void tintBlock(@NotNull IEventBus modBus,
                                  @NotNull Supplier<? extends Block> block,
                                  @NotNull BlockColor handler) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(block, "block must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        modBus.addListener(RegisterColorHandlersEvent.Block.class, event ->
                event.register(handler, block.get()));
    }

    @SafeVarargs
    public static void tintBlocks(@NotNull IEventBus modBus,
                                   @NotNull BlockColor handler,
                                   @NotNull Supplier<? extends Block>... blocks) {
        Objects.requireNonNull(modBus, "modBus must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(blocks, "blocks must not be null");
        for (var block : blocks) {
            tintBlock(modBus, block, handler);
        }
    }
}
