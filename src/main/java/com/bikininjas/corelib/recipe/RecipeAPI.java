package com.bikininjas.corelib.recipe;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmatic recipe API for NeoForge 1.21.1.
 *
 * <p>The vanilla {@link RecipeManager} exposes no public {@code addRecipe} method, so this utility
 * mutates the manager's private {@code byName} map and {@code byType} multimap through reflection
 * (with {@link Field#setAccessible(boolean)}). Recipes are then pushed to clients via
 * {@link ClientboundUpdateRecipesPacket}.</p>
 *
 * <p>No external recipe API (JEI, CraftTweaker, ...) is used and no event bus subscriber is
 * registered. Build recipes with {@link RecipeBuilder} and register them through
 * {@link #addRecipe(RecipeHolder)}.</p>
 */
public final class RecipeAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeAPI.class);

    private RecipeAPI() {
    }

    /**
     * Adds a recipe holder to the server's {@link RecipeManager} at runtime.
     * Mutates the private {@code byName} map and {@code byType} multimap via reflection.
     *
     * @param holder the recipe holder to register
     */
    public static void addRecipe(final RecipeHolder<?> holder) {
        final MinecraftServer server = currentServer();
        if (server == null) {
            LOGGER.warn("Cannot add recipe {}: no running MinecraftServer", holder.id());
            return;
        }
        final RecipeManager manager = server.getRecipeManager();
        try {
            final Map<ResourceLocation, RecipeHolder<?>> byName = getByName(manager);
            byName.put(holder.id(), holder);

            @SuppressWarnings("unchecked")
            final com.google.common.collect.Multimap<RecipeType<?>, RecipeHolder<?>> byType =
                    getByType(manager);
            byType.put(holder.value().getType(), holder);
        } catch (final ReflectiveOperationException e) {
            LOGGER.error("Failed to add recipe {} via reflection", holder.id(), e);
        }
    }

    /**
     * Removes a recipe from the server's {@link RecipeManager} by its identifier.
     * Clears the entry from the private {@code byName} map and {@code byType} multimap.
     *
     * @param id the resource location of the recipe to remove
     */
    public static void removeRecipe(final ResourceLocation id) {
        final MinecraftServer server = currentServer();
        if (server == null) {
            LOGGER.warn("Cannot remove recipe {}: no running MinecraftServer", id);
            return;
        }
        final RecipeManager manager = server.getRecipeManager();
        try {
            final Map<ResourceLocation, RecipeHolder<?>> byName = getByName(manager);
            final RecipeHolder<?> removed = byName.remove(id);

            @SuppressWarnings("unchecked")
            final com.google.common.collect.Multimap<RecipeType<?>, RecipeHolder<?>> byType =
                    getByType(manager);
            if (removed != null) {
                byType.remove(removed.value().getType(), removed);
            }
        } catch (final ReflectiveOperationException e) {
            LOGGER.error("Failed to remove recipe {} via reflection", id, e);
        }
    }

    /**
     * Synchronises the full recipe set to a single player by sending a
     * {@link ClientboundUpdateRecipesPacket}.
     *
     * @param player the player to sync
     */
    public static void syncToPlayer(final ServerPlayer player) {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            LOGGER.warn("Cannot sync recipes: player has no server");
            return;
        }
        final Collection<RecipeHolder<?>> recipes = server.getRecipeManager().getRecipes();
        player.connection.send(new ClientboundUpdateRecipesPacket(recipes));
    }

    /**
     * Synchronises the full recipe set to every connected player on the server.
     *
     * @param server the running server
     */
    public static void syncToAll(final MinecraftServer server) {
        final PlayerList players = server.getPlayerList();
        final Collection<RecipeHolder<?>> recipes = server.getRecipeManager().getRecipes();
        final ClientboundUpdateRecipesPacket packet = new ClientboundUpdateRecipesPacket(recipes);
        for (final ServerPlayer player : players.getPlayers()) {
            player.connection.send(packet);
        }
    }

    private static MinecraftServer currentServer() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, RecipeHolder<?>> getByName(final RecipeManager manager)
            throws ReflectiveOperationException {
        final Field field = RecipeManager.class.getDeclaredField("byName");
        field.setAccessible(true);
        return (Map<ResourceLocation, RecipeHolder<?>>) field.get(manager);
    }

    @SuppressWarnings("unchecked")
    private static com.google.common.collect.Multimap<RecipeType<?>, RecipeHolder<?>> getByType(
            final RecipeManager manager) throws ReflectiveOperationException {
        final Field field = RecipeManager.class.getDeclaredField("byType");
        field.setAccessible(true);
        return (com.google.common.collect.Multimap<RecipeType<?>, RecipeHolder<?>>) field.get(manager);
    }

    /**
     * Looks up a registered recipe holder by its identifier.
     *
     * @param id the resource location of the recipe
     * @return the holder if present
     */
    public static Optional<RecipeHolder<?>> getRecipe(final ResourceLocation id) {
        final MinecraftServer server = currentServer();
        if (server == null) {
            return Optional.empty();
        }
        return server.getRecipeManager().byKey(id);
    }
}
