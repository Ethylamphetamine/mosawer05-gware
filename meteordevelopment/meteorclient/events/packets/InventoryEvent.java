/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.InventoryS2CPacket
 */
package meteordevelopment.meteorclient.events.packets;

import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;

public class InventoryEvent {
    private static final InventoryEvent INSTANCE = new InventoryEvent();
    public InventoryS2CPacket packet;

    public static InventoryEvent get(InventoryS2CPacket packet) {
        InventoryEvent.INSTANCE.packet = packet;
        return INSTANCE;
    }
}

