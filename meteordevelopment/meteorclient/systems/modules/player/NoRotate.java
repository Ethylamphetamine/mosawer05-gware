/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.network.packet.s2c.play.PositionFlag
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerPositionLookS2CPacketAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;

public class NoRotate
extends Module {
    public NoRotate() {
        super(Categories.Player, "no-rotate", "Attempts to block rotations sent from server to client.");
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet2 = (PlayerPositionLookS2CPacket)packet;
            if (packet2.getFlags().contains(PositionFlag.Y_ROT)) {
                ((PlayerPositionLookS2CPacketAccessor)packet2).setYaw(0.0f);
            } else {
                ((PlayerPositionLookS2CPacketAccessor)packet2).setYaw(this.mc.player.getYaw());
            }
            if (packet2.getFlags().contains(PositionFlag.X_ROT)) {
                ((PlayerPositionLookS2CPacketAccessor)packet2).setPitch(0.0f);
            } else {
                ((PlayerPositionLookS2CPacketAccessor)packet2).setPitch(this.mc.player.getPitch());
            }
        }
    }
}

