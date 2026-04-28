/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.LocalCapture
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.LiquidInteract;
import meteordevelopment.meteorclient.systems.modules.player.NoMiningTrace;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.render.Zoom;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    @Shadow
    @Final
    MinecraftClient client;
    @Shadow
    @Final
    private Camera camera;
    @Unique
    private Renderer3D renderer;
    @Unique
    private final MatrixStack matrices = new MatrixStack();
    @Unique
    private boolean freecamSet = false;

    @Shadow
    public abstract void updateCrosshairTarget(float var1);

    @Shadow
    public abstract void reset();

    @Shadow
    protected abstract void bobView(MatrixStack var1, float var2);

    @Shadow
    protected abstract void tiltViewWhenHurt(MatrixStack var1, float var2);

    @Inject(method={"renderWorld"}, at={@At(value="INVOKE_STRING", target="Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args={"ldc=hand"})}, locals=LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal=1) Matrix4f matrix4f2, @Local(ordinal=1) float tickDelta, @Local MatrixStack matrixStack) {
        if (!Utils.canUpdate()) {
            return;
        }
        this.client.getProfiler().push("meteor-client_render");
        if (this.renderer == null) {
            this.renderer = new Renderer3D();
        }
        Render3DEvent event = Render3DEvent.get(matrixStack, this.renderer, tickDelta, this.camera.getPos().x, this.camera.getPos().y, this.camera.getPos().z);
        RenderUtils.updateScreenCenter();
        NametagUtils.onRender(matrix4f2);
        RenderSystem.getModelViewStack().pushMatrix().mul((Matrix4fc)matrix4f2);
        this.matrices.push();
        this.tiltViewWhenHurt(this.matrices, this.camera.getLastTickDelta());
        if (((Boolean)this.client.options.getBobView().getValue()).booleanValue()) {
            this.bobView(this.matrices, this.camera.getLastTickDelta());
        }
        RenderSystem.getModelViewStack().mul((Matrix4fc)this.matrices.peek().getPositionMatrix().invert());
        this.matrices.pop();
        RenderSystem.applyModelViewMatrix();
        this.renderer.begin();
        MeteorClient.EVENT_BUS.post(event);
        this.renderer.render(matrixStack);
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
        this.client.getProfiler().pop();
    }

    @Inject(method={"renderWorld"}, at={@At(value="TAIL")})
    private void onRenderWorldTail(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(RenderAfterWorldEvent.get());
    }

    @ModifyReturnValue(method={"findCrosshairTarget"}, at={@At(value="RETURN")})
    private HitResult onUpdateTargetedEntity(HitResult original, @Local HitResult hitResult) {
        Entity entity;
        if (original instanceof EntityHitResult) {
            EntityHitResult ehr = (EntityHitResult)original;
            entity = ehr.getEntity();
        } else {
            entity = null;
        }
        if (Modules.get().get(NoMiningTrace.class).canWork(entity) && hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult;
        }
        return original;
    }

    @Redirect(method={"findCrosshairTarget"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult updateTargetedEntityEntityRayTraceProxy(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
        if (Modules.get().isActive(LiquidInteract.class)) {
            HitResult result = entity.raycast(maxDistance, tickDelta, includeFluids);
            if (result.getType() != HitResult.Type.MISS) {
                return result;
            }
            return entity.raycast(maxDistance, tickDelta, true);
        }
        return entity.raycast(maxDistance, tickDelta, includeFluids);
    }

    @Inject(method={"showFloatingItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && Modules.get().get(NoRender.class).noTotemAnimation()) {
            info.cancel();
        }
    }

    @ModifyExpressionValue(method={"renderWorld"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerp(FFF)F")})
    private float applyCameraTransformationsMathHelperLerpProxy(float original) {
        return Modules.get().get(NoRender.class).noNausea() ? 0.0f : original;
    }

    @Inject(method={"renderNausea"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderNausea(DrawContext context, float distortionStrength, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noNausea()) {
            ci.cancel();
        }
    }

    @Inject(method={"updateCrosshairTarget"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        Freecam freecam = Modules.get().get(Freecam.class);
        boolean highwayBuilder = Modules.get().isActive(HighwayBuilder.class);
        if ((freecam.isActive() || highwayBuilder) && this.client.getCameraEntity() != null && !this.freecamSet) {
            info.cancel();
            Entity cameraE = this.client.getCameraEntity();
            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double prevX = cameraE.prevX;
            double prevY = cameraE.prevY;
            double prevZ = cameraE.prevZ;
            float yaw = cameraE.getYaw();
            float pitch = cameraE.getPitch();
            float prevYaw = cameraE.prevYaw;
            float prevPitch = cameraE.prevPitch;
            if (highwayBuilder) {
                cameraE.setYaw(this.camera.getYaw());
                cameraE.setPitch(this.camera.getPitch());
            } else {
                ((IVec3d)cameraE.getPos()).set(freecam.pos.x, freecam.pos.y - (double)cameraE.getEyeHeight(cameraE.getPose()), freecam.pos.z);
                cameraE.prevX = freecam.prevPos.x;
                cameraE.prevY = freecam.prevPos.y - (double)cameraE.getEyeHeight(cameraE.getPose());
                cameraE.prevZ = freecam.prevPos.z;
                cameraE.setYaw(freecam.yaw);
                cameraE.setPitch(freecam.pitch);
                cameraE.prevYaw = freecam.prevYaw;
                cameraE.prevPitch = freecam.prevPitch;
            }
            this.freecamSet = true;
            this.updateCrosshairTarget(tickDelta);
            this.freecamSet = false;
            ((IVec3d)cameraE.getPos()).set(x, y, z);
            cameraE.prevX = prevX;
            cameraE.prevY = prevY;
            cameraE.prevZ = prevZ;
            cameraE.setYaw(yaw);
            cameraE.setPitch(pitch);
            cameraE.prevYaw = prevYaw;
            cameraE.prevPitch = prevPitch;
        }
    }

    @Inject(method={"renderHand"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        if (!Modules.get().get(Freecam.class).renderHands() || !Modules.get().get(Zoom.class).renderHands()) {
            ci.cancel();
        }
    }
}

