package com.bikininjas.corelib.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory centralisée des {@link ModLogger}.
 * <p>
 * Point d'entrée unique pour obtenir un logger enrichi dans n'importe quel
 * module ou mod enfant :
 * <pre>{@code
 * private static final ModLogger LOGGER = LogManager.getLogger("my_mod", MyClass.class);
 * }</pre>
 * <p>
 * La configuration des niveaux de log se fait via Logback (logback.xml).
 * Cette classe est un utilitaire statique — elle n'est pas instanciable.
 */
public final class LogManager {

    private static final Logger INTERNAL = LoggerFactory.getLogger(LogManager.class);

    private LogManager() {
        // Static-only utility.
    }

    /**
     * Obtain (or create) a {@link ModLogger} for the given mod and class.
     * <p>
     * Loggers are lightweight objects — no caching is performed (SLF4J's
     * {@link LoggerFactory} handles its own caching).
     *
     * @param modId mod identifier (e.g. {@code "core_lib"}, {@code "super_crafting"})
     * @param clazz the class that will log
     * @return a new {@link ModLogger} (never null)
     */
    public static ModLogger getLogger(String modId, Class<?> clazz) {
        return new ModLogger(modId, clazz);
    }

    /**
     * Must be called during mod construction to register with the event bus
     * and load configuration.
     * <p>
     * Follows the {@code init()} pattern used by all core-lib modules.
     */
    public static void init() {
        INTERNAL.info("LogManager initialized");
    }
}
