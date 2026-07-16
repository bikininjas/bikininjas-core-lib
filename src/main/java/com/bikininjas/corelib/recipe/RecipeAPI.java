package com.bikininjas.corelib.recipe;

import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API for programmatically adding and removing recipes at runtime.
 * <p>
 * Recipes are stored in an internal registry and applied when the server starts.
 * Changes can be synchronized to players.
 * <p>
 * All methods are static. No event bus registration (relies on explicit calls).
 */
public final class RecipeAPI {

    private static final Map<String, RecipeHolder<?>> pendingAdditions = new ConcurrentHashMap<>();
    private static final java.util.Set<String> pendingRemovals = ConcurrentHashMap.newKeySet();

    static {
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> {
            applyPending(event.getServer());
        });
    }

    private RecipeAPI() {
    }

    /**
     * Add a recipe. The recipe ID must be unique (e.g. {@code "my_mod:custom_sword"}).
     */
    public static void addRecipe(@NotNull String id, @NotNull RecipeHolder<?> holder) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(holder, "holder must not be null");
        pendingAdditions.put(id, holder);
        pendingRemovals.remove(id);
    }

    /**
     * Remove a recipe by its ID.
     */
    public static void removeRecipe(@NotNull String id) {
        Objects.requireNonNull(id, "id must not be null");
        pendingRemovals.add(id);
        pendingAdditions.remove(id);
    }

    /**
     * Sync the server's current recipe list to a single player.
     */
    public static void syncToPlayer(@NotNull ServerPlayer player) {
        Objects.requireNonNull(player, "player must not be null");
        player.connection.send(new ClientboundUpdateRecipesPacket(
                java.util.List.copyOf(player.server.getRecipeManager().getRecipes())));
    }

    /**
     * Sync the current modified recipe set to all players on the server.
     */
    public static void syncToAll(@NotNull MinecraftServer server) {
        Objects.requireNonNull(server, "server must not be null");
        for (var player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    // -- Internal ------------------------------------------------------------

    private static void applyPending(MinecraftServer server) {
        var manager = server.getRecipeManager();
        if (pendingAdditions.isEmpty() && pendingRemovals.isEmpty()) {
            return;
        }

        var recipeMap = new java.util.LinkedHashMap<net.minecraft.resources.ResourceLocation, RecipeHolder<?>>();
        var byKey = com.google.common.collect.MultimapBuilder.hashKeys().arrayListValues()
                .<net.minecraft.world.item.crafting.RecipeType<?>, RecipeHolder<?>>build();

        for (var holder : manager.getRecipes()) {
            var loc = holder.id();
            if (!pendingRemovals.contains(loc.toString())) {
                recipeMap.put(loc, holder);
                byKey.put(holder.value().getType(), holder);
            }
        }

        for (var entry : pendingAdditions.entrySet()) {
            var loc = net.minecraft.resources.ResourceLocation.parse(entry.getKey());
            var holder = entry.getValue();
            recipeMap.put(loc, holder);
            byKey.put(holder.value().getType(), holder);
        }

        try {
            var managerClass = net.minecraft.world.item.crafting.RecipeManager.class;
            var recipesField = managerClass.getDeclaredField("recipes");
            recipesField.setAccessible(true);
            recipesField.set(manager, recipeMap);

            var byNameField = managerClass.getDeclaredField("byName");
            byNameField.setAccessible(true);
            byNameField.set(manager, recipeMap);
        } catch (java.lang.ReflectiveOperationException e) {
            com.bikininjas.corelib.log.LogManager.getLogger("core_lib", RecipeAPI.class)
                    .error("Failed to apply pending recipes")
                    .cause(e)
                    .report();
        }
    }
}
