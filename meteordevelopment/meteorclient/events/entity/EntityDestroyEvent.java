/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package meteordevelopment.meteorclient.events.entity;

import net.minecraft.entity.Entity;

public class EntityDestroyEvent {
    private static final EntityDestroyEvent INSTANCE = new EntityDestroyEvent();
    public Entity entity;

    public static EntityDestroyEvent get(Entity entity) {
        EntityDestroyEvent.INSTANCE.entity = entity;
        return INSTANCE;
    }
}

