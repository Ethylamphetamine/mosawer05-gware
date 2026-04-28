/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.item.ItemRenderer
 *  net.minecraft.client.render.model.BakedModel
 *  net.minecraft.client.render.model.BakedQuad
 *  net.minecraft.client.render.model.json.ModelTransformationMode
 *  net.minecraft.client.render.model.json.Transformation
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.random.Random
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.render.ApplyTransformationEvent;
import meteordevelopment.meteorclient.events.render.RenderItemEntityEvent;
import meteordevelopment.meteorclient.mixininterface.IBakedQuad;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class ItemPhysics
extends Module {
    private static final Direction[] FACES = new Direction[]{null, Direction.UP, Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
    private static final float PIXEL_SIZE = 0.0625f;
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> randomRotation;
    private final Random random;
    private boolean renderingItem;

    public ItemPhysics() {
        super(Categories.Render, "item-physics", "Applies physics to items on the ground.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.randomRotation = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("random-rotation")).description("Adds a random rotation to every item.")).defaultValue(true)).build());
        this.random = Random.createLocal();
    }

    @EventHandler
    private void onRenderItemEntity(RenderItemEntityEvent event) {
        MatrixStack matrices = event.matrixStack;
        matrices.push();
        ItemStack itemStack = event.itemEntity.getStack();
        BakedModel model = this.getModel(event.itemEntity);
        ModelInfo info = this.getInfo(model);
        this.random.setSeed((long)event.itemEntity.getId() * 2365798L);
        this.applyTransformation(matrices, model);
        matrices.translate(0.0f, info.offsetY, 0.0f);
        this.offsetInWater(matrices, event.itemEntity);
        this.preventZFighting(matrices, event.itemEntity);
        if (info.flat) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            matrices.translate(0.0f, 0.0f, info.offsetZ);
        }
        if (this.randomRotation.get().booleanValue()) {
            RotationAxis axis = RotationAxis.POSITIVE_Y;
            if (info.flat) {
                axis = RotationAxis.POSITIVE_Z;
            }
            float degrees = (this.random.nextFloat() * 2.0f - 1.0f) * 90.0f;
            matrices.multiply(axis.rotationDegrees(degrees));
        }
        this.renderItem(event, matrices, itemStack, model, info);
        matrices.pop();
        event.cancel();
    }

    @EventHandler
    private void onApplyTransformation(ApplyTransformationEvent event) {
        if (this.renderingItem) {
            event.cancel();
        }
    }

    private void renderItem(RenderItemEntityEvent event, MatrixStack matrices, ItemStack itemStack, BakedModel model, ModelInfo info) {
        this.renderingItem = true;
        int count = this.getRenderedCount(itemStack);
        for (int i = 0; i < count; ++i) {
            matrices.push();
            if (i > 0) {
                float x = (this.random.nextFloat() * 2.0f - 1.0f) * 0.25f;
                float z = (this.random.nextFloat() * 2.0f - 1.0f) * 0.25f;
                this.translate(matrices, info, x, 0.0f, z);
            }
            event.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrices, event.vertexConsumerProvider, event.light, OverlayTexture.DEFAULT_UV, model);
            matrices.pop();
            float y = Math.max(this.random.nextFloat() * 0.0625f, 0.03125f);
            this.translate(matrices, info, 0.0f, y, 0.0f);
        }
        this.renderingItem = false;
    }

    private void translate(MatrixStack matrices, ModelInfo info, float x, float y, float z) {
        if (info.flat) {
            float temp = y;
            y = z;
            z = -temp;
        }
        matrices.translate(x, y, z);
    }

    private int getRenderedCount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }
        return i;
    }

    private void applyTransformation(MatrixStack matrices, BakedModel model) {
        Transformation transformation = model.getTransformation().ground;
        float prevY = transformation.translation.y;
        transformation.translation.y = 0.0f;
        transformation.apply(false, matrices);
        transformation.translation.y = prevY;
    }

    private void offsetInWater(MatrixStack matrices, ItemEntity entity) {
        if (entity.isTouchingWater()) {
            matrices.translate(0.0f, 0.333f, 0.0f);
        }
    }

    private void preventZFighting(MatrixStack matrices, ItemEntity entity) {
        float offset = 1.0E-4f;
        float distance = (float)this.mc.gameRenderer.getCamera().getPos().distanceTo(entity.getPos());
        offset = Math.min(offset * Math.max(1.0f, distance), 0.01f);
        matrices.translate(0.0f, offset, 0.0f);
    }

    private BakedModel getModel(ItemEntity entity) {
        ItemStack itemStack = entity.getStack();
        if (itemStack.isOf(Items.TRIDENT)) {
            return this.mc.getItemRenderer().getModels().getModelManager().getModel(ItemRenderer.TRIDENT);
        }
        if (itemStack.isOf(Items.SPYGLASS)) {
            return this.mc.getItemRenderer().getModels().getModelManager().getModel(ItemRenderer.SPYGLASS);
        }
        return this.mc.getItemRenderer().getModel(itemStack, entity.getWorld(), null, entity.getId());
    }

    private ModelInfo getInfo(BakedModel model) {
        Random random = Random.createLocal();
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = Float.MIN_VALUE;
        for (Direction face : FACES) {
            for (BakedQuad _quad : model.getQuads(null, face, random)) {
                IBakedQuad quad = (IBakedQuad)_quad;
                block10: for (int i = 0; i < 4; ++i) {
                    switch (_quad.getFace()) {
                        case DOWN: {
                            minY = Math.min(minY, quad.meteor$getY(i));
                            continue block10;
                        }
                        case UP: {
                            maxY = Math.max(maxY, quad.meteor$getY(i));
                            continue block10;
                        }
                        case NORTH: {
                            minZ = Math.min(minZ, quad.meteor$getZ(i));
                            continue block10;
                        }
                        case SOUTH: {
                            maxZ = Math.max(maxZ, quad.meteor$getZ(i));
                            continue block10;
                        }
                        case WEST: {
                            minX = Math.min(minX, quad.meteor$getX(i));
                            continue block10;
                        }
                        case EAST: {
                            maxX = Math.max(maxX, quad.meteor$getX(i));
                        }
                    }
                }
            }
        }
        if (minX == Float.MAX_VALUE) {
            minX = 0.0f;
        }
        if (minY == Float.MAX_VALUE) {
            minY = 0.0f;
        }
        if (minZ == Float.MAX_VALUE) {
            minZ = 0.0f;
        }
        if (maxX == Float.MIN_VALUE) {
            maxX = 1.0f;
        }
        if (maxY == Float.MIN_VALUE) {
            maxY = 1.0f;
        }
        if (maxZ == Float.MIN_VALUE) {
            maxZ = 1.0f;
        }
        float x = maxX - minX;
        float y = maxY - minY;
        float z = maxZ - minZ;
        boolean flat = x > 0.0625f && y > 0.0625f && z <= 0.0625f;
        return new ModelInfo(flat, 0.5f - minY, minZ - minY);
    }

    record ModelInfo(boolean flat, float offsetY, float offsetZ) {
    }
}

