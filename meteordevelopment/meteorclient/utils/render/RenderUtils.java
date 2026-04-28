/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package meteordevelopment.meteorclient.utils.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class RenderUtils {
    public static Vec3d center;
    private static final Pool<RenderBlock> renderBlockPool;
    private static final List<RenderBlock> renderBlocks;
    private static final long initTime;

    private RenderUtils() {
    }

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(RenderUtils.class);
    }

    public static void drawItem(DrawContext drawContext, ItemStack itemStack, int x, int y, float scale, boolean overlay, String countOverride) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(0.0f, 0.0f, 401.0f);
        int scaledX = (int)((float)x / scale);
        int scaledY = (int)((float)y / scale);
        drawContext.drawItem(itemStack, scaledX, scaledY);
        if (overlay) {
            drawContext.drawItemInSlot(MeteorClient.mc.textRenderer, itemStack, scaledX, scaledY, countOverride);
        }
        matrices.pop();
    }

    public static void drawItem(DrawContext drawContext, ItemStack itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(drawContext, itemStack, x, y, scale, overlay, null);
    }

    public static void updateScreenCenter() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vector3f pos = new Vector3f(0.0f, 0.0f, 1.0f);
        if (((Boolean)mc.options.getBobView().getValue()).booleanValue()) {
            MatrixStack bobViewMatrices = new MatrixStack();
            RenderUtils.bobView(bobViewMatrices);
            pos.mulPosition((Matrix4fc)bobViewMatrices.peek().getPositionMatrix().invert());
        }
        center = new Vec3d((double)pos.x, (double)(-pos.y), (double)pos.z).rotateX(-((float)Math.toRadians(mc.gameRenderer.getCamera().getPitch()))).rotateY(-((float)Math.toRadians(mc.gameRenderer.getCamera().getYaw()))).add(mc.gameRenderer.getCamera().getPos());
    }

    private static void bobView(MatrixStack matrices) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        if (cameraEntity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)cameraEntity;
            float f = MeteorClient.mc.getRenderTickCounter().getTickDelta(true);
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp((float)f, (float)playerEntity.prevStrideDistance, (float)playerEntity.strideDistance);
            matrices.translate(-((double)(MathHelper.sin((float)(h * (float)Math.PI)) * i) * 0.5), (double)Math.abs(MathHelper.cos((float)(h * (float)Math.PI)) * i), 0.0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin((float)(h * (float)Math.PI)) * i * 3.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos((float)(h * (float)Math.PI - 0.2f)) * i) * 5.0f));
        }
    }

    public static void renderTickingBlock(BlockPos blockPos, Color sideColor, Color lineColor, ShapeMode shapeMode, int excludeDir, int duration, boolean fade, boolean shrink) {
        Iterator<RenderBlock> iterator = renderBlocks.iterator();
        while (iterator.hasNext()) {
            RenderBlock next = iterator.next();
            if (!next.pos.equals((Object)blockPos)) continue;
            iterator.remove();
            renderBlockPool.free(next);
        }
        renderBlocks.add(renderBlockPool.get().set(blockPos, sideColor, lineColor, shapeMode, excludeDir, duration, fade, shrink));
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        if (renderBlocks.isEmpty()) {
            return;
        }
        renderBlocks.forEach(RenderBlock::tick);
        Iterator<RenderBlock> iterator = renderBlocks.iterator();
        while (iterator.hasNext()) {
            RenderBlock next = iterator.next();
            if (next.ticks > 0) continue;
            iterator.remove();
            renderBlockPool.free(next);
        }
    }

    @EventHandler
    private static void onRender(Render3DEvent event) {
        renderBlocks.forEach(block -> block.render(event));
    }

    public static double getCurrentGameTickCalculated() {
        return RenderUtils.getCurrentGameTickCalculatedNano(System.nanoTime());
    }

    public static double getCurrentGameTickCalculatedNano(long nanoTime) {
        return (double)(nanoTime - initTime) / (double)TimeUnit.MILLISECONDS.toNanos(50L);
    }

    static {
        renderBlockPool = new Pool<RenderBlock>(RenderBlock::new);
        renderBlocks = new ArrayList<RenderBlock>();
        initTime = System.nanoTime();
    }

    public static class RenderBlock {
        public BlockPos.Mutable pos = new BlockPos.Mutable();
        public Color sideColor;
        public Color lineColor;
        public ShapeMode shapeMode;
        public int excludeDir;
        public int ticks;
        public int duration;
        public boolean fade;
        public boolean shrink;

        public RenderBlock set(BlockPos blockPos, Color sideColor, Color lineColor, ShapeMode shapeMode, int excludeDir, int duration, boolean fade, boolean shrink) {
            this.pos.set((Vec3i)blockPos);
            this.sideColor = sideColor;
            this.lineColor = lineColor;
            this.shapeMode = shapeMode;
            this.excludeDir = excludeDir;
            this.fade = fade;
            this.shrink = shrink;
            this.ticks = duration;
            this.duration = duration;
            return this;
        }

        public void tick() {
            --this.ticks;
        }

        public void render(Render3DEvent event) {
            int preSideA = this.sideColor.a;
            int preLineA = this.lineColor.a;
            double x1 = this.pos.getX();
            double y1 = this.pos.getY();
            double z1 = this.pos.getZ();
            double x2 = this.pos.getX() + 1;
            double y2 = this.pos.getY() + 1;
            double z2 = this.pos.getZ() + 1;
            double d = (double)((float)this.ticks - event.tickDelta) / (double)this.duration;
            if (this.fade) {
                this.sideColor.a = (int)((double)this.sideColor.a * d);
                this.lineColor.a = (int)((double)this.lineColor.a * d);
            }
            if (this.shrink) {
                x1 += d;
                y1 += d;
                z1 += d;
                x2 -= d;
                y2 -= d;
                z2 -= d;
            }
            event.renderer.box(x1, y1, z1, x2, y2, z2, this.sideColor, this.lineColor, this.shapeMode, this.excludeDir);
            this.sideColor.a = preSideA;
            this.lineColor.a = preLineA;
        }
    }
}

