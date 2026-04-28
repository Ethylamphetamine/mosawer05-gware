/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;

public class MouseScrollEvent
extends Cancellable {
    private static final MouseScrollEvent INSTANCE = new MouseScrollEvent();
    public double value;

    public static MouseScrollEvent get(double value) {
        INSTANCE.setCancelled(false);
        MouseScrollEvent.INSTANCE.value = value;
        return INSTANCE;
    }
}

