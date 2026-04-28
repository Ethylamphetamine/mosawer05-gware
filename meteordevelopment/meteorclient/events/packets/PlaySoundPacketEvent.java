/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 */
package meteordevelopment.meteorclient.events.packets;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;

public class PlaySoundPacketEvent {
    private static final PlaySoundPacketEvent INSTANCE = new PlaySoundPacketEvent();
    public PlaySoundS2CPacket packet;

    public static PlaySoundPacketEvent get(PlaySoundS2CPacket packet) {
        PlaySoundPacketEvent.INSTANCE.packet = packet;
        return INSTANCE;
    }
}

