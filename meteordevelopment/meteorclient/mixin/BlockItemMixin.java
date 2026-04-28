/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.ItemPlacementContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.NoGhostBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockItem.class})
public abstract class BlockItemMixin {
    @Shadow
    protected abstract BlockState getPlacementState(ItemPlacementContext var1);

    @Inject(method={"place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (!context.getWorld().isClient) {
            return;
        }
        if (MeteorClient.EVENT_BUS.post(PlaceBlockEvent.get(context.getBlockPos(), state.getBlock())).isCancelled()) {
            info.setReturnValue((Object)true);
        }
    }

    @ModifyVariable(method={"place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;"}, ordinal=1, at=@At(value="INVOKE", target="Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private BlockState modifyState(BlockState state, ItemPlacementContext context) {
        NoGhostBlocks noGhostBlocks = Modules.get().get(NoGhostBlocks.class);
        if (noGhostBlocks.isActive() && noGhostBlocks.placing.get().booleanValue()) {
            return this.getPlacementState(context);
        }
        return state;
    }
}

