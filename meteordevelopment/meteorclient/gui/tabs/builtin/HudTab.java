/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.nbt.NbtCompound
 */
package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;

public class HudTab
extends Tab {
    public HudTab() {
        super("HUD");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HudScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HudScreen;
    }

    public static class HudScreen
    extends WindowTabScreen {
        private final Hud hud = Hud.get();

        public HudScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            this.hud.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            this.add(this.theme.settings(this.hud.settings)).expandX();
            this.add(this.theme.horizontalSeparator()).expandX();
            WButton openEditor = this.add(this.theme.button("Edit")).expandX().widget();
            openEditor.action = () -> MeteorClient.mc.setScreen((Screen)new HudEditorScreen(this.theme));
            WHorizontalList buttons = this.add(this.theme.horizontalList()).expandX().widget();
            buttons.add(this.theme.button((String)"Clear")).expandX().widget().action = this.hud::clear;
            buttons.add(this.theme.button((String)"Reset to default elements")).expandX().widget().action = this.hud::resetToDefaultElements;
            this.add(this.theme.horizontalSeparator()).expandX();
            WHorizontalList bottom = this.add(this.theme.horizontalList()).expandX().widget();
            bottom.add(this.theme.label("Active: "));
            WCheckbox active = bottom.add(this.theme.checkbox(this.hud.active)).expandCellX().widget();
            active.action = () -> {
                this.hud.active = active.checked;
            };
            WButton resetSettings = bottom.add(this.theme.button(GuiRenderer.RESET)).widget();
            resetSettings.action = this.hud.settings::reset;
        }

        @Override
        protected void onRenderBefore(DrawContext drawContext, float delta) {
            HudEditorScreen.renderElements(drawContext);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard("hud-settings", this.hud.settings.toTag());
        }

        @Override
        public boolean fromClipboard() {
            NbtCompound clipboard = NbtUtils.fromClipboard(this.hud.settings.toTag());
            if (clipboard != null) {
                this.hud.settings.fromTag(clipboard);
                return true;
            }
            return false;
        }
    }
}

