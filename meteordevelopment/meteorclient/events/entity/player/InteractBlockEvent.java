/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 */
package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class InteractBlockEvent
extends Cancellable {
    private static final InteractBlockEvent INSTANCE = new InteractBlockEvent();
    public Hand hand;
    public BlockHitResult result;

    public static InteractBlockEvent get(Hand hand, BlockHitResult result) {
        INSTANCE.setCancelled(false);
        InteractBlockEvent.INSTANCE.hand = hand;
        InteractBlockEvent.INSTANCE.result = result;
        return INSTANCE;
    }
}

