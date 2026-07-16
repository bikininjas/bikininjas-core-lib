package com.bikininjas.corelib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * Brigadier command tree for challenge management.
 * <p>
 * Registered by {@link CommandRegister} on the NeoForge event bus.
 * All methods are static; no instance is required.
 */
public final class ChallengeCommand {

    private ChallengeCommand() {
    }

    /**
     * Register the {@code /challenge} command tree on the given dispatcher.
     *
     * @param dispatcher the command dispatcher to register on
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("challenge")
                .then(Commands.literal("list")
                        .executes(ChallengeCommand::handleList)
                )
                .then(Commands.literal("start")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(ChallengeCommand::suggestChallenges)
                                .executes(ChallengeCommand::handleStart)
                        )
                )
                .then(Commands.literal("status")
                        .executes(ChallengeCommand::handleStatus)
                )
                .then(Commands.literal("abort")
                        .requires(src -> src.getPlayer() != null)
                        .executes(ChallengeCommand::handleAbort)
                )
        );
    }

    // ──────────────────────────────────────────────
    //  Handlers
    // ──────────────────────────────────────────────

    private static int handleList(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var definitions = com.bikininjas.corelib.objective.ChallengeRegistry.getAvailable();

        if (definitions.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No challenges available."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6§lAvailable Challenges:"), false);
        for (var def : definitions) {
            int objCount = def.objectives().size();
            String limit = def.timeLimitSeconds() > 0
                    ? String.format(" §7(%d:%02d)",
                    def.timeLimitSeconds() / 60, def.timeLimitSeconds() % 60)
                    : "";
            source.sendSuccess(() -> Component.literal(
                    String.format(" §e• §f%s §7- %d objective(s)%s",
                            def.displayName(), objCount, limit)), false);
        }
        return definitions.size();
    }

    private static int handleStart(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§cOnly players can start challenges."));
            return 0;
        }

        String name = StringArgumentType.getString(ctx, "name");
        var def = com.bikininjas.corelib.objective.ChallengeRegistry.get(name);
        if (def == null) {
            source.sendFailure(Component.literal("§cChallenge '" + name + "' not found."));
            return 0;
        }

        // Check mod requirements.
        if (!com.bikininjas.corelib.objective.ChallengeRegistry.areModsLoaded(def)) {
            source.sendFailure(Component.literal(
                    "§cRequired mods not loaded for challenge '" + name + "'."));
            return 0;
        }

        // Check no active challenge.
        if (com.bikininjas.corelib.objective.ObjectiveTracker.isTracking(player)) {
            source.sendFailure(Component.literal(
                    "§cYou already have an active challenge. Use /challenge abort first."));
            return 0;
        }

        var challenge = def.toChallenge();
        com.bikininjas.corelib.objective.ObjectiveTracker.startChallenge(player, challenge);

        source.sendSuccess(() -> Component.literal(
                "§a✔ Challenge '§f" + def.displayName() + "§a' started!"), false);
        return 1;
    }

    private static int handleStatus(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§cOnly players can check status."));
            return 0;
        }

        if (!com.bikininjas.corelib.objective.ObjectiveTracker.isTracking(player)) {
            source.sendFailure(Component.literal("§cYou have no active challenge."));
            return 0;
        }

        String name = com.bikininjas.corelib.objective.ObjectiveTracker.getActiveChallengeName(player);
        float progress = com.bikininjas.corelib.objective.ObjectiveTracker.getProgress(player);
        int pct = Math.round(progress * 100.0f);
        long elapsed = com.bikininjas.corelib.objective.ObjectiveTracker.getElapsedSeconds(player);
        long mins = elapsed / 60;
        long secs = elapsed % 60;

        source.sendSuccess(() -> Component.literal("§6§lChallenge: §f" + (name != null ? name : "?")), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "§7Progress: §a%d%% §7| §eTime: %d:%02d", pct, mins, secs)), false);

        var objectives = com.bikininjas.corelib.objective.ObjectiveTracker.getObjectives(player);
        for (var obj : objectives) {
            int cur = obj.progressValue(player);
            int tgt = obj.target();
            String status = obj.isComplete(player) ? "§a✔" : "§7⬜";
            source.sendSuccess(() -> Component.literal(String.format(
                    " %s §f%s §7(%d/%d)", status, obj.description(), cur, tgt)), false);
        }
        return objectives.size();
    }

    private static int handleAbort(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§cOnly players can abort challenges."));
            return 0;
        }

        if (!com.bikininjas.corelib.objective.ObjectiveTracker.isTracking(player)) {
            source.sendFailure(Component.literal("§cYou have no active challenge to abort."));
            return 0;
        }

        String name = com.bikininjas.corelib.objective.ObjectiveTracker.getActiveChallengeName(player);
        com.bikininjas.corelib.objective.ObjectiveTracker.stopChallenge(player);

        source.sendSuccess(() -> Component.literal(
                "§eChallenge '§f" + (name != null ? name : "?") + "§e' aborted."), false);
        return 1;
    }

    // ──────────────────────────────────────────────
    //  Tab-completion suggestions
    // ──────────────────────────────────────────────

    private static CompletableFuture<Suggestions> suggestChallenges(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        var definitions = com.bikininjas.corelib.objective.ChallengeRegistry.getAvailable();
        for (var def : definitions) {
            if (def.name().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(def.name(), Component.literal(def.displayName()));
            }
        }
        return builder.buildFuture();
    }
}
