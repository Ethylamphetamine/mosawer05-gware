/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class AntiPacketKick
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> catchExceptions;
    public final Setting<Boolean> logExceptions;

    public AntiPacketKick() {
        super(Categories.Misc, "anti-packet-kick", "Attempts to prevent you from being disconnected by large packets.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.catchExceptions = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("catch-exceptions")).description("Drops corrupted packets.")).defaultValue(false)).build());
        this.logExceptions = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("log-exceptions")).description("Logs caught exceptions.")).defaultValue(false)).visible(this.catchExceptions::get)).build());
    }

    public boolean catchExceptions() {
        return this.isActive() && this.catchExceptions.get() != false;
    }
}

