package com.bikininjas.corelib.objective;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable definition of a challenge stored in the {@link ChallengeRegistry}.
 * <p>
 * A definition describes a named challenge that players can start. It includes
 * the human-readable {@link #displayName()}, the {@link Objective}s to complete,
 * an optional wall-clock time limit, and a list of mod IDs that must be loaded
 * before the challenge can be started.
 * <p>
 * Use {@link #toChallenge()} to create a runtime {@link Challenge} instance for
 * the {@link ObjectiveTracker}.
 *
 * @param name             short registry key (e.g. {@code "dragon_rush"});
 *                         never {@code null}.
 * @param displayName      human-readable name shown in commands and the action
 *                         bar; never {@code null}.
 * @param objectives       the objectives composing the challenge; never
 *                         {@code null}.
 * @param timeLimitSeconds optional wall-clock limit in seconds ({@code 0}
 *                         means no limit).
 * @param requiredMods     mod IDs that must be loaded; never {@code null}.
 *                         An empty list means no mod requirement.
 */
public record ChallengeDefinition(
        @NotNull String name,
        @NotNull String displayName,
        @NotNull List<Objective> objectives,
        int timeLimitSeconds,
        @NotNull List<String> requiredMods
) {

    /**
     * Compact constructor that defensively copies list arguments.
     */
    public ChallengeDefinition {
        objectives = List.copyOf(objectives);
        requiredMods = List.copyOf(requiredMods);
    }

    /**
     * Convenience constructor for challenges that require no specific mods.
     *
     * @param name             registry key
     * @param displayName      human-readable name
     * @param objectives       the objectives
     * @param timeLimitSeconds time limit ({@code 0} = no limit)
     */
    public ChallengeDefinition(
            @NotNull String name,
            @NotNull String displayName,
            @NotNull List<Objective> objectives,
            int timeLimitSeconds
    ) {
        this(name, displayName, objectives, timeLimitSeconds, List.of());
    }

    /**
     * Convert this definition to a runtime {@link Challenge} for the
     * {@link ObjectiveTracker}.
     *
     * @return a new {@link Challenge} with the same objectives and time limit.
     */
    public Challenge toChallenge() {
        return new Challenge(name, objectives, timeLimitSeconds);
    }
}
