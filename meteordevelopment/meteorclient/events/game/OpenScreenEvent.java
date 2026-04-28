/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package meteordevelopment.meteorclient.events.game;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent
extends Cancellable {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();
    public Screen screen;

    public static OpenScreenEvent get(Screen screen) {
        INSTANCE.setCancelled(false);
        OpenScreenEvent.INSTANCE.screen = screen;
        return INSTANCE;
    }
}

