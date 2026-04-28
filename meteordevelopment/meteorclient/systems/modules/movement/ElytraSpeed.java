/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraFakeFly;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraSpeed
extends Module {
    private final SettingGroup sgGeneral;
    private boolean using;
    private double yaw;
    private double pitch;
    private Vec3d lastMovement;
    private boolean rubberband;
    private final Setting<Double> startSpeed;
    private final Setting<Double> accel;
    private final Setting<Double> maxSpeed;

    public ElytraSpeed() {
        super(Categories.Movement, "elytra-speed", "Makes your elytra faster when you use a firework.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.startSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("start-speed")).description("Initial speed when you use a firework")).defaultValue(30.0).min(0.0).sliderMax(100.0).build());
        this.accel = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("accel-speed")).description("Acceleration")).defaultValue(3.0).min(0.0).sliderMax(5.0).build());
        this.maxSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-speed")).description("Maximum speed you can go while flying")).defaultValue(100.0).min(0.0).sliderMax(250.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.using = false;
        for (Entity entity : this.mc.world.getEntities()) {
            FireworkRocketEntity firework;
            if (!(entity instanceof FireworkRocketEntity) || (firework = (FireworkRocketEntity)entity).getOwner() == null || !firework.getOwner().equals((Object)this.mc.player)) continue;
            this.using = true;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.rubberband) {
            return;
        }
        this.yaw = Math.toRadians(this.mc.player.getYaw());
        this.pitch = Math.toRadians(this.mc.player.getPitch());
    }

    @Override
    public void onDeactivate() {
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!this.isActive()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket lookS2CPacket = (PlayerPositionLookS2CPacket)packet;
            this.rubberband = true;
            this.lastMovement = new Vec3d(0.0, 0.0, 0.0);
            this.yaw = Math.toRadians(lookS2CPacket.getYaw());
            this.pitch = Math.toRadians(lookS2CPacket.getPitch());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Vec3d slerpedDirection;
        Vec3d newMovement;
        ElytraFly eFly = Modules.get().get(ElytraFly.class);
        ElytraFakeFly geFly = Modules.get().get(ElytraFakeFly.class);
        if (!(this.rubberband || this.using && this.mc.player.isFallFlying() && !eFly.isActive() && !geFly.isActive())) {
            this.lastMovement = event.movement;
            return;
        }
        if (!this.isActive()) {
            return;
        }
        if (this.lastMovement == null) {
            this.lastMovement = event.movement;
        }
        Vec3d direction = new Vec3d(-Math.sin(this.yaw) * Math.cos(this.pitch), -Math.sin(this.pitch), Math.cos(this.yaw) * Math.cos(this.pitch)).normalize();
        Vec3d currentMovement = direction.multiply(this.lastMovement.length());
        if (this.rubberband) {
            newMovement = currentMovement;
        } else {
            newMovement = this.lastMovement.length() < this.startSpeed.get() / 20.0 ? direction.multiply(this.startSpeed.get() / 20.0) : currentMovement.add(direction.multiply(this.accel.get() / 20.0));
            if (newMovement.length() > this.maxSpeed.get() / 20.0) {
                newMovement = newMovement.normalize().multiply(this.maxSpeed.get() / 20.0);
            }
        }
        double speed = this.lastMovement.length();
        double speedFactor = Math.max(0.1, Math.min(1.0, (this.maxSpeed.get() * 2.5 / 20.0 - speed) / (this.maxSpeed.get() * 2.5 / 20.0)));
        Vec3d lastDirection = this.lastMovement.normalize();
        Vec3d newDirection = newMovement.normalize();
        double dot = lastDirection.dotProduct(newDirection);
        dot = MathHelper.clamp((double)dot, (double)-1.0, (double)1.0);
        double theta = Math.acos(dot) * speedFactor;
        if (Math.abs(theta) < 0.001) {
            slerpedDirection = newDirection;
        } else {
            Vec3d relativeDirection = newDirection.subtract(lastDirection.multiply(dot)).normalize();
            slerpedDirection = lastDirection.multiply(Math.cos(theta)).add(relativeDirection.multiply(Math.sin(theta)));
        }
        if (this.lastMovement.length() < this.startSpeed.get()) {
            newMovement = slerpedDirection.multiply(newMovement.length());
        }
        if (newMovement.length() > this.maxSpeed.get() / 20.0) {
            newMovement = newMovement.normalize().multiply(this.maxSpeed.get() / 20.0);
        }
        this.mc.player.setVelocity(newMovement);
        ((IVec3d)event.movement).set(newMovement.x, newMovement.y, newMovement.z);
        this.lastMovement = newMovement;
        if (this.rubberband) {
            this.rubberband = false;
        }
    }
}

