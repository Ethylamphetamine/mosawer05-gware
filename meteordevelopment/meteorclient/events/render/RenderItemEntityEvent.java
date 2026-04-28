/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.item.ItemRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.util.math.random.Random
 */
package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.random.Random;

public class RenderItemEntityEvent
extends Cancellable {
    private static final RenderItemEntityEvent INSTANCE = new RenderItemEntityEvent();
    public ItemEntity itemEntity;
    public float f;
    public float tickDelta;
    public MatrixStack matrixStack;
    public VertexConsumerProvider vertexConsumerProvider;
    public int light;
    public Random random;
    public ItemRenderer itemRenderer;

    public static RenderItemEntityEvent get(ItemEntity itemEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, Random random, ItemRenderer itemRenderer) {
        INSTANCE.setCancelled(false);
        RenderItemEntityEvent.INSTANCE.itemEntity = itemEntity;
        RenderItemEntityEvent.INSTANCE.f = f;
        RenderItemEntityEvent.INSTANCE.tickDelta = tickDelta;
        RenderItemEntityEvent.INSTANCE.matrixStack = matrixStack;
        RenderItemEntityEvent.INSTANCE.vertexConsumerProvider = vertexConsumerProvider;
        RenderItemEntityEvent.INSTANCE.light = light;
        RenderItemEntityEvent.INSTANCE.random = random;
        RenderItemEntityEvent.INSTANCE.itemRenderer = itemRenderer;
        return INSTANCE;
    }
}

