/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BannerBlock
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.WallBannerBlock
 *  net.minecraft.block.entity.BannerBlockEntity
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.block.entity.BannerBlockEntityRenderer
 *  net.minecraft.client.render.model.ModelLoader
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.RotationAxis
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BannerBlockEntityRenderer.class})
public abstract class BannerBlockEntityRendererMixin {
    @Final
    @Shadow
    private ModelPart pillar;
    @Final
    @Shadow
    private ModelPart crossbar;

    @Inject(method={"render(Lnet/minecraft/block/entity/BannerBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void render(BannerBlockEntity bannerBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci) {
        if (bannerBlockEntity.getWorld() != null) {
            NoRender.BannerRenderMode renderMode = Modules.get().get(NoRender.class).getBannerRenderMode();
            if (renderMode == NoRender.BannerRenderMode.None) {
                ci.cancel();
            } else if (renderMode == NoRender.BannerRenderMode.Pillar) {
                BlockState blockState = bannerBlockEntity.getCachedState();
                if (blockState.getBlock() instanceof BannerBlock) {
                    this.pillar.visible = true;
                    this.crossbar.visible = false;
                    this.renderPillar(bannerBlockEntity, matrixStack, vertexConsumerProvider, i, j);
                } else {
                    this.pillar.visible = false;
                    this.crossbar.visible = true;
                    this.renderCrossbar(bannerBlockEntity, matrixStack, vertexConsumerProvider, i, j);
                }
                ci.cancel();
            }
        }
    }

    @Unique
    private void renderPillar(BannerBlockEntity bannerBlockEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        matrixStack.push();
        BlockState blockState = bannerBlockEntity.getCachedState();
        matrixStack.translate(0.5, 0.5, 0.5);
        float h = (float)(-((Integer)blockState.get((Property)BannerBlock.ROTATION)).intValue() * 360) / 16.0f;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
        matrixStack.push();
        matrixStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        VertexConsumer vertexConsumer = ModelLoader.BANNER_BASE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
        this.pillar.render(matrixStack, vertexConsumer, i, j);
        matrixStack.pop();
        matrixStack.pop();
    }

    @Unique
    private void renderCrossbar(BannerBlockEntity bannerBlockEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        matrixStack.push();
        BlockState blockState = bannerBlockEntity.getCachedState();
        matrixStack.translate(0.5, -0.1666666716337204, 0.5);
        float h = -((Direction)blockState.get((Property)WallBannerBlock.FACING)).asRotation();
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
        matrixStack.translate(0.0, -0.3125, -0.4375);
        matrixStack.push();
        matrixStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        VertexConsumer vertexConsumer = ModelLoader.BANNER_BASE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
        this.crossbar.render(matrixStack, vertexConsumer, i, j);
        matrixStack.pop();
        matrixStack.pop();
    }
}

