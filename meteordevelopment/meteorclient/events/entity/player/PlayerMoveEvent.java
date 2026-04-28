/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.MovementType
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class PlayerMoveEvent {
    private static final PlayerMoveEvent INSTANCE = new PlayerMoveEvent();
    public MovementType type;
    public Vec3d movement;

    public static PlayerMoveEvent get(MovementType type, Vec3d movement) {
        PlayerMoveEvent.INSTANCE.type = type;
        PlayerMoveEvent.INSTANCE.movement = movement;
        return INSTANCE;
    }
}

