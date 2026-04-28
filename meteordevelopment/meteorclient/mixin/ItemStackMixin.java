/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.text.Text
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.game.ItemStackTooltipEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public abstract class ItemStackMixin {
    @ModifyReturnValue(method={"getTooltip"}, at={@At(value="RETURN")})
    private List<Text> onGetTooltip(List<Text> original) {
        if (Utils.canUpdate()) {
            ItemStackTooltipEvent event = MeteorClient.EVENT_BUS.post(new ItemStackTooltipEvent((ItemStack)this, original));
            return event.list();
        }
        return original;
    }

    @ModifyExpressionValue(method={"getTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/BlockPredicatesChecker;showInTooltip()Z", ordinal=0)})
    private boolean modifyCanBreakText(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return bt.isActive() && bt.canDestroy.get() != false || original;
    }

    @ModifyExpressionValue(method={"getTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/BlockPredicatesChecker;showInTooltip()Z", ordinal=1)})
    private boolean modifyCanPlaceText(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return bt.isActive() && bt.canPlaceOn.get() != false || original;
    }

    @ModifyExpressionValue(method={"getTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", ordinal=0)})
    private boolean modifyContainsTooltip(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return (!bt.isActive() || bt.tooltip.get() == false) && original;
    }

    @ModifyExpressionValue(method={"getTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;contains(Lnet/minecraft/component/ComponentType;)Z", ordinal=3)})
    private boolean modifyContainsAdditional(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return (!bt.isActive() || bt.additional.get() == false) && original;
    }

    @Inject(method={"finishUsing"}, at={@At(value="HEAD")})
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (user == MeteorClient.mc.player) {
            MeteorClient.EVENT_BUS.post(FinishUsingItemEvent.get((ItemStack)this));
        }
    }

    @Inject(method={"onStoppedUsing"}, at={@At(value="HEAD")})
    private void onStoppedUsing(World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        if (user == MeteorClient.mc.player) {
            MeteorClient.EVENT_BUS.post(StoppedUsingItemEvent.get((ItemStack)this));
        }
    }

    @ModifyExpressionValue(method={"appendAttributeModifiersTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/component/type/AttributeModifiersComponent;showInTooltip()Z")})
    private boolean modifyShowInTooltip(boolean original) {
        BetterTooltips bt = Modules.get().get(BetterTooltips.class);
        return bt.isActive() && bt.modifiers.get() != false || original;
    }
}

