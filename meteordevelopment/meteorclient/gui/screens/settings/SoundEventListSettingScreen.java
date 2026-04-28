/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.registry.Registries
 *  net.minecraft.sound.SoundEvent
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Collection;
import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

public class SoundEventListSettingScreen
extends RegistryListSettingScreen<SoundEvent> {
    public SoundEventListSettingScreen(GuiTheme theme, Setting<List<SoundEvent>> setting) {
        super(theme, "Select Sounds", setting, (Collection)setting.get(), Registries.SOUND_EVENT);
    }

    @Override
    protected WWidget getValueWidget(SoundEvent value) {
        return this.theme.label(this.getValueName(value));
    }

    @Override
    protected String getValueName(SoundEvent value) {
        return value.getId().getPath();
    }
}

