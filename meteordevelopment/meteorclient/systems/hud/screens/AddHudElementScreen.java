/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 */
package meteordevelopment.meteorclient.systems.hud.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudElementPresetsScreen;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class AddHudElementScreen
extends WindowScreen {
    private final int x;
    private final int y;
    private final WTextBox searchBar;
    private Object firstObject;

    public AddHudElementScreen(GuiTheme theme, int x, int y) {
        super(theme, "Add Hud element");
        this.x = x;
        this.y = y;
        this.searchBar = theme.textBox("");
        this.searchBar.action = () -> {
            this.clear();
            this.initWidgets();
        };
        this.enterAction = () -> this.runObject(this.firstObject);
    }

    @Override
    public void initWidgets() {
        this.firstObject = null;
        this.add(this.searchBar).expandX();
        this.searchBar.setFocused(true);
        Hud hud = Hud.get();
        HashMap<HudGroup, List> grouped = new HashMap<HudGroup, List>();
        for (HudElementInfo<?> info : hud.infos.values()) {
            if (info.hasPresets() && !this.searchBar.get().isEmpty()) {
                for (HudElementInfo.Preset preset : info.presets) {
                    String title = info.title + "  -  " + preset.title;
                    if (!Utils.searchTextDefault(title, this.searchBar.get(), false)) continue;
                    grouped.computeIfAbsent(info.group, hudGroup -> new ArrayList()).add(new Item(title, info.description, preset));
                }
                continue;
            }
            if (!Utils.searchTextDefault(info.title, this.searchBar.get(), false)) continue;
            grouped.computeIfAbsent(info.group, hudGroup -> new ArrayList()).add(new Item(info.title, info.description, info));
        }
        for (HudGroup group : grouped.keySet()) {
            WSection section = this.add(this.theme.section(group.title())).expandX().widget();
            for (Item item : (List)grouped.get(group)) {
                WHorizontalList l = section.add(this.theme.horizontalList()).expandX().widget();
                WLabel title = l.add(this.theme.label(item.title)).widget();
                title.tooltip = item.description;
                Object object = item.object;
                if (object instanceof HudElementInfo.Preset) {
                    HudElementInfo.Preset preset = (HudElementInfo.Preset)object;
                    WPlus add = l.add(this.theme.plus()).expandCellX().right().widget();
                    add.action = () -> this.runObject(preset);
                    if (this.firstObject != null) continue;
                    this.firstObject = preset;
                    continue;
                }
                HudElementInfo info = (HudElementInfo)item.object;
                if (info.hasPresets()) {
                    WButton open = l.add(this.theme.button(" > ")).expandCellX().right().widget();
                    open.action = () -> this.runObject(info);
                } else {
                    WPlus add = l.add(this.theme.plus()).expandCellX().right().widget();
                    add.action = () -> this.runObject(info);
                }
                if (this.firstObject != null) continue;
                this.firstObject = info;
            }
        }
    }

    private void runObject(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof HudElementInfo.Preset) {
            HudElementInfo.Preset preset = (HudElementInfo.Preset)object;
            Hud.get().add(preset, this.x, this.y);
            this.close();
        } else {
            HudElementInfo info = (HudElementInfo)object;
            if (info.hasPresets()) {
                HudElementPresetsScreen screen = new HudElementPresetsScreen(this.theme, info, this.x, this.y);
                screen.parent = this.parent;
                MeteorClient.mc.setScreen((Screen)screen);
            } else {
                Hud.get().add(info, this.x, this.y);
                this.close();
            }
        }
    }

    @Override
    protected void onRenderBefore(DrawContext drawContext, float delta) {
        HudEditorScreen.renderElements(drawContext);
    }

    private record Item(String title, String description, Object object) {
    }
}

