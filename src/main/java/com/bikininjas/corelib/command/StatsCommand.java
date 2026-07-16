package com.bikininjas.corelib.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /stats} command — displays tracked statistics for the issuing player.
 * <p>
 * Delegates to {@code PlayerStatsManager} for counter data.
 */
public final class StatsCommand {

    private StatsCommand() {
    }

    /**
     * Register the {@code /stats} command on the given dispatcher.
     *
     * @param dispatcher the command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stats")
                .requires(src -> src.getPlayer() != null)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendFailure(Component.literal("§cOnly players can use /stats."));
                        return 0;
                    }

                    var stats = com.bikininjas.corelib.stats.PlayerStatsManager.getStats(player);

                    ctx.getSource().sendSuccess(() -> Component.literal("§6§lYour Stats:"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            String.format(" §c☠ Deaths: §f%d", stats.deaths())), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            String.format(" §e⚔ Kills: §f%d", stats.kills())), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            String.format(" §7⛏ Blocks Broken: §f%d", stats.blocksBroken())), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            String.format(" §d🔨 Crafts: §f%d", stats.crafts())), false);
                    return 1;
                })
        );
    }
}
