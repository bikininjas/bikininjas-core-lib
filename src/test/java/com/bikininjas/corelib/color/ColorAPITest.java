package com.bikininjas.corelib.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ColorAPITest {

    private static IEventBus newBus() {
        return BusBuilder.builder().build();
    }

    private static Supplier<ItemLike> s(ItemLike item) {
        return () -> item;
    }

    private static Supplier<Block> sb(Block block) {
        return () -> block;
    }

    // ================================================================
    // Null checks
    // ================================================================

    @Nested
    class NullChecks {

        @Test
        void tintItemNullBus() {
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItem(null, s(Items.STONE), 0xFF0000));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintItemNullItem() {
            var bus = newBus();
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItem(bus, null, 0xFF0000));
        }

        @Test
        void tintItemHandlerNullBus() {
            var handler = (ItemColor) (stack, layer) -> 0xFFFFFFFF;
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItem(null, s(Items.STONE), handler));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintItemHandlerNullItem() {
            var bus = newBus();
            var handler = (ItemColor) (stack, layer) -> 0xFFFFFFFF;
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItem(bus, null, handler));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintItemHandlerNullHandler() {
            var bus = newBus();
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItem(bus, s(Items.STONE), null));
        }

        @Test
        void tintItemsNullBus() {
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItems(null, 0xFF0000, s(Items.STONE), s(Items.DIRT)));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintItemsNullItems() {
            var bus = newBus();
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintItems(bus, 0xFF0000, (Supplier<? extends ItemLike>[]) null));
        }

        @Test
        void tintBlockNullBus() {
            var handler = (BlockColor) (state, level, pos, layer) -> 0xFFFFFFFF;
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintBlock(null, sb(Blocks.STONE), handler));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintBlockNullBlock() {
            var bus = newBus();
            var handler = (BlockColor) (state, level, pos, layer) -> 0xFFFFFFFF;
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintBlock(bus, null, handler));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintBlockNullHandler() {
            var bus = newBus();
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintBlock(bus, sb(Blocks.STONE), null));
        }

        @Test
        void tintBlocksNullBus() {
            var handler = (BlockColor) (state, level, pos, layer) -> 0xFFFFFFFF;
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintBlocks(null, handler, sb(Blocks.STONE)));
        }

        @Test
        void tintBlocksNullHandler() {
            var bus = newBus();
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintBlocks(bus, null, sb(Blocks.STONE)));
        }

        @SuppressWarnings("unchecked")
        @Test
        void tintBlocksNullBlocks() {
            var bus = newBus();
            var handler = (BlockColor) (state, level, pos, layer) -> 0xFFFFFFFF;
            assertThrows(NullPointerException.class,
                    () -> ColorAPI.tintBlocks(bus, handler, (Supplier<? extends Block>[]) null));
        }
    }

    // ================================================================
    // Registration
    // ================================================================

    @Nested
    class Registration {

        @Test
        void constantColorLayer0() {
            var bus = newBus();
            assertDoesNotThrow(() ->
                    ColorAPI.tintItem(bus, s(Items.STONE), 0xFFFF0000));
        }

        @Test
        void constantColorSpecificLayer() {
            var bus = newBus();
            assertDoesNotThrow(() ->
                    ColorAPI.tintItem(bus, s(Items.APPLE), 0xFF8888FF, 1));
        }

        @Test
        void dynamicItemHandler() {
            var bus = newBus();
            ItemColor handler = (stack, layer) ->
                    layer == 0 ? 0xFFFF6666 : 0xFFFFFFFF;
            assertDoesNotThrow(() ->
                    ColorAPI.tintItem(bus, s(Items.DIAMOND), handler));
        }

        @Test
        void batchItems() {
            var bus = newBus();
            assertDoesNotThrow(() ->
                    ColorAPI.tintItems(bus, 0xFF4488FF,
                            s(Items.STONE), s(Items.DIRT), s(Items.COBBLESTONE)));
        }

        @Test
        void batchSingleItem() {
            var bus = newBus();
            assertDoesNotThrow(() ->
                    ColorAPI.tintItems(bus, 0xFF4488FF, s(Items.GOLD_INGOT)));
        }

        @Test
        void emptyBatchItems() {
            var bus = newBus();
            assertDoesNotThrow(() ->
                    ColorAPI.tintItems(bus, 0xFF4488FF));
        }

        @Test
        void singleBlock() {
            var bus = newBus();
            BlockColor handler = (state, level, pos, layer) ->
                    layer == 0 ? 0xFF88FF88 : 0xFFFFFFFF;
            assertDoesNotThrow(() ->
                    ColorAPI.tintBlock(bus, sb(Blocks.GRASS_BLOCK), handler));
        }

        @Test
        void batchBlocks() {
            var bus = newBus();
            BlockColor handler = (state, level, pos, layer) -> 0xFFAAAAFF;
            assertDoesNotThrow(() ->
                    ColorAPI.tintBlocks(bus, handler,
                            sb(Blocks.STONE), sb(Blocks.DIRT), sb(Blocks.SAND)));
        }
    }

    // ================================================================
    // Multiple registrations
    // ================================================================

    @Nested
    class MultipleRegistrations {

        @Test
        void twoColorsOnDifferentItems() {
            var bus = newBus();
            assertDoesNotThrow(() -> {
                ColorAPI.tintItem(bus, s(Items.STONE), 0xFFFF0000);
                ColorAPI.tintItem(bus, s(Items.DIRT), 0xFF00FF00);
            });
        }

        @Test
        void itemAndBlockOnSameBus() {
            var bus = newBus();
            assertDoesNotThrow(() -> {
                ColorAPI.tintItem(bus, s(Items.APPLE), 0xFFFF0000);
                ColorAPI.tintBlock(bus, sb(Blocks.GRASS_BLOCK),
                        (state, level, pos, layer) -> 0xFF00FF00);
            });
        }
    }
}
