/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 */
package meteordevelopment.meteorclient.events.entity;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.item.ItemStack;

public class DropItemsEvent
extends Cancellable {
    private static final DropItemsEvent INSTANCE = new DropItemsEvent();
    public ItemStack itemStack;

    public static DropItemsEvent get(ItemStack itemStack) {
        INSTANCE.setCancelled(false);
        DropItemsEvent.INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}

