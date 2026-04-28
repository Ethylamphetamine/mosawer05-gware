/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.BoatMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.Vec3d;

public class BoatFly
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> speed;
    private final Setting<Double> verticalSpeed;
    private final Setting<Double> fallSpeed;
    private final Setting<Boolean> cancelServerPackets;

    public BoatFly() {
        super(Categories.Movement, "boat-fly", "Transforms your boat into a plane.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("Horizontal speed in blocks per second.")).defaultValue(10.0).min(0.0).sliderMax(50.0).build());
        this.verticalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vertical-speed")).description("Vertical speed in blocks per second.")).defaultValue(6.0).min(0.0).sliderMax(20.0).build());
        this.fallSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-speed")).description("How fast you fall in blocks per second.")).defaultValue(0.1).min(0.0).build());
        this.cancelServerPackets = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cancel-server-packets")).description("Cancels incoming boat move packets.")).defaultValue(false)).build());
    }

    @EventHandler
    private void onBoatMove(BoatMoveEvent event) {
        if (event.boat.getControllingPassenger() != this.mc.player) {
            return;
        }
        event.boat.setYaw(this.mc.player.getYaw());
        Vec3d vel = PlayerUtils.getHorizontalVelocity(this.speed.get());
        double velX = vel.getX();
        double velY = 0.0;
        double velZ = vel.getZ();
        if (this.mc.options.jumpKey.isPressed()) {
            velY += this.verticalSpeed.get() / 20.0;
        }
        velY = this.mc.options.sprintKey.isPressed() ? (velY -= this.verticalSpeed.get() / 20.0) : (velY -= this.fallSpeed.get() / 20.0);
        ((IVec3d)event.boat.getVelocity()).set(velX, velY, velZ);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof VehicleMoveS2CPacket && this.cancelServerPackets.get().booleanValue()) {
            event.cancel();
        }
    }
}

