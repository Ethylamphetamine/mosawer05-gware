/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 */
package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.item.ItemStack;

public class FinishUsingItemEvent {
    private static final FinishUsingItemEvent INSTANCE = new FinishUsingItemEvent();
    public ItemStack itemStack;

    public static FinishUsingItemEvent get(ItemStack itemStack) {
        FinishUsingItemEvent.INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}

