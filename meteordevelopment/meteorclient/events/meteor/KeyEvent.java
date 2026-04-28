/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.events.Cancellable;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;

public class KeyEvent
extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();
    public int key;
    public int modifiers;
    public KeyAction action;

    public static KeyEvent get(int key, int modifiers, KeyAction action) {
        INSTANCE.setCancelled(false);
        KeyEvent.INSTANCE.key = key;
        KeyEvent.INSTANCE.modifiers = modifiers;
        KeyEvent.INSTANCE.action = action;
        return INSTANCE;
    }
}

