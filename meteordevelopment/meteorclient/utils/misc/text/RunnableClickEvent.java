/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.misc.text;

import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;

public class RunnableClickEvent
extends MeteorClickEvent {
    public final Runnable runnable;

    public RunnableClickEvent(Runnable runnable) {
        super(null, null);
        this.runnable = runnable;
    }
}

