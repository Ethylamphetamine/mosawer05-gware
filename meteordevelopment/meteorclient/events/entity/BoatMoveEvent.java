/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.vehicle.BoatEntity
 */
package meteordevelopment.meteorclient.events.entity;

import net.minecraft.entity.vehicle.BoatEntity;

public class BoatMoveEvent {
    private static final BoatMoveEvent INSTANCE = new BoatMoveEvent();
    public BoatEntity boat;

    public static BoatMoveEvent get(BoatEntity entity) {
        BoatMoveEvent.INSTANCE.boat = entity;
        return INSTANCE;
    }
}

