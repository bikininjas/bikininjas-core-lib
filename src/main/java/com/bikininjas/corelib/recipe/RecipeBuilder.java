package com.bikininjas.corelib.recipe;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/**
 * Fluent builder for programmatic Minecraft recipes (shaped, shapeless and smelting)
 * targeting NeoForge 1.21.1.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * Optional<RecipeHolder<?>> holder = new RecipeBuilder()
 *     .id("mymod", "super_pickaxe")
 *     .shaped(new ItemStack(Items.DIAMOND_PICKAXE), "XX", "X#", " #")
 *     .defined('X', Ingredient.of(Items.DIAMOND))
 *     .defined('#', Ingredient.of(Items.STICK))
 *     .build();
 * }</pre>
 *
 * <p>All recipe objects are built using the vanilla NeoForge 1.21.1 constructors and wrapped
 * in a {@link RecipeHolder}. No external recipe API (JEI, CraftTweaker, ...) is used.</p>
 */
public final class RecipeBuilder {

    private ResourceLocation id;
    private ItemStack result;
    private RecipeKind kind;
    private String[] pattern;
    private ItemStack[] shapelessIngredients;
    private ItemStack smeltIngredient;
    private float experience;
    private int cookingTime;
    private final Map<Character, Ingredient> ingredientMap = new HashMap<>();

    private enum RecipeKind {
        SHAPED, SHAPELESS, SMELTING
    }

    private RecipeBuilder() {
    }

    /**
     * Creates a new {@link RecipeBuilder} instance.
     *
     * @return a fresh builder
     */
    public static RecipeBuilder create() {
        return new RecipeBuilder();
    }

    /**
     * Sets the {@link ResourceLocation} identifier of the recipe.
     *
     * @param namespace the resource namespace (e.g. {@code "mymod"})
     * @param path      the resource path (e.g. {@code "super_pickaxe"})
     * @return this builder, for chaining
     */
    public RecipeBuilder id(final String namespace, final String path) {
        this.id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        return this;
    }

    /**
     * Declares a shaped recipe with the given result and pattern rows.
     * Pattern characters must be mapped via {@link #defined(char, Ingredient)}.
     *
     * @param result  the crafted item stack
     * @param pattern the pattern rows (single chars per slot, space = empty)
     * @return this builder, for chaining
     */
    public RecipeBuilder shaped(final ItemStack result, final String... pattern) {
        this.kind = RecipeKind.SHAPED;
        this.result = result;
        this.pattern = pattern;
        return this;
    }

    /**
     * Declares a shapeless recipe with the given result and ingredients.
     *
     * @param result      the crafted item stack
     * @param ingredients the input item stacks (order independent)
     * @return this builder, for chaining
     */
    public RecipeBuilder shapeless(final ItemStack result, final ItemStack... ingredients) {
        this.kind = RecipeKind.SHAPELESS;
        this.result = result;
        this.shapelessIngredients = ingredients;
        return this;
    }

    /**
     * Declares a smelting (furnace) recipe.
     *
     * @param result      the smelted item stack
     * @param ingredient  the input item stack
     * @param experience  xp granted per smelt
     * @param cookingTime ticks required to smelt
     * @return this builder, for chaining
     */
    public RecipeBuilder smelting(final ItemStack result, final ItemStack ingredient,
                                  final float experience, final int cookingTime) {
        this.kind = RecipeKind.SMELTING;
        this.result = result;
        this.smeltIngredient = ingredient;
        this.experience = experience;
        this.cookingTime = cookingTime;
        return this;
    }

    /**
     * Maps a pattern character to an {@link Ingredient} for shaped recipes.
     *
     * @param key        the single pattern character (e.g. {@code 'X'})
     * @param ingredient the ingredient produced from {@link Ingredient#of(ItemStack)}
     * @return this builder, for chaining
     */
    public RecipeBuilder defined(final char key, final Ingredient ingredient) {
        this.ingredientMap.put(key, ingredient);
        return this;
    }

    /**
     * Builds the configured recipe and wraps it in an {@link Optional} {@link RecipeHolder}.
     * Returns {@link Optional#empty()} when the identifier or recipe kind was not set.
     *
     * @return the holder containing the built recipe, or empty if misconfigured
     */
    public Optional<RecipeHolder<?>> build() {
        if (this.id == null || this.kind == null || this.result == null) {
            return Optional.empty();
        }
        return switch (this.kind) {
            case SHAPED -> Optional.of(new RecipeHolder<>(this.id, buildShaped()));
            case SHAPELESS -> Optional.of(new RecipeHolder<>(this.id, buildShapeless()));
            case SMELTING -> Optional.of(new RecipeHolder<>(this.id, buildSmelting()));
        };
    }

    private ShapedRecipe buildShaped() {
        final ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(
                ImmutableMap.copyOf(this.ingredientMap), this.pattern);
        return new ShapedRecipe("", CraftingBookCategory.MISC, shapedPattern, this.result, true);
    }

    private ShapelessRecipe buildShapeless() {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        for (final ItemStack stack : this.shapelessIngredients) {
            ingredients.add(Ingredient.of(stack));
        }
        return new ShapelessRecipe("", CraftingBookCategory.MISC, this.result, ingredients);
    }

    private SmeltingRecipe buildSmelting() {
        return new SmeltingRecipe("", CookingBookCategory.MISC,
                Ingredient.of(this.smeltIngredient), this.result, this.experience, this.cookingTime);
    }
}
