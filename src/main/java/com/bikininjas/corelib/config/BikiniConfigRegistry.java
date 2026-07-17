package com.bikininjas.corelib.config;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for Bikini mod configuration options.
 * Each mod registers its options here. The GUI reads from this registry.
 */
public final class BikiniConfigRegistry {

    private static final Map<String, Map<String, ConfigOption>> options = new ConcurrentHashMap<>();
    private static final Map<String, String> modDisplayNames = new ConcurrentHashMap<>();

    private BikiniConfigRegistry() {}

    public static void registerMod(@NotNull String modId, @NotNull String displayName) {
        options.putIfAbsent(modId, new ConcurrentHashMap<>());
        modDisplayNames.putIfAbsent(modId, displayName);
    }

    public static void registerOption(@NotNull ConfigOption option) {
        options.computeIfAbsent(option.modId(), k -> new ConcurrentHashMap<>())
                .put(option.key(), option);
    }

    public static Collection<ConfigOption> getOptions(@NotNull String modId) {
        var modOptions = options.get(modId);
        return modOptions != null ? modOptions.values() : Collections.emptyList();
    }

    public static Set<String> getRegisteredMods() {
        return Collections.unmodifiableSet(options.keySet());
    }

    public static String getModDisplayName(@NotNull String modId) {
        return modDisplayNames.getOrDefault(modId, modId);
    }

    public static void updateValue(@NotNull String modId, @NotNull String key, @NotNull Object value) {
        var modOptions = options.get(modId);
        if (modOptions != null) {
            var option = modOptions.get(key);
            if (option != null) {
                modOptions.put(key, option.withValue(value));
            }
        }
    }

    public static void clear() {
        options.clear();
        modDisplayNames.clear();
    }
}
