/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.PlayerEntityRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={PlayerEntityRenderer.class})
public abstract class PlayerEntityRendererMixin {
    @ModifyArgs(method={"renderArm"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal=0))
    private void modifyRenderLayer(Args args, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve) {
        Chams chams = Modules.get().get(Chams.class);
        if (chams.isActive() && chams.hand.get().booleanValue()) {
            Identifier texture = chams.handTexture.get() != false ? player.getSkinTextures().comp_1626() : Chams.BLANK;
            args.set(1, (Object)vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent((Identifier)texture)));
        }
    }

    @Redirect(method={"renderArm"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal=0))
    private void redirectRenderMain(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        Chams chams = Modules.get().get(Chams.class);
        if (chams.isActive() && chams.hand.get().booleanValue()) {
            Color color = chams.handColor.get();
            modelPart.render(matrices, vertices, light, overlay, color.getPacked());
        } else {
            modelPart.render(matrices, vertices, light, overlay);
        }
    }

    @Redirect(method={"renderArm"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal=1))
    private void redirectRenderSleeve(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        Chams chams = Modules.get().get(Chams.class);
        if (Modules.get().isActive(HandView.class)) {
            return;
        }
        if (chams.isActive() && chams.hand.get().booleanValue()) {
            Color color = chams.handColor.get();
            modelPart.render(matrices, vertices, light, overlay, color.getPacked());
        } else {
            modelPart.render(matrices, vertices, light, overlay);
        }
    }
}

