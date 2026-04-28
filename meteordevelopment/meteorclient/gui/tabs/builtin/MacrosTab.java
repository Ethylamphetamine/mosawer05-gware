/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.EditSystemScreen;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.macros.Macros;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

public class MacrosTab
extends Tab {
    public MacrosTab() {
        super("Macros");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new MacrosScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof MacrosScreen;
    }

    private static class MacrosScreen
    extends WindowTabScreen {
        public MacrosScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = this.add(this.theme.table()).expandX().minWidth(400.0).widget();
            this.initTable(table);
            this.add(this.theme.horizontalSeparator()).expandX();
            WButton create = this.add(this.theme.button("Create")).expandX().widget();
            create.action = () -> MeteorClient.mc.setScreen((Screen)new EditMacroScreen(this.theme, null, this::reload));
        }

        private void initTable(WTable table) {
            table.clear();
            if (Macros.get().isEmpty()) {
                return;
            }
            for (Macro macro : Macros.get()) {
                table.add(this.theme.label(macro.name.get() + " (" + String.valueOf(macro.keybind.get()) + ")"));
                WButton edit = table.add(this.theme.button(GuiRenderer.EDIT)).expandCellX().right().widget();
                edit.action = () -> MeteorClient.mc.setScreen((Screen)new EditMacroScreen(this.theme, macro, this::reload));
                WMinus remove = table.add(this.theme.minus()).widget();
                remove.action = () -> {
                    Macros.get().remove(macro);
                    this.reload();
                };
                table.row();
            }
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Macros.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Macros.get());
        }
    }

    private static class EditMacroScreen
    extends EditSystemScreen<Macro> {
        public EditMacroScreen(GuiTheme theme, Macro value, Runnable reload) {
            super(theme, value, reload);
        }

        @Override
        public Macro create() {
            return new Macro();
        }

        @Override
        public boolean save() {
            if (((Macro)this.value).name.get().isBlank() || ((Macro)this.value).messages.get().isEmpty() || !((Macro)this.value).keybind.get().isSet()) {
                return false;
            }
            if (this.isNew) {
                for (Macro m : Macros.get()) {
                    if (!((Macro)this.value).equals(m)) continue;
                    return false;
                }
            }
            if (this.isNew) {
                Macros.get().add((Macro)this.value);
            } else {
                Macros.get().save();
            }
            return true;
        }

        @Override
        public Settings getSettings() {
            return ((Macro)this.value).settings;
        }
    }
}

