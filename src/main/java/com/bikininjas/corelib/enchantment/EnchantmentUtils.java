package com.bikininjas.corelib.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Utilities for applying enchantments with a hard cap at level 100.
 * <p>
 * All methods are static. No event bus registration.
 */
public final class EnchantmentUtils {

    /** Hard cap for all enchantment levels applied through this API. */
    public static final int MAX_LEVEL = 100;

    private EnchantmentUtils() {
    }

    /**
     * Apply a single enchantment to an item stack, capped at {@link #MAX_LEVEL}.
     *
     * @param stack       the item stack to enchant
     * @param enchantment the enchantment holder
     * @param level       the desired level (will be capped to {@code MAX_LEVEL})
     */
    public static void applyEnchantment(
            @NotNull ItemStack stack,
            @NotNull Holder<Enchantment> enchantment,
            int level) {
        Objects.requireNonNull(stack, "stack must not be null");
        Objects.requireNonNull(enchantment, "enchantment must not be null");
        var clampedLevel = Math.min(level, MAX_LEVEL);
        stack.enchant(enchantment, clampedLevel);
    }

    /**
     * Apply multiple enchantments to an item stack.
     *
     * @param stack        the item stack to enchant
     * @param enchantments a map of enchantment holders to levels
     */
    public static void applyEnchantments(
            @NotNull ItemStack stack,
            @NotNull Map<Holder<Enchantment>, Integer> enchantments) {
        Objects.requireNonNull(stack, "stack must not be null");
        Objects.requireNonNull(enchantments, "enchantments must not be null");
        for (var entry : enchantments.entrySet()) {
            applyEnchantment(stack, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get the maximum allowed enchantment level for the given tool and enchantment.
     * Returns the normal max level, but capped at {@link #MAX_LEVEL}.
     *
     * @param enchantment the enchantment holder
     * @param stack       the item stack
     * @return the max level (≥ 0, ≤ MAX_LEVEL)
     */
    public static int getMaxLevelForTool(
            @NotNull Holder<Enchantment> enchantment,
            @NotNull ItemStack stack) {
        Objects.requireNonNull(enchantment, "enchantment must not be null");
        Objects.requireNonNull(stack, "stack must not be null");
        var vanillaMax = enchantment.value().getMaxLevel();
        return Math.min(vanillaMax, MAX_LEVEL);
    }

    /**
     * Check whether the given level is within the allowed cap.
     */
    public static boolean canEnchantAtLevel(int level) {
        return level >= 0 && level <= MAX_LEVEL;
    }

    /**
     * Check whether the given item stack has the specified enchantment.
     *
     * @param stack       the item stack to check
     * @param enchantment the enchantment holder
     * @return true if the stack has the enchantment at a positive level
     */
    public static boolean hasEnchantment(
            @NotNull ItemStack stack,
            @NotNull Holder<Enchantment> enchantment) {
        Objects.requireNonNull(stack, "stack must not be null");
        Objects.requireNonNull(enchantment, "enchantment must not be null");
        return stack.getEnchantments().getLevel(enchantment) > 0;
    }

    /**
     * Get the level of the specified enchantment on the given item stack.
     *
     * @param stack       the item stack to check
     * @param enchantment the enchantment holder
     * @return the enchantment level (0 if not present)
     */
    public static int getEnchantmentLevel(
            @NotNull ItemStack stack,
            @NotNull Holder<Enchantment> enchantment) {
        Objects.requireNonNull(stack, "stack must not be null");
        Objects.requireNonNull(enchantment, "enchantment must not be null");
        return stack.getEnchantments().getLevel(enchantment);
    }

    /**
     * Remove the specified enchantment from the item stack.
     *
     * @param stack       the item stack to modify
     * @param enchantment the enchantment holder to remove
     * @return true if the enchantment was present and removed, false otherwise
     */
    public static boolean removeEnchantment(
            @NotNull ItemStack stack,
            @NotNull Holder<Enchantment> enchantment) {
        Objects.requireNonNull(stack, "stack must not be null");
        Objects.requireNonNull(enchantment, "enchantment must not be null");
        var current = stack.getEnchantments();
        if (current.getLevel(enchantment) <= 0) {
            return false;
        }
        var mutable = new ItemEnchantments.Mutable(current);
        mutable.set(enchantment, 0);
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return true;
    }
}
