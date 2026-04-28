/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class UnfocusedCPU
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Integer> fps;

    public UnfocusedCPU() {
        super(Categories.Render, "unfocused-cpu", "Limits FPS when your Minecraft window is not focused.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fps = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("target-fps")).description("Target FPS to set as the limit when the window is not focused.")).min(1).defaultValue(1)).sliderRange(1, 20).build());
    }
}

