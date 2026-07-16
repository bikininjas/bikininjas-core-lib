package com.bikininjas.corelib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Client-side HUD overlay that renders a translucent stats panel
 * on the right side of the screen.
 * <p>
 * The overlay is drawn during {@link RenderGuiEvent.Post} and respects
 * the visibility and field-preference flags synced from the server.
 * Auto-sizing: the panel width expands to fit the longest stat line.
 * <p>
 * Registered on the NeoForge event bus from {@code CoreLib} via
 * {@code FMLClientSetupEvent}.
 */
public final class StatsOverlayRenderer {

    private static final int BG_COLOR         = 0x88000000;
    private static final int HEADER_COLOR     = 0xFFDAA520;
    private static final int DEATH_COLOR      = 0xFFFF5555;
    private static final int KILL_COLOR       = 0xFFFFAA00;
    private static final int BLOCK_COLOR      = 0xFFAAAAAA;
    private static final int CRAFT_COLOR      = 0xFFFF55FF;
    private static final int MARGIN           = 4;

    private record UiLine(String text, int color) {}

    private StatsOverlayRenderer() {}

    @SubscribeEvent
    static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!StatsClientData.isVisible()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics gg = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // ── Build lines ────────────────────────────
        java.util.List<UiLine> uiLines = new java.util.ArrayList<>();
        uiLines.add(new UiLine("Stats:", HEADER_COLOR));

        var fields = StatsClientData.getVisibleFields();
        if (fields.contains("deaths"))
            uiLines.add(new UiLine("\u2620 Deaths: " + StatsClientData.getDeaths(), DEATH_COLOR));
        if (fields.contains("kills"))
            uiLines.add(new UiLine("\u2694 Kills: " + StatsClientData.getKills(), KILL_COLOR));
        if (fields.contains("blocksBroken"))
            uiLines.add(new UiLine("\u26CF Blocks Broken: " + StatsClientData.getBlocksBroken(), BLOCK_COLOR));
        if (fields.contains("crafts"))
            uiLines.add(new UiLine("\uD83D\uDD28 Crafts: " + StatsClientData.getCrafts(), CRAFT_COLOR));

        if (uiLines.size() <= 1) {
            return;
        }

        // ── Layout ─────────────────────────────────
        int lineHeight = font.lineHeight + 1;
        int width = 0;
        for (UiLine line : uiLines) {
            width = Math.max(width, font.width(line.text));
        }
        width += MARGIN * 2;
        int height = uiLines.size() * lineHeight + MARGIN * 2;

        int x = screenWidth - width - MARGIN;
        int y = screenHeight / 2 - height / 2;

        // ── Draw background ─────────────────────────
        gg.fill(x, y, x + width, y + height, BG_COLOR);

        // ── Draw text ───────────────────────────────
        int textY = y + MARGIN;
        for (UiLine line : uiLines) {
            gg.drawString(font, line.text, x + MARGIN, textY, line.color);
            textY += lineHeight;
        }
    }
}
