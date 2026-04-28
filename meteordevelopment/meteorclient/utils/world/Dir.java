/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Direction
 */
package meteordevelopment.meteorclient.utils.world;

import net.minecraft.util.math.Direction;

public class Dir {
    public static final byte UP = 2;
    public static final byte DOWN = 4;
    public static final byte NORTH = 8;
    public static final byte SOUTH = 16;
    public static final byte WEST = 32;
    public static final byte EAST = 64;

    private Dir() {
    }

    public static byte get(Direction dir) {
        return switch (dir) {
            default -> throw new MatchException(null, null);
            case Direction.UP -> 2;
            case Direction.DOWN -> 4;
            case Direction.NORTH -> 8;
            case Direction.SOUTH -> 16;
            case Direction.WEST -> 32;
            case Direction.EAST -> 64;
        };
    }

    public static boolean is(int dir, byte idk) {
        return (dir & idk) == idk;
    }

    public static boolean isNot(int dir, byte idk) {
        return (dir & idk) != idk;
    }
}

