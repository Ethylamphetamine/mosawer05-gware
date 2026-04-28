/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.events.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class LivingEntityMoveEvent {
    private static final LivingEntityMoveEvent INSTANCE = new LivingEntityMoveEvent();
    public LivingEntity entity;
    public Vec3d movement;

    public static LivingEntityMoveEvent get(LivingEntity entity, Vec3d movement) {
        LivingEntityMoveEvent.INSTANCE.entity = entity;
        LivingEntityMoveEvent.INSTANCE.movement = movement;
        return INSTANCE;
    }
}

