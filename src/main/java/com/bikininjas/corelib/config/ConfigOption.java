package com.bikininjas.corelib.config;

import net.minecraft.network.chat.Component;

/**
 * A single configurable option that can be managed via the Bikini Config GUI.
 */
public record ConfigOption(
        String modId,
        String key,
        String category,
        Component displayName,
        Component description,
        OptionType type,
        Object defaultValue,
        Object currentValue,
        String[] enumValues
) {
    public enum OptionType { BOOL, INT, FLOAT, ENUM, STRING }

    public ConfigOption {
        if (type == OptionType.ENUM && (enumValues == null || enumValues.length == 0)) {
            throw new IllegalArgumentException("ENUM type requires enumValues");
        }
    }

    public ConfigOption withValue(Object newValue) {
        return new ConfigOption(modId, key, category, displayName, description,
                type, defaultValue, newValue, enumValues);
    }
}
