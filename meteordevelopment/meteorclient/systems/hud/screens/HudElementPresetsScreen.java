/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package meteordevelopment.meteorclient.systems.hud.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.DrawContext;

public class HudElementPresetsScreen
extends WindowScreen {
    private final HudElementInfo<?> info;
    private final int x;
    private final int y;
    private final WTextBox searchBar;
    private HudElementInfo.Preset firstPreset;

    public HudElementPresetsScreen(GuiTheme theme, HudElementInfo<?> info, int x, int y) {
        super(theme, "Select preset for " + info.title);
        this.info = info;
        this.x = x + 9;
        this.y = y;
        this.searchBar = theme.textBox("");
        this.searchBar.action = () -> {
            this.clear();
            this.initWidgets();
        };
        this.enterAction = () -> {
            Hud.get().add(this.firstPreset, x, y);
            this.close();
        };
    }

    @Override
    public void initWidgets() {
        this.firstPreset = null;
        this.add(this.searchBar).expandX();
        this.searchBar.setFocused(true);
        for (HudElementInfo.Preset preset : this.info.presets) {
            if (!Utils.searchTextDefault(preset.title, this.searchBar.get(), false)) continue;
            WHorizontalList l = this.add(this.theme.horizontalList()).expandX().widget();
            l.add(this.theme.label(preset.title));
            WPlus add = l.add(this.theme.plus()).expandCellX().right().widget();
            add.action = () -> {
                Hud.get().add(preset, this.x, this.y);
                this.close();
            };
            if (this.firstPreset != null) continue;
            this.firstPreset = preset;
        }
    }

    @Override
    protected void onRenderBefore(DrawContext drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }
}

