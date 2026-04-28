/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Packet
extends ElytraFlightMode {
    private final Vec3d vec3d = new Vec3d(0.0, 0.0, 0.0);

    public Packet() {
        super(ElytraFlightModes.Packet);
    }

    @Override
    public void onDeactivate() {
        this.mc.player.getAbilities().flying = false;
        this.mc.player.getAbilities().allowFlying = false;
    }

    @Override
    public void onTick() {
        super.onTick();
        if (this.mc.player.getInventory().getArmorStack(2).getItem() != Items.ELYTRA || (double)this.mc.player.fallDistance <= 0.2 || this.mc.options.sneakKey.isPressed()) {
            return;
        }
        if (this.mc.options.forwardKey.isPressed()) {
            this.vec3d.add(0.0, 0.0, this.elytraFly.horizontalSpeed.get().doubleValue());
            this.vec3d.rotateY(-((float)Math.toRadians(this.mc.player.getYaw())));
        } else if (this.mc.options.backKey.isPressed()) {
            this.vec3d.add(0.0, 0.0, this.elytraFly.horizontalSpeed.get().doubleValue());
            this.vec3d.rotateY((float)Math.toRadians(this.mc.player.getYaw()));
        }
        if (this.mc.options.jumpKey.isPressed()) {
            this.vec3d.add(0.0, this.elytraFly.verticalSpeed.get().doubleValue(), 0.0);
        } else if (!this.mc.options.jumpKey.isPressed()) {
            this.vec3d.add(0.0, -this.elytraFly.verticalSpeed.get().doubleValue(), 0.0);
        }
        this.mc.player.setVelocity(this.vec3d);
        this.mc.player.networkHandler.sendPacket((net.minecraft.network.packet.Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        this.mc.player.networkHandler.sendPacket((net.minecraft.network.packet.Packet)new PlayerMoveC2SPacket.OnGroundOnly(true));
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) {
            this.mc.player.networkHandler.sendPacket((net.minecraft.network.packet.Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @Override
    public void onPlayerMove() {
        this.mc.player.getAbilities().flying = true;
        this.mc.player.getAbilities().setFlySpeed(this.elytraFly.horizontalSpeed.get().floatValue() / 20.0f);
    }
}

