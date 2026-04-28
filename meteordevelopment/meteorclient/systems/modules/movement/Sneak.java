/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class Sneak
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;

    public Sneak() {
        super(Categories.Movement, "sneak", "Sneaks for you");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which method to sneak.")).defaultValue(Mode.Vanilla)).build());
    }

    public boolean doPacket() {
        return this.isActive() && !this.mc.player.getAbilities().flying && this.mode.get() == Mode.Packet;
    }

    public boolean doVanilla() {
        return this.isActive() && !this.mc.player.getAbilities().flying && this.mode.get() == Mode.Vanilla;
    }

    public static enum Mode {
        Packet,
        Vanilla;

    }
}

