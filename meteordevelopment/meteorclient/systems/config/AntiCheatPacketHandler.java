/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 */
package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class AntiCheatPacketHandler {
    public static final AntiCheatPacketHandler INSTANCE = new AntiCheatPacketHandler();

    private AntiCheatPacketHandler() {
    }

    @EventHandler(priority=200)
    private void onPacketSend(PacketEvent.Send event) {
        PlayerActionC2SPacket packet;
        if (!AntiCheatConfig.get().instantMineBypass.get().booleanValue()) {
            return;
        }
        if (MeteorClient.mc.player == null) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof PlayerActionC2SPacket && (packet = (PlayerActionC2SPacket)packet2).getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), packet.getDirection()));
            PacketManager.INSTANCE.incrementInteract();
        }
    }
}

