/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockRenderType
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.block.entity.BlockEntityRenderer
 *  net.minecraft.client.render.model.BakedModel
 *  net.minecraft.client.render.model.BakedQuad
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.random.Random
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.utils.render;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IBakedQuad;
import meteordevelopment.meteorclient.utils.render.IVertexConsumerProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;

public class SimpleBlockRenderer {
    private static final MatrixStack MATRICES = new MatrixStack();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Random RANDOM = Random.create();

    private SimpleBlockRenderer() {
    }

    public static void renderWithBlockEntity(BlockEntity blockEntity, float tickDelta, IVertexConsumerProvider vertexConsumerProvider) {
        vertexConsumerProvider.setOffset(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ());
        SimpleBlockRenderer.render(blockEntity.getPos(), blockEntity.getCachedState(), vertexConsumerProvider);
        BlockEntityRenderer renderer = MeteorClient.mc.getBlockEntityRenderDispatcher().get(blockEntity);
        if (renderer != null && blockEntity.hasWorld() && blockEntity.getType().supports(blockEntity.getCachedState())) {
            renderer.render(blockEntity, tickDelta, MATRICES, (VertexConsumerProvider)vertexConsumerProvider, 0xF000F0, OverlayTexture.DEFAULT_UV);
        }
        vertexConsumerProvider.setOffset(0, 0, 0);
    }

    public static void render(BlockPos pos, BlockState state, VertexConsumerProvider consumerProvider) {
        if (state.getRenderType() != BlockRenderType.MODEL) {
            return;
        }
        VertexConsumer consumer = consumerProvider.getBuffer(RenderLayer.getSolid());
        BakedModel model = MeteorClient.mc.getBlockRenderManager().getModel(state);
        Vec3d offset = state.getModelOffset((BlockView)MeteorClient.mc.world, pos);
        float offsetX = (float)offset.x;
        float offsetY = (float)offset.y;
        float offsetZ = (float)offset.z;
        for (Direction direction : DIRECTIONS) {
            List list = model.getQuads(state, direction, RANDOM);
            if (list.isEmpty()) continue;
            SimpleBlockRenderer.renderQuads(list, offsetX, offsetY, offsetZ, consumer);
        }
        List list = model.getQuads(state, null, RANDOM);
        if (!list.isEmpty()) {
            SimpleBlockRenderer.renderQuads(list, offsetX, offsetY, offsetZ, consumer);
        }
    }

    private static void renderQuads(List<BakedQuad> quads, float offsetX, float offsetY, float offsetZ, VertexConsumer consumer) {
        for (BakedQuad bakedQuad : quads) {
            IBakedQuad quad = (IBakedQuad)bakedQuad;
            for (int j = 0; j < 4; ++j) {
                float x = quad.meteor$getX(j);
                float y = quad.meteor$getY(j);
                float z = quad.meteor$getZ(j);
                consumer.vertex(offsetX + x, offsetY + y, offsetZ + z);
            }
        }
    }
}

