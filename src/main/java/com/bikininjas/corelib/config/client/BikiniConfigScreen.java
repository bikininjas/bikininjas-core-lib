package com.bikininjas.corelib.config.client;

import com.bikininjas.corelib.config.BikiniConfigRegistry;
import com.bikininjas.corelib.config.ConfigOption;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game GUI for managing Bikini mod configurations.
 * Shows mod list on the left, options for the selected mod on the right.
 */
public class BikiniConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    private final List<String> modIds;
    private final List<List<ConfigOption>> modOptions;
    private int selectedModIndex;

    public BikiniConfigScreen() {
        super(Component.literal("Bikini Config"));
        this.modIds = new ArrayList<>(BikiniConfigRegistry.getRegisteredMods());
        this.modOptions = new ArrayList<>();
        for (var modId : modIds) {
            modOptions.add(new ArrayList<>(BikiniConfigRegistry.getOptions(modId)));
        }
    }

    @Override
    protected void init() {
        super.init();
        int y = 40;

        for (int i = 0; i < modIds.size(); i++) {
            final int idx = i;
            var name = BikiniConfigRegistry.getModDisplayName(modIds.get(i));
            addRenderableWidget(Button.builder(
                    Component.literal(idx == selectedModIndex ? "§a▶ " + name : "  " + name),
                    btn -> { selectedModIndex = idx; rebuildWidgets(); }
            ).pos(10, y).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + 4;
        }

        if (selectedModIndex < modOptions.size()) {
            y = 40;
            for (var option : modOptions.get(selectedModIndex)) {
                addRenderableWidget(createOptionWidget(option, y));
                y += BUTTON_HEIGHT + 4;
            }
        }

        addRenderableWidget(Button.builder(
                Component.literal("Close"), btn -> onClose()
        ).pos(this.width / 2 - 50, this.height - 30).size(100, BUTTON_HEIGHT).build());
    }

    private Button createOptionWidget(ConfigOption option, int y) {
        var label = option.displayName().copy().append(": ").append(String.valueOf(option.currentValue()));
        return Button.builder(label, btn -> {
            Object newValue = toggleValue(option);
            BikiniConfigRegistry.updateValue(option.modId(), option.key(), newValue);
            rebuildWidgets();
        }).pos(this.width / 2 + 10, y).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }

    private Object toggleValue(ConfigOption option) {
        return switch (option.type()) {
            case BOOL -> !(boolean) option.currentValue();
            case ENUM -> {
                var values = option.enumValues();
                var current = (String) option.currentValue();
                for (int i = 0; i < values.length; i++) {
                    if (values[i].equals(current)) {
                        yield values[(i + 1) % values.length];
                    }
                }
                yield values[0];
            }
            default -> option.currentValue();
        };
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        gui.drawString(font, "§6§lBikini Mods", 10, 25, 0xFFFFFF);
        if (selectedModIndex < modIds.size()) {
            gui.drawString(font, "§6§l" + BikiniConfigRegistry.getModDisplayName(modIds.get(selectedModIndex)),
                    width / 2 + 10, 25, 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
