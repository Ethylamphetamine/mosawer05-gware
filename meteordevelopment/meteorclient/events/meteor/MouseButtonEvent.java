/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;

public class MouseButtonEvent
extends Cancellable {
    private static final MouseButtonEvent INSTANCE = new MouseButtonEvent();
    public int button;
    public KeyAction action;

    public static MouseButtonEvent get(int button, KeyAction action) {
        INSTANCE.setCancelled(false);
        MouseButtonEvent.INSTANCE.button = button;
        MouseButtonEvent.INSTANCE.action = action;
        return INSTANCE;
    }
}

