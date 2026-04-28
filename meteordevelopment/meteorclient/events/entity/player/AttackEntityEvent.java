/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.entity.Entity;

public class AttackEntityEvent
extends Cancellable {
    private static final AttackEntityEvent INSTANCE = new AttackEntityEvent();
    public Entity entity;

    public static AttackEntityEvent get(Entity entity) {
        INSTANCE.setCancelled(false);
        AttackEntityEvent.INSTANCE.entity = entity;
        return INSTANCE;
    }
}

