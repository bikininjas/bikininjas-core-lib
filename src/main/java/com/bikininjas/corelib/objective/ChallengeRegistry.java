package com.bikininjas.corelib.objective;

import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Static registry of available {@link ChallengeDefinition}s.
 * <p>
 * Definitions are registered once (typically during mod construction) and are
 * immutable thereafter. The registry provides query methods that filter by
 * mod availability so that challenges whose required mods are not loaded are
 * hidden from players.
 * <p>
 * This is a pure static utility. No event bus registration is needed.
 */
public final class ChallengeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeRegistry.class);

    /** Name → definition, insertion order preserved. */
    private static final Map<String, ChallengeDefinition> DEFINITIONS = new LinkedHashMap<>();

    private ChallengeRegistry() {
        // Static-only utility.
    }

    /**
     * Register a challenge definition. If a definition with the same name
     * already exists it is silently overwritten.
     *
     * @param definition the definition to register; never {@code null}.
     * @throws NullPointerException if {@code definition} is {@code null}.
     */
    public static void register(@NotNull ChallengeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        DEFINITIONS.put(definition.name(), definition);
        LOGGER.debug("Registered challenge definition '{}'", definition.name());
    }

    /**
     * Look up a definition by name.
     *
     * @param name the registry key; never {@code null}.
     * @return the definition, or {@code null} if not found.
     */
    @Nullable
    public static ChallengeDefinition get(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        return DEFINITIONS.get(name);
    }

    /**
     * @return an immutable list of all registered definitions (regardless of
     *         mod availability).
     */
    @NotNull
    public static List<ChallengeDefinition> getAll() {
        return List.copyOf(DEFINITIONS.values());
    }

    /**
     * @return an immutable list of definitions whose {@code requiredMods} are
     *         all currently loaded. Challenges with an empty required-mods list
     *         are always included.
     */
    @NotNull
    public static List<ChallengeDefinition> getAvailable() {
        return DEFINITIONS.values().stream()
                .filter(ChallengeRegistry::areModsLoaded)
                .toList();
    }

    /**
     * Remove a definition from the registry. Primarily useful for testing.
     *
     * @param name the registry key of the definition to remove.
     */
    public static void remove(@NotNull String name) {
        DEFINITIONS.remove(name);
    }

    /**
     * Clear all definitions. Primarily useful for testing.
     */
    public static void clear() {
        DEFINITIONS.clear();
    }

    /**
     * Check whether all mods required by a definition are currently loaded.
     *
     * @param definition the definition to check; never {@code null}.
     * @return {@code true} if all required mods are loaded or the list is empty.
     */
    public static boolean areModsLoaded(@NotNull ChallengeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        List<String> required = definition.requiredMods();
        if (required.isEmpty()) {
            return true;
        }
        ModList modList = ModList.get();
        if (modList == null) {
            // Not inside a Minecraft runtime (e.g. unit tests) — assume loaded.
            return true;
        }
        for (String modId : required) {
            if (!modList.isLoaded(modId)) {
                LOGGER.debug("Mod '{}' required by challenge '{}' is not loaded",
                        modId, definition.name());
                return false;
            }
        }
        return true;
    }

    /**
     * @return the total number of registered definitions.
     */
    public static int count() {
        return DEFINITIONS.size();
    }
}
