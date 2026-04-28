/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class Rotation
extends Module {
    private final SettingGroup sgYaw;
    private final SettingGroup sgPitch;
    private final Setting<LockMode> yawLockMode;
    private final Setting<Double> yawAngle;
    private final Setting<LockMode> pitchLockMode;
    private final Setting<Double> pitchAngle;

    public Rotation() {
        super(Categories.Player, "rotation", "Changes/locks your yaw and pitch.");
        this.sgYaw = this.settings.createGroup("Yaw");
        this.sgPitch = this.settings.createGroup("Pitch");
        this.yawLockMode = this.sgYaw.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("yaw-lock-mode")).description("The way in which your yaw is locked.")).defaultValue(LockMode.Simple)).build());
        this.yawAngle = this.sgYaw.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("yaw-angle")).description("Yaw angle in degrees.")).defaultValue(0.0).sliderMax(360.0).max(360.0).visible(() -> this.yawLockMode.get() == LockMode.Simple)).build());
        this.pitchLockMode = this.sgPitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("pitch-lock-mode")).description("The way in which your pitch is locked.")).defaultValue(LockMode.Simple)).build());
        this.pitchAngle = this.sgPitch.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch-angle")).description("Pitch angle in degrees.")).defaultValue(0.0).range(-90.0, 90.0).sliderRange(-90.0, 90.0).visible(() -> this.pitchLockMode.get() == LockMode.Simple)).build());
    }

    @Override
    public void onActivate() {
        this.onTick(null);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        switch (this.yawLockMode.get().ordinal()) {
            case 1: {
                this.setYawAngle(this.yawAngle.get().floatValue());
                break;
            }
            case 0: {
                this.setYawAngle(this.getSmartYawDirection());
            }
        }
        switch (this.pitchLockMode.get().ordinal()) {
            case 1: {
                this.mc.player.setPitch(this.pitchAngle.get().floatValue());
                break;
            }
            case 0: {
                this.mc.player.setPitch(this.getSmartPitchDirection());
            }
        }
    }

    private float getSmartYawDirection() {
        return (float)Math.round((this.mc.player.getYaw() + 1.0f) / 45.0f) * 45.0f;
    }

    private float getSmartPitchDirection() {
        return (float)Math.round((this.mc.player.getPitch() + 1.0f) / 30.0f) * 30.0f;
    }

    private void setYawAngle(float yawAngle) {
        this.mc.player.setYaw(yawAngle);
        this.mc.player.headYaw = yawAngle;
        this.mc.player.bodyYaw = yawAngle;
    }

    public static enum LockMode {
        Smart,
        Simple,
        None;

    }
}

