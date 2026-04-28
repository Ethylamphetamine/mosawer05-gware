/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.EndCrystalEntityRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.MathHelper
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={EndCrystalEntityRenderer.class})
public abstract class EndCrystalEntityRendererMixin {
    @Mutable
    @Shadow
    @Final
    private static RenderLayer END_CRYSTAL;
    @Shadow
    @Final
    private static Identifier TEXTURE;
    @Shadow
    @Final
    public ModelPart core;
    @Shadow
    @Final
    public ModelPart frame;

    @Inject(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")})
    private void render(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams module = Modules.get().get(Chams.class);
        END_CRYSTAL = RenderLayer.getEntityTranslucent((Identifier)(module.isActive() && module.crystals.get() != false && module.crystalsTexture.get() == false ? Chams.BLANK : TEXTURE));
    }

    @ModifyArgs(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal=0))
    private void modifyScale(Args args) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            return;
        }
        args.set(0, (Object)Float.valueOf(2.0f * module.crystalsScale.get().floatValue()));
        args.set(1, (Object)Float.valueOf(2.0f * module.crystalsScale.get().floatValue()));
        args.set(2, (Object)Float.valueOf(2.0f * module.crystalsScale.get().floatValue()));
    }

    @Redirect(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/EndCrystalEntityRenderer;getYOffset(Lnet/minecraft/entity/decoration/EndCrystalEntity;F)F"))
    private float getYOff(EndCrystalEntity crystal, float tickDelta) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            return EndCrystalEntityRenderer.getYOffset((EndCrystalEntity)crystal, (float)tickDelta);
        }
        float f = (float)crystal.endCrystalAge + tickDelta;
        float g = MathHelper.sin((float)(f * 0.2f)) / 2.0f + 0.5f;
        g = (g * g + g) * 0.4f * module.crystalsBounce.get().floatValue();
        return g - 1.4f;
    }

    @ModifyArgs(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"))
    private void modifySpeed(Args args) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            return;
        }
        args.set(0, (Object)Float.valueOf(((Float)args.get(0)).floatValue() * module.crystalsRotationSpeed.get().floatValue()));
    }

    @Redirect(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal=3))
    private void modifyCore(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            this.core.render(matrices, vertices, light, overlay);
            return;
        }
        if (module.renderCore.get().booleanValue()) {
            Color color = module.crystalsCoreColor.get();
            this.core.render(matrices, vertices, light, overlay, color.getPacked());
        }
    }

    @Redirect(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal=1))
    private void modifyFrame1(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            this.frame.render(matrices, vertices, light, overlay);
            return;
        }
        if (module.renderFrame1.get().booleanValue()) {
            Color color = module.crystalsFrame1Color.get();
            this.frame.render(matrices, vertices, light, overlay, color.getPacked());
        }
    }

    @Redirect(method={"render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal=2))
    private void modifyFrame2(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.crystals.get().booleanValue()) {
            this.frame.render(matrices, vertices, light, overlay);
            return;
        }
        if (module.renderFrame2.get().booleanValue()) {
            Color color = module.crystalsFrame2Color.get();
            this.frame.render(matrices, vertices, light, overlay, color.getPacked());
        }
    }
}

