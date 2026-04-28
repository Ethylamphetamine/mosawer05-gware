/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

public class Slide
extends ElytraFlightMode {
    boolean rubberbanded = false;
    int tickDelay;

    public Slide() {
        super(ElytraFlightModes.Slide);
        this.tickDelay = this.elytraFly.restartDelay.get();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (this.mc.options.jumpKey.isPressed() && !this.mc.player.isFallFlying()) {
            this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        if (Slide.checkConditions(this.mc.player) && this.mc.player.isOnGround()) {
            double yaw = Math.toRadians(this.mc.player.getYaw());
            double speedFactor = Math.max(0.1, Math.min(1.0, (100.0 * this.elytraFly.slideAccel.get() / 20.0 - this.mc.player.getVelocity().length()) / (100.0 * this.elytraFly.slideAccel.get() / 20.0)));
            Vec3d dir = new Vec3d(-Math.sin(yaw), 0.0, Math.cos(yaw));
            this.mc.player.addVelocity(dir.multiply(this.elytraFly.slideMaxSpeed.get() / 2000.0 / speedFactor));
            if (this.rubberbanded && this.elytraFly.restart.get().booleanValue()) {
                if (this.tickDelay > 0) {
                    --this.tickDelay;
                } else {
                    this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    this.rubberbanded = false;
                    this.tickDelay = this.elytraFly.restartDelay.get();
                }
            }
        }
    }

    @Override
    public void onPreTick() {
        super.onPreTick();
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            this.rubberbanded = true;
            this.mc.player.stopFallFlying();
        }
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket)event.packet).getMode().equals((Object)ClientCommandC2SPacket.Mode.START_FALL_FLYING) && !this.elytraFly.sprint.get().booleanValue()) {
            this.mc.player.setSprinting(true);
        }
    }

    public static boolean recastElytra(ClientPlayerEntity player) {
        if (Slide.checkConditions(player) && Slide.ignoreGround(player)) {
            player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return true;
        }
        return false;
    }

    public static boolean checkConditions(ClientPlayerEntity player) {
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
        return !player.hasVehicle() && !player.isClimbing() && itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable((ItemStack)itemStack);
    }

    private static boolean ignoreGround(ClientPlayerEntity player) {
        if (!player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable((ItemStack)itemStack)) {
                player.startFallFlying();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void onActivate() {
    }

    @Override
    public void onDeactivate() {
        this.rubberbanded = false;
    }
}

