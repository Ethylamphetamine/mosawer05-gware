/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Collection;
import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class ModuleListSettingScreen
extends RegistryListSettingScreen<Module> {
    public ModuleListSettingScreen(GuiTheme theme, Setting<List<Module>> setting) {
        super(theme, "Select Modules", setting, (Collection)setting.get(), Modules.REGISTRY);
    }

    @Override
    protected WWidget getValueWidget(Module value) {
        return this.theme.label(this.getValueName(value));
    }

    @Override
    protected String getValueName(Module value) {
        return value.title;
    }
}

