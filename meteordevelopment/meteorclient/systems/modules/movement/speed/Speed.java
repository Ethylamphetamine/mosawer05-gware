/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.MovementType
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 */
package meteordevelopment.meteorclient.systems.modules.movement.speed;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.speed.SpeedMode;
import meteordevelopment.meteorclient.systems.modules.movement.speed.SpeedModes;
import meteordevelopment.meteorclient.systems.modules.movement.speed.modes.Strafe;
import meteordevelopment.meteorclient.systems.modules.movement.speed.modes.Vanilla;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class Speed
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<SpeedModes> speedMode;
    public final Setting<Double> vanillaSpeed;
    public final Setting<Double> ncpSpeed;
    public final Setting<Boolean> ncpSpeedLimit;
    public final Setting<Double> timer;
    public final Setting<Boolean> inLiquids;
    public final Setting<Boolean> whenSneaking;
    public final Setting<Boolean> vanillaOnGround;
    private SpeedMode currentMode;

    public Speed() {
        super(Categories.Movement, "speed", "Modifies your movement speed when moving on the ground.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.speedMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The method of applying speed.")).defaultValue(SpeedModes.Vanilla)).onModuleActivated(speedModesSetting -> this.onSpeedModeChanged((SpeedModes)((Object)((Object)speedModesSetting.get()))))).onChanged(this::onSpeedModeChanged)).build());
        this.vanillaSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vanilla-speed")).description("The speed in blocks per second.")).defaultValue(5.6).min(0.0).sliderMax(20.0).visible(() -> this.speedMode.get() == SpeedModes.Vanilla)).build());
        this.ncpSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("strafe-speed")).description("The speed.")).visible(() -> this.speedMode.get() == SpeedModes.Strafe)).defaultValue(1.6).min(0.0).sliderMax(3.0).build());
        this.ncpSpeedLimit = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("speed-limit")).description("Limits your speed on servers with very strict anticheats.")).visible(() -> this.speedMode.get() == SpeedModes.Strafe)).defaultValue(false)).build());
        this.timer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("timer")).description("Timer override.")).defaultValue(1.0).min(0.01).sliderMin(0.01).sliderMax(10.0).build());
        this.inLiquids = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("in-liquids")).description("Uses speed when in lava or water.")).defaultValue(false)).build());
        this.whenSneaking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("when-sneaking")).description("Uses speed when sneaking.")).defaultValue(false)).build());
        this.vanillaOnGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Uses speed only when standing on a block.")).visible(() -> this.speedMode.get() == SpeedModes.Vanilla)).defaultValue(false)).build());
        this.onSpeedModeChanged(this.speedMode.get());
    }

    @Override
    public void onActivate() {
        this.currentMode.onActivate();
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(1.0);
        this.currentMode.onDeactivate();
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF || this.mc.player.isFallFlying() || this.mc.player.isClimbing() || this.mc.player.getVehicle() != null) {
            return;
        }
        if (!this.whenSneaking.get().booleanValue() && this.mc.player.isSneaking()) {
            return;
        }
        if (this.vanillaOnGround.get().booleanValue() && !this.mc.player.isOnGround() && this.speedMode.get() == SpeedModes.Vanilla) {
            return;
        }
        if (!this.inLiquids.get().booleanValue() && (this.mc.player.isTouchingWater() || this.mc.player.isInLava())) {
            return;
        }
        if (this.timer.get() != 1.0) {
            Modules.get().get(Timer.class).setOverride(PlayerUtils.isMoving() ? this.timer.get() : 1.0);
        }
        this.currentMode.onMove(event);
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.mc.player.isFallFlying() || this.mc.player.isClimbing() || this.mc.player.getVehicle() != null) {
            return;
        }
        if (!this.whenSneaking.get().booleanValue() && this.mc.player.isSneaking()) {
            return;
        }
        if (this.vanillaOnGround.get().booleanValue() && !this.mc.player.isOnGround() && this.speedMode.get() == SpeedModes.Vanilla) {
            return;
        }
        if (!this.inLiquids.get().booleanValue() && (this.mc.player.isTouchingWater() || this.mc.player.isInLava())) {
            return;
        }
        this.currentMode.onTick();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            this.currentMode.onRubberband();
        }
    }

    private void onSpeedModeChanged(SpeedModes mode) {
        switch (mode) {
            case Vanilla: {
                this.currentMode = new Vanilla();
                break;
            }
            case Strafe: {
                this.currentMode = new Strafe();
            }
        }
    }

    @Override
    public String getInfoString() {
        return this.currentMode.getHudString();
    }
}

