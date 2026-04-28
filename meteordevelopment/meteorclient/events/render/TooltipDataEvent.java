/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.tooltip.TooltipData
 */
package meteordevelopment.meteorclient.events.render;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;

public class TooltipDataEvent {
    private static final TooltipDataEvent INSTANCE = new TooltipDataEvent();
    public TooltipData tooltipData;
    public ItemStack itemStack;

    public static TooltipDataEvent get(ItemStack itemStack) {
        TooltipDataEvent.INSTANCE.tooltipData = null;
        TooltipDataEvent.INSTANCE.itemStack = itemStack;
        return INSTANCE;
    }
}

