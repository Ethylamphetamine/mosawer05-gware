/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.screen.PlayerScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.world.BlockView
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.player.BreakDelay;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientPlayerInteractionManager.class})
public abstract class ClientPlayerInteractionManagerMixin
implements IClientPlayerInteractionManager {
    @Shadow
    private int blockBreakingCooldown;
    @Shadow
    @Final
    private ClientPlayNetworkHandler networkHandler;

    @Shadow
    protected abstract void syncSelectedSlot();

    @Shadow
    public abstract void clickSlot(int var1, int var2, int var3, SlotActionType var4, PlayerEntity var5);

    @Shadow
    public abstract boolean breakBlock(BlockPos var1);

    @Inject(method={"clickSlot"}, at={@At(value="HEAD")}, cancellable=true)
    private void onClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo info) {
        if (actionType == SlotActionType.THROW && slotId >= 0 && slotId < player.currentScreenHandler.slots.size()) {
            if (MeteorClient.EVENT_BUS.post(DropItemsEvent.get(((Slot)player.currentScreenHandler.slots.get(slotId)).getStack())).isCancelled()) {
                info.cancel();
            }
        } else if (slotId == -999 && MeteorClient.EVENT_BUS.post(DropItemsEvent.get(player.currentScreenHandler.getCursorStack())).isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"clickSlot"}, at={@At(value="HEAD")}, cancellable=true)
    public void onClickArmorSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!Modules.get().get(InventoryTweaks.class).armorStorage()) {
            return;
        }
        ScreenHandler screenHandler = player.currentScreenHandler;
        if (screenHandler instanceof PlayerScreenHandler && slotId >= 5 && slotId <= 8) {
            int armorSlot = 8 - slotId + 36;
            if (actionType == SlotActionType.PICKUP && !screenHandler.getCursorStack().isEmpty()) {
                this.clickSlot(syncId, 17, armorSlot, SlotActionType.SWAP, player);
                this.clickSlot(syncId, 17, button, SlotActionType.PICKUP, player);
                this.clickSlot(syncId, 17, armorSlot, SlotActionType.SWAP, player);
                ci.cancel();
            } else if (actionType == SlotActionType.SWAP) {
                if (button >= 10) {
                    this.clickSlot(syncId, 45, armorSlot, SlotActionType.SWAP, player);
                    ci.cancel();
                } else {
                    this.clickSlot(syncId, 36 + button, armorSlot, SlotActionType.SWAP, player);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method={"attackBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.EVENT_BUS.post(StartBreakingBlockEvent.get(blockPos, direction)).isCancelled()) {
            info.cancel();
        } else {
            SpeedMine sm = Modules.get().get(SpeedMine.class);
            BlockState state = MeteorClient.mc.world.getBlockState(blockPos);
            if (!sm.instamine() || !sm.filter(state.getBlock())) {
                return;
            }
            if (state.calcBlockBreakingDelta((PlayerEntity)MeteorClient.mc.player, (BlockView)MeteorClient.mc.world, blockPos) > 0.5f) {
                this.breakBlock(blockPos);
                this.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
                this.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                info.setReturnValue((Object)true);
            }
        }
    }

    @Inject(method={"interactBlock"}, at={@At(value="HEAD")}, cancellable=true)
    public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (MeteorClient.EVENT_BUS.post(InteractBlockEvent.get(player.getMainHandStack().isEmpty() ? Hand.OFF_HAND : hand, hitResult)).isCancelled()) {
            cir.setReturnValue((Object)ActionResult.FAIL);
        }
    }

    @Inject(method={"attackEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo info) {
        if (MeteorClient.EVENT_BUS.post(AttackEntityEvent.get(target)).isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"interactEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if (MeteorClient.EVENT_BUS.post(InteractEntityEvent.get(entity, hand)).isCancelled()) {
            info.setReturnValue((Object)ActionResult.FAIL);
        }
    }

    @Inject(method={"dropCreativeStack"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDropCreativeStack(ItemStack stack, CallbackInfo info) {
        if (MeteorClient.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled()) {
            info.cancel();
        }
    }

    @Redirect(method={"updateBlockBreakingProgress"}, at=@At(value="FIELD", target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode=181, ordinal=1))
    private void creativeBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        this.blockBreakingCooldown = event.cooldown;
    }

    @Redirect(method={"updateBlockBreakingProgress"}, at=@At(value="FIELD", target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode=181, ordinal=2))
    private void survivalBreakDelayChange(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        this.blockBreakingCooldown = event.cooldown;
    }

    @Redirect(method={"attackBlock"}, at=@At(value="FIELD", target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode=181))
    private void creativeBreakDelayChange2(ClientPlayerInteractionManager interactionManager, int value) {
        BlockBreakingCooldownEvent event = MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(value));
        this.blockBreakingCooldown = event.cooldown;
    }

    @Redirect(method={"method_41930"}, at=@At(value="INVOKE", target="Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    private float deltaChange(BlockState blockState, PlayerEntity player, BlockView world, BlockPos pos) {
        float delta = blockState.calcBlockBreakingDelta(player, world, pos);
        if (Modules.get().get(BreakDelay.class).preventInstaBreak() && delta >= 1.0f) {
            BlockBreakingCooldownEvent event = MeteorClient.EVENT_BUS.post(BlockBreakingCooldownEvent.get(this.blockBreakingCooldown));
            this.blockBreakingCooldown = event.cooldown;
            return 0.0f;
        }
        return delta;
    }

    @Inject(method={"breakBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onBreakBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.EVENT_BUS.post(BreakBlockEvent.get(blockPos)).isCancelled()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"interactItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        InteractItemEvent event = MeteorClient.EVENT_BUS.post(InteractItemEvent.get(hand));
        if (event.toReturn != null) {
            info.setReturnValue((Object)event.toReturn);
        }
    }

    @Inject(method={"cancelBlockBreaking"}, at={@At(value="HEAD")}, cancellable=true)
    private void onCancelBlockBreaking(CallbackInfo info) {
        if (BlockUtils.breaking) {
            info.cancel();
        }
    }

    @Override
    public void meteor$syncSelected() {
        this.syncSelectedSlot();
    }
}

