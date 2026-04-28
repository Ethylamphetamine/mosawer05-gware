/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.renderer;

public enum DrawMode {
    Lines(2),
    Triangles(3);

    public final int indicesCount;

    private DrawMode(int indicesCount) {
        this.indicesCount = indicesCount;
    }

    public int getGL() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 1;
            case 1 -> 4;
        };
    }
}

