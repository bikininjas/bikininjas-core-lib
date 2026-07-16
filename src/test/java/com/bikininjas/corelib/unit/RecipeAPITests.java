package com.bikininjas.corelib.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bikininjas.corelib.recipe.RecipeAPI;
import com.bikininjas.corelib.recipe.RecipeBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.junit.jupiter.api.Test;

/**
 * Pure structural and construction tests for the programmatic recipe API.
 * No Minecraft server is required; only recipe object construction is exercised.
 */
class RecipeAPITests {

    @Test
    void recipeBuilderIsFinalWithPrivateConstructor() {
        assertTrue(Modifier.isFinal(RecipeBuilder.class.getModifiers()));
        final Constructor<?>[] constructors = RecipeBuilder.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void recipeApiIsFinalWithPrivateConstructor() {
        assertTrue(Modifier.isFinal(RecipeAPI.class.getModifiers()));
        final Constructor<?>[] constructors = RecipeAPI.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void shapedRecipeBuildsValidHolder() {
        final Optional<RecipeHolder<?>> holder = RecipeBuilder.create()
                .id("mymod", "test_shaped")
                .shaped(new ItemStack(Items.DIAMOND_PICKAXE), "XX", "X#", " #")
                .defined('X', Ingredient.of(new ItemStack(Items.DIAMOND)))
                .defined('#', Ingredient.of(new ItemStack(Items.STICK)))
                .build();

        assertTrue(holder.isPresent());
        final RecipeHolder<?> recipeHolder = holder.get();
        assertEquals(ResourceLocation.fromNamespaceAndPath("mymod", "test_shaped"), recipeHolder.id());
        assertTrue(recipeHolder.value() instanceof ShapedRecipe);
    }

    @Test
    void shapelessRecipeBuildsValidHolder() {
        final Optional<RecipeHolder<?>> holder = RecipeBuilder.create()
                .id("mymod", "test_shapeless")
                .shapeless(new ItemStack(Items.DIAMOND), new ItemStack(Items.DIRT), new ItemStack(Items.COBBLESTONE))
                .build();

        assertTrue(holder.isPresent());
        final RecipeHolder<?> recipeHolder = holder.get();
        assertEquals(ResourceLocation.fromNamespaceAndPath("mymod", "test_shapeless"), recipeHolder.id());
        assertTrue(recipeHolder.value() instanceof ShapelessRecipe);
    }

    @Test
    void smeltingRecipeBuildsValidHolder() {
        final Optional<RecipeHolder<?>> holder = RecipeBuilder.create()
                .id("mymod", "test_smelting")
                .smelting(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.RAW_IRON), 0.7f, 200)
                .build();

        assertTrue(holder.isPresent());
        final RecipeHolder<?> recipeHolder = holder.get();
        assertEquals(ResourceLocation.fromNamespaceAndPath("mymod", "test_smelting"), recipeHolder.id());
        assertTrue(recipeHolder.value() instanceof SmeltingRecipe);
    }

    @Test
    void buildWithoutIdReturnsEmpty() {
        final Optional<RecipeHolder<?>> holder = RecipeBuilder.create()
                .shaped(new ItemStack(Items.STICK), "X")
                .defined('X', Ingredient.of(new ItemStack(Items.OAK_PLANKS)))
                .build();

        assertNotNull(holder);
        assertFalse(holder.isPresent());
    }
}
