/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Entry
 */
package meteordevelopment.meteorclient.events.game;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class PlayerJoinLeaveEvent {

    public static class Leave {
        private static final Leave INSTANCE = new Leave();
        private PlayerListEntry entry;

        public static Leave get(PlayerListEntry entry) {
            Leave.INSTANCE.entry = entry;
            return INSTANCE;
        }

        public PlayerListEntry getEntry() {
            return this.entry;
        }
    }

    public static class Join {
        private static final Join INSTANCE = new Join();
        private PlayerListS2CPacket.Entry entry;

        public static Join get(PlayerListS2CPacket.Entry entry) {
            Join.INSTANCE.entry = entry;
            return INSTANCE;
        }

        public PlayerListS2CPacket.Entry getEntry() {
            return this.entry;
        }
    }
}

