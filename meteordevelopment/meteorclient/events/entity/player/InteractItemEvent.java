/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class InteractItemEvent {
    private static final InteractItemEvent INSTANCE = new InteractItemEvent();
    public Hand hand;
    public ActionResult toReturn;

    public static InteractItemEvent get(Hand hand) {
        InteractItemEvent.INSTANCE.hand = hand;
        InteractItemEvent.INSTANCE.toReturn = null;
        return INSTANCE;
    }
}

