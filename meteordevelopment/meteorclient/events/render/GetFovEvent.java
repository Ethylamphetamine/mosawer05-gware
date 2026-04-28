/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.render;

public class GetFovEvent {
    private static final GetFovEvent INSTANCE = new GetFovEvent();
    public double fov;

    public static GetFovEvent get(double fov) {
        GetFovEvent.INSTANCE.fov = fov;
        return INSTANCE;
    }
}

