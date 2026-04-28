/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.LivingEntityRenderer
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.scoreboard.Team
 *  net.minecraft.util.Identifier
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.opengl.GL11
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private LivingEntity lastEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalHeadYaw;
    @Unique
    private float originalBodyYaw;
    @Unique
    private float originalPitch;
    @Unique
    private float originalPrevYaw;
    @Unique
    private float originalPrevHeadYaw;
    @Unique
    private float originalPrevBodyYaw;

    @Shadow
    @Nullable
    protected abstract RenderLayer getRenderLayer(T var1, boolean var2, boolean var3, boolean var4);

    @ModifyExpressionValue(method={"hasLabel(Lnet/minecraft/entity/LivingEntity;)Z"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;")})
    private Entity hasLabelGetCameraEntityProxy(Entity cameraEntity) {
        return Modules.get().isActive(Freecam.class) ? null : cameraEntity;
    }

    @ModifyVariable(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, ordinal=2, at=@At(value="STORE", ordinal=0))
    public float changeYaw(float oldValue, LivingEntity entity) {
        if (entity.equals((Object)MeteorClient.mc.player) && Rotations.rotationTimer < 10) {
            return Rotations.serverYaw;
        }
        return oldValue;
    }

    @ModifyVariable(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, ordinal=3, at=@At(value="STORE", ordinal=0))
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        if (entity.equals((Object)MeteorClient.mc.player) && Rotations.rotationTimer < 10) {
            return Rotations.serverYaw;
        }
        return oldValue;
    }

    @ModifyVariable(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, ordinal=5, at=@At(value="STORE", ordinal=3))
    public float changePitch(float oldValue, LivingEntity entity) {
        if (entity.equals((Object)MeteorClient.mc.player) && Rotations.rotationTimer < 10) {
            return Rotations.serverPitch;
        }
        return oldValue;
    }

    @ModifyExpressionValue(method={"hasLabel(Lnet/minecraft/entity/LivingEntity;)Z"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;getScoreboardTeam()Lnet/minecraft/scoreboard/Team;")})
    private Team hasLabelClientPlayerEntityGetScoreboardTeamProxy(Team team) {
        return MeteorClient.mc.player == null ? null : team;
    }

    @Inject(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderHead(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams chams;
        if (Modules.get().get(NoRender.class).noDeadEntities() && livingEntity.isDead()) {
            ci.cancel();
        }
        if ((chams = Modules.get().get(Chams.class)).isActive() && chams.shouldRender((Entity)livingEntity)) {
            GL11.glEnable((int)32823);
            GL11.glPolygonOffset((float)1.0f, (float)-1100000.0f);
        }
    }

    @Inject(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="TAIL")})
    private void renderTail(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams chams = Modules.get().get(Chams.class);
        if (chams.isActive() && chams.shouldRender((Entity)livingEntity)) {
            GL11.glPolygonOffset((float)1.0f, (float)1100000.0f);
            GL11.glDisable((int)32823);
        }
    }

    @ModifyArgs(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal=1))
    private void modifyScale(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Modules.get().get(Chams.class);
        if (!(module.isActive() && module.players.get().booleanValue() && livingEntity instanceof PlayerEntity)) {
            return;
        }
        if (module.ignoreSelf.get().booleanValue() && livingEntity == MeteorClient.mc.player) {
            return;
        }
        args.set(0, (Object)Float.valueOf(-module.playersScale.get().floatValue()));
        args.set(1, (Object)Float.valueOf(-module.playersScale.get().floatValue()));
        args.set(2, (Object)Float.valueOf(module.playersScale.get().floatValue()));
    }

    @ModifyArgs(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void modifyColor(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Modules.get().get(Chams.class);
        if (!(module.isActive() && module.players.get().booleanValue() && livingEntity instanceof PlayerEntity)) {
            return;
        }
        if (module.ignoreSelf.get().booleanValue() && livingEntity == MeteorClient.mc.player) {
            return;
        }
        Color color = PlayerUtils.getPlayerColor((PlayerEntity)livingEntity, module.playersColor.get());
        args.set(4, (Object)color.getPacked());
    }

    @Redirect(method={"render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer getRenderLayer(LivingEntityRenderer<T, M> livingEntityRenderer, T livingEntity, boolean showBody, boolean translucent, boolean showOutline) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.players.get().booleanValue() || !(livingEntity instanceof PlayerEntity) || module.playersTexture.get().booleanValue()) {
            return this.getRenderLayer(livingEntity, showBody, translucent, showOutline);
        }
        if (module.ignoreSelf.get().booleanValue() && livingEntity == MeteorClient.mc.player) {
            return this.getRenderLayer(livingEntity, showBody, translucent, showOutline);
        }
        return RenderLayer.getItemEntityTranslucentCull((Identifier)Chams.BLANK);
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MeteorClient.mc.player != null && livingEntity == MeteorClient.mc.player) {
            this.originalYaw = livingEntity.getYaw();
            this.originalHeadYaw = ((LivingEntity)livingEntity).headYaw;
            this.originalBodyYaw = ((LivingEntity)livingEntity).bodyYaw;
            this.originalPitch = livingEntity.getPitch();
            this.originalPrevYaw = ((LivingEntity)livingEntity).prevYaw;
            this.originalPrevHeadYaw = ((LivingEntity)livingEntity).prevHeadYaw;
            this.originalPrevBodyYaw = ((LivingEntity)livingEntity).prevBodyYaw;
            livingEntity.setYaw(RotationManager.getRenderYawOffset());
            ((LivingEntity)livingEntity).headYaw = RotationManager.getRotationYawHead();
            ((LivingEntity)livingEntity).bodyYaw = RotationManager.getRenderYawOffset();
            livingEntity.setPitch(RotationManager.getRenderPitch());
            ((LivingEntity)livingEntity).prevYaw = RotationManager.getPrevRenderYawOffset();
            ((LivingEntity)livingEntity).prevHeadYaw = RotationManager.getPrevRotationYawHead();
            ((LivingEntity)livingEntity).prevBodyYaw = RotationManager.getPrevRenderYawOffset();
            ((LivingEntity)livingEntity).prevPitch = RotationManager.getPrevPitch();
        }
        this.lastEntity = livingEntity;
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MeteorClient.mc.player != null && livingEntity == MeteorClient.mc.player) {
            livingEntity.setYaw(this.originalYaw);
            ((LivingEntity)livingEntity).headYaw = this.originalHeadYaw;
            ((LivingEntity)livingEntity).bodyYaw = this.originalBodyYaw;
            livingEntity.setPitch(this.originalPitch);
            ((LivingEntity)livingEntity).prevYaw = this.originalPrevYaw;
            ((LivingEntity)livingEntity).prevHeadYaw = this.originalPrevHeadYaw;
            ((LivingEntity)livingEntity).prevBodyYaw = this.originalPrevBodyYaw;
            ((LivingEntity)livingEntity).prevPitch = this.originalPitch;
        }
    }
}

