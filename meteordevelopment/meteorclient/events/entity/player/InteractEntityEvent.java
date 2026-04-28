/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

public class InteractEntityEvent
extends Cancellable {
    private static final InteractEntityEvent INSTANCE = new InteractEntityEvent();
    public Entity entity;
    public Hand hand;

    public static InteractEntityEvent get(Entity entity, Hand hand) {
        INSTANCE.setCancelled(false);
        InteractEntityEvent.INSTANCE.entity = entity;
        InteractEntityEvent.INSTANCE.hand = hand;
        return INSTANCE;
    }
}

