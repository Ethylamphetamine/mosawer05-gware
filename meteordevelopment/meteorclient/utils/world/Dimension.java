/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.world;

public enum Dimension {
    Overworld,
    Nether,
    End;


    public Dimension opposite() {
        return switch (this.ordinal()) {
            case 0 -> Nether;
            case 1 -> Overworld;
            default -> this;
        };
    }
}

