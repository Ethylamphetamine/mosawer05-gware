/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package meteordevelopment.meteorclient.events.entity;

import net.minecraft.entity.Entity;

public class EntityAddedEvent {
    private static final EntityAddedEvent INSTANCE = new EntityAddedEvent();
    public Entity entity;

    public static EntityAddedEvent get(Entity entity) {
        EntityAddedEvent.INSTANCE.entity = entity;
        return INSTANCE;
    }
}

