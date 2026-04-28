/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.item.BundleItem
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import net.minecraft.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BundleItem.class})
public abstract class BundleItemMixin {
    @ModifyExpressionValue(method={"getTooltipData"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", ordinal=0)})
    private boolean modifyContains1(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return (!bt.isActive() || bt.tooltip.get() == false) && original;
    }

    @ModifyExpressionValue(method={"getTooltipData"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", ordinal=1)})
    private boolean modifyContains2(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return (!bt.isActive() || bt.additional.get() == false) && original;
    }
}

