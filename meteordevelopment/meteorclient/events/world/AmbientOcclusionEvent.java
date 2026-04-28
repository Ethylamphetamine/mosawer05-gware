/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.world;

public class AmbientOcclusionEvent {
    private static final AmbientOcclusionEvent INSTANCE = new AmbientOcclusionEvent();
    public float lightLevel = -1.0f;

    public static AmbientOcclusionEvent get() {
        AmbientOcclusionEvent.INSTANCE.lightLevel = -1.0f;
        return INSTANCE;
    }
}

