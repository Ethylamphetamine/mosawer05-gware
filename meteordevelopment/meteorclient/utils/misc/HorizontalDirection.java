/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.misc;

public enum HorizontalDirection {
    South("South", "Z+", false, 0.0f, 0, 1),
    SouthEast("South East", "X+ Z+", true, -45.0f, 1, 1),
    West("West", "X-", false, 90.0f, -1, 0),
    NorthWest("North West", "X- Z-", true, 135.0f, -1, -1),
    North("North", "Z-", false, 180.0f, 0, -1),
    NorthEast("North East", "X+ Z-", true, -135.0f, 1, -1),
    East("East", "X+", false, -90.0f, 1, 0),
    SouthWest("South West", "X- Z+", true, 45.0f, -1, 1);

    public final String name;
    public final String axis;
    public final boolean diagonal;
    public final float yaw;
    public final int offsetX;
    public final int offsetZ;

    private HorizontalDirection(String name, String axis, boolean diagonal, float yaw, int offsetX, int offsetZ) {
        this.axis = axis;
        this.name = name;
        this.diagonal = diagonal;
        this.yaw = yaw;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public HorizontalDirection opposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> North;
            case 1 -> NorthWest;
            case 2 -> East;
            case 3 -> SouthEast;
            case 4 -> South;
            case 5 -> SouthWest;
            case 6 -> West;
            case 7 -> NorthEast;
        };
    }

    public HorizontalDirection rotateLeft() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> SouthEast;
            case 1 -> East;
            case 6 -> NorthEast;
            case 5 -> North;
            case 4 -> NorthWest;
            case 3 -> West;
            case 2 -> SouthWest;
            case 7 -> South;
        };
    }

    public HorizontalDirection rotateLeftSkipOne() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> East;
            case 6 -> North;
            case 4 -> West;
            case 2 -> South;
            case 1 -> NorthEast;
            case 5 -> NorthWest;
            case 3 -> SouthWest;
            case 7 -> SouthEast;
        };
    }

    public HorizontalDirection rotateRight() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> SouthWest;
            case 7 -> West;
            case 2 -> NorthWest;
            case 3 -> North;
            case 4 -> NorthEast;
            case 5 -> East;
            case 6 -> SouthEast;
            case 1 -> South;
        };
    }

    public static HorizontalDirection get(float yaw) {
        if ((yaw %= 360.0f) < 0.0f) {
            yaw += 360.0f;
        }
        if ((double)yaw >= 337.5 || (double)yaw < 22.5) {
            return South;
        }
        if ((double)yaw >= 22.5 && (double)yaw < 67.5) {
            return SouthWest;
        }
        if ((double)yaw >= 67.5 && (double)yaw < 112.5) {
            return West;
        }
        if ((double)yaw >= 112.5 && (double)yaw < 157.5) {
            return NorthWest;
        }
        if ((double)yaw >= 157.5 && (double)yaw < 202.5) {
            return North;
        }
        if ((double)yaw >= 202.5 && (double)yaw < 247.5) {
            return NorthEast;
        }
        if ((double)yaw >= 247.5 && (double)yaw < 292.5) {
            return East;
        }
        if ((double)yaw >= 292.5 && (double)yaw < 337.5) {
            return SouthEast;
        }
        return South;
    }
}

