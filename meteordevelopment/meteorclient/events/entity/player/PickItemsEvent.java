/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 */
package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.item.ItemStack;

public class PickItemsEvent {
    private static final PickItemsEvent INSTANCE = new PickItemsEvent();
    public ItemStack itemStack;
    public int count;

    public static PickItemsEvent get(ItemStack itemStack, int count) {
        PickItemsEvent.INSTANCE.itemStack = itemStack;
        PickItemsEvent.INSTANCE.count = count;
        return INSTANCE;
    }
}

