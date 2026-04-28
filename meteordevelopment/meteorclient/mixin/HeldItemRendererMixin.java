/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.item.HeldItemRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.Arm
 *  net.minecraft.util.Hand
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import com.google.common.base.MoreObjects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.ArmRenderEvent;
import meteordevelopment.meteorclient.events.render.HeldItemRendererEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.FakeItem;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={HeldItemRenderer.class})
public abstract class HeldItemRendererMixin {
    @Shadow
    private float equipProgressMainHand;
    @Shadow
    private float equipProgressOffHand;
    @Shadow
    private ItemStack mainHand;
    @Shadow
    private ItemStack offHand;

    @ModifyVariable(method={"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at=@At(value="STORE", ordinal=0), index=6)
    private float modifySwing(float swingProgress) {
        HandView module = Modules.get().get(HandView.class);
        Hand hand = (Hand)MoreObjects.firstNonNull((Object)MeteorClient.mc.player.preferredHand, (Object)Hand.MAIN_HAND);
        if (module.isActive()) {
            if (hand == Hand.OFF_HAND && !MeteorClient.mc.player.getOffHandStack().isEmpty()) {
                return swingProgress + module.offSwing.get().floatValue();
            }
            if (hand == Hand.MAIN_HAND && !MeteorClient.mc.player.getMainHandStack().isEmpty()) {
                return swingProgress + module.mainSwing.get().floatValue();
            }
        }
        return swingProgress;
    }

    @Redirect(method={"updateHeldItems"}, at=@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;areEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean redirectSwapping(ItemStack left, ItemStack right) {
        return this.showSwapping(left, right);
    }

    @ModifyArg(method={"updateHeldItems"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal=2), index=0)
    private float modifyEquipProgressMainhand(float value) {
        float f = MeteorClient.mc.player.getAttackCooldownProgress(1.0f);
        float modified = Modules.get().get(HandView.class).oldAnimations() ? 1.0f : f * f * f;
        return (this.showSwapping(this.mainHand, MeteorClient.mc.player.getMainHandStack()) ? modified : 0.0f) - this.equipProgressMainHand;
    }

    @ModifyArg(method={"updateHeldItems"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal=3), index=0)
    private float modifyEquipProgressOffhand(float value) {
        return (float)(this.showSwapping(this.offHand, MeteorClient.mc.player.getOffHandStack()) ? 1 : 0) - this.equipProgressOffHand;
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")})
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(HeldItemRendererEvent.get(hand, matrices));
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V")})
    private void onRenderArm(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(ArmRenderEvent.get(hand, matrices));
    }

    @Inject(method={"applyEatOrDrinkTransformation"}, at={@At(value="INVOKE", target="Ljava/lang/Math;pow(DD)D", shift=At.Shift.BEFORE)}, cancellable=true)
    private void cancelTransformations(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        if (Modules.get().get(HandView.class).disableFoodAnimation()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method={"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void modifyRenderItemArgs(Args args) {
        ItemStack currentStack;
        ClientPlayerEntity player;
        ItemStack replacement;
        Object playerObj;
        ItemStack silentStack;
        ItemStack crystalStack;
        SilentMine silentMine = Modules.get().get(SilentMine.class);
        AutoCrystal autoCrystal = Modules.get().get(AutoCrystal.class);
        Hand hand = (Hand)args.get(3);
        if (autoCrystal != null && autoCrystal.isActive() && hand == Hand.MAIN_HAND && !(crystalStack = autoCrystal.getRenderStack()).isEmpty()) {
            args.set(5, (Object)crystalStack);
            return;
        }
        if (silentMine != null && silentMine.isActive() && hand == Hand.MAIN_HAND && !(silentStack = silentMine.getRenderStack()).isEmpty()) {
            args.set(5, (Object)silentStack);
            return;
        }
        FakeItem module = Modules.get().get(FakeItem.class);
        if (module.isActive() && (playerObj = args.get(0)) instanceof ClientPlayerEntity && !(replacement = module.getRenderStack(hand, player = (ClientPlayerEntity)playerObj, currentStack = (ItemStack)args.get(5))).isEmpty()) {
            args.set(5, (Object)replacement);
        }
    }

    @Unique
    private boolean showSwapping(ItemStack stack1, ItemStack stack2) {
        return !Modules.get().get(HandView.class).showSwapping() || ItemStack.areEqual((ItemStack)stack1, (ItemStack)stack2);
    }
}

