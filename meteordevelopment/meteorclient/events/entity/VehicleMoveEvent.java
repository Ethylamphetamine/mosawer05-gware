/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket
 */
package meteordevelopment.meteorclient.events.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

public class VehicleMoveEvent {
    private static final VehicleMoveEvent INSTANCE = new VehicleMoveEvent();
    public Entity entity;
    public VehicleMoveC2SPacket packet;

    public static VehicleMoveEvent get(VehicleMoveC2SPacket packet, Entity entity) {
        VehicleMoveEvent.INSTANCE.entity = entity;
        VehicleMoveEvent.INSTANCE.packet = packet;
        return INSTANCE;
    }
}

