package com.bikininjas.corelib.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Logger enrichi pour l'écosystème BikiniNinjas.
 * <p>
 * Ajoute automatiquement le modId et le nom de la classe à chaque message.
 * Fournit un {@link ErrorBuilder} pour les erreurs structurées avec contexte
 * clé/valeur, cause, et override du modId.
 * <p>
 * Usage :
 * <pre>{@code
 * private static final ModLogger LOGGER = LogManager.getLogger("my_mod", MyClass.class);
 *
 * // Simple
 * LOGGER.info("Player {} joined", playerName);
 *
 * // Erreur parlante
 * LOGGER.error("Failed to register recipe")
 *     .ctx("recipe", recipeId)
 *     .ctx("input", inputItem)
 *     .cause(exception)
 *     .report();
 * }</pre>
 */
public final class ModLogger {

    private final String modId;
    private final Class<?> clazz;
    private final Logger slf4j;

    /**
     * Package-private — use {@link LogManager#getLogger(String, Class)}.
     */
    ModLogger(String modId, Class<?> clazz) {
        this.modId = Objects.requireNonNull(modId, "modId must not be null");
        this.clazz = Objects.requireNonNull(clazz, "clazz must not be null");
        this.slf4j = LoggerFactory.getLogger(clazz);
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    /** The mod identifier attached to every log line. */
    public String modId() {
        return modId;
    }

    /** Simple class name of the logger's origin. */
    public String className() {
        return clazz.getSimpleName();
    }

    // ──────────────────────────────────────────────
    //  Level methods — info / debug / warn / trace
    // ──────────────────────────────────────────────

    /**
     * Log a formatted info message with auto-prefix {@code [modId][ClassName]}.
     *
     * @param msg  format string with {@code {}} placeholders
     * @param args arguments for placeholders
     */
    public void info(String msg, Object... args) {
        if (slf4j.isInfoEnabled()) {
            slf4j.info("[{}][{}] {}", modId, clazz.getSimpleName(),
                    MessageFormatter.arrayFormat(msg, args).getMessage());
        }
    }

    /**
     * Log a formatted debug message with auto-prefix.
     *
     * @param msg  format string with {@code {}} placeholders
     * @param args arguments for placeholders
     */
    public void debug(String msg, Object... args) {
        if (slf4j.isDebugEnabled()) {
            slf4j.debug("[{}][{}] {}", modId, clazz.getSimpleName(),
                    MessageFormatter.arrayFormat(msg, args).getMessage());
        }
    }

    /**
     * Log a formatted warning message with auto-prefix.
     *
     * @param msg  format string with {@code {}} placeholders
     * @param args arguments for placeholders
     */
    public void warn(String msg, Object... args) {
        if (slf4j.isWarnEnabled()) {
            slf4j.warn("[{}][{}] {}", modId, clazz.getSimpleName(),
                    MessageFormatter.arrayFormat(msg, args).getMessage());
        }
    }

    /**
     * Log a formatted trace message with auto-prefix.
     *
     * @param msg  format string with {@code {}} placeholders
     * @param args arguments for placeholders
     */
    public void trace(String msg, Object... args) {
        if (slf4j.isTraceEnabled()) {
            slf4j.trace("[{}][{}] {}", modId, clazz.getSimpleName(),
                    MessageFormatter.arrayFormat(msg, args).getMessage());
        }
    }

    /**
     * Start a rich error report.
     * <p>
     * Returns an {@link ErrorBuilder} that lets you attach contextual entries
     * ({@link ErrorBuilder#ctx(String, Object)}), a {@link Throwable} cause
     * ({@link ErrorBuilder#cause(Throwable)}), and an optional modId override
     * ({@link ErrorBuilder#mod(String)}) before emitting via
     * {@link ErrorBuilder#report()}.
     * <p>
     * If the error level is disabled, the returned builder is a no-op
     * (all methods are cheap and {@code report()} produces no output).
     *
     * @param msg  format string with {@code {}} placeholders
     * @param args arguments for placeholders
     * @return an {@link ErrorBuilder} (never null)
     */
    public ErrorBuilder error(String msg, Object... args) {
        if (!slf4j.isErrorEnabled()) {
            return ErrorBuilder.NOOP;
        }
        return new ErrorBuilder(this, MessageFormatter.arrayFormat(msg, args).getMessage());
    }

    // ──────────────────────────────────────────────
    //  ErrorBuilder — fluent error context
    // ──────────────────────────────────────────────

    /**
     * Fluent builder for structured error reports with contextual data.
     * <p>
     * Usage:
     * <pre>{@code
     * LOGGER.error("Failed to register recipe")
     *     .ctx("recipeId", "sword_t3")
     *     .ctx("input", "iron_sword")
     *     .cause(registryException)
     *     .report();
     * }</pre>
     *
     * Output format:
     * <pre>
     * [ERROR] [modId][ClassName] Failed to register recipe
     *   recipeId: sword_t3
     *   input: iron_sword
     *   Cause: com.example.RegistryException: registry is null
     *     at ...
     * </pre>
     */
    public static final class ErrorBuilder {

        /** No-op sentinel returned when error level is disabled. */
        private static final ErrorBuilder NOOP = new ErrorBuilder();

        private final ModLogger logger;
        private final String message;
        private final Map<String, Object> context;
        private Throwable cause;
        private String modIdOverride;

        /** Active builder constructor. */
        private ErrorBuilder(ModLogger logger, String message) {
            this.logger = Objects.requireNonNull(logger, "logger must not be null");
            this.message = Objects.requireNonNull(message, "message must not be null");
            this.context = new LinkedHashMap<>();
        }

        /** No-op sentinel constructor — logger is null, all methods short-circuit. */
        private ErrorBuilder() {
            this.logger = null;
            this.message = "";
            this.context = null;
        }

        /**
         * Add a contextual key-value pair to the error report.
         *
         * @param key   context key (short, descriptive)
         * @param value context value (toString() will be called at report time)
         * @return {@code this} for chaining
         */
        public ErrorBuilder ctx(String key, Object value) {
            if (logger == null) return this; // no-op
            context.put(Objects.requireNonNull(key, "key must not be null"), value);
            return this;
        }

        /**
         * Attach a {@link Throwable} cause. Its stack trace will be appended
         * after the context entries.
         *
         * @param cause the exception or error
         * @return {@code this} for chaining
         */
        public ErrorBuilder cause(Throwable cause) {
            if (logger == null) return this; // no-op
            this.cause = cause;
            return this;
        }

        /**
         * Override the modId shown in the log prefix. Useful when logging
         * from shared utility code on behalf of a specific mod.
         *
         * @param modId the effective mod identifier
         * @return {@code this} for chaining
         */
        public ErrorBuilder mod(String modId) {
            if (logger == null) return this; // no-op
            this.modIdOverride = Objects.requireNonNull(modId, "modId must not be null");
            return this;
        }

        /**
         * Emit the error report.
         * <p>
         * Builds the final message as:
         * <pre>
         * [ERROR] [modId][ClassName] message
         *   key1: value1
         *   key2: value2
         *   Cause: exception
         *     at ...
         * </pre>
         */
        public void report() {
            if (logger == null || !logger.slf4j.isErrorEnabled()) {
                return;
            }

            String effectiveModId = modIdOverride != null ? modIdOverride : logger.modId;
            String simpleName = logger.clazz.getSimpleName();

            StringBuilder sb = new StringBuilder();
            sb.append('[').append(effectiveModId).append(']');
            sb.append('[').append(simpleName).append(']');
            sb.append(' ').append(message);

            for (var entry : context.entrySet()) {
                sb.append(System.lineSeparator());
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue());
            }

            if (cause != null) {
                sb.append(System.lineSeparator());
                sb.append("  Cause: ");
                logger.slf4j.error(sb.toString(), cause);
            } else {
                logger.slf4j.error(sb.toString());
            }
        }
    }
}
