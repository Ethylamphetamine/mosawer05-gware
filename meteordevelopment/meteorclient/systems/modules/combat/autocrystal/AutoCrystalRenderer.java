/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public class AutoCrystalRenderer {
    private final Settings settings = new Settings();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final AutoCrystal autoCrystal;
    private final Setting<RenderMode> renderMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("render-mode")).description("The mode to render in.")).defaultValue(RenderMode.Gradient)).build());
    private final Setting<Integer> renderTime = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-time")).description("How long to render placements (in ticks).")).defaultValue(10)).min(0).sliderMax(40).visible(() -> this.renderMode.get() != RenderMode.None && this.renderMode.get() != RenderMode.DelayDraw)).build());
    private final Setting<ShapeMode> shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(() -> this.renderMode.get() != RenderMode.None && this.renderMode.get() != RenderMode.DelayDraw)).build());
    private final Setting<SettingColor> sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the block overlay.")).defaultValue(new SettingColor(255, 255, 255, 45)).visible(() -> this.renderMode.get() != RenderMode.None && this.renderMode.get() != RenderMode.DelayDraw && this.shapeMode.get().sides())).build());
    private final Setting<SettingColor> lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the block overlay.")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> this.renderMode.get() != RenderMode.None && this.renderMode.get() != RenderMode.DelayDraw && this.shapeMode.get().lines())).build());
    private final Setting<Integer> smoothness = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("smoothness")).description("How smoothly the render should move around.")).defaultValue(10)).min(0).sliderMax(20).visible(() -> this.renderMode.get() == RenderMode.Smooth || this.renderMode.get() == RenderMode.GradientSmooth)).build());
    private final Setting<Double> height = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("height")).description("How tall the gradient should be.")).defaultValue(0.7).min(0.0).sliderMax(1.0).visible(() -> this.renderMode.get() == RenderMode.Gradient || this.renderMode.get() == RenderMode.GradientSmooth)).build());
    private final Setting<ShapeMode> placeDelayShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("place-delay-shape-mode")).description("How the shapes are rendered in DelayDraw.")).defaultValue(ShapeMode.Both)).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<SettingColor> placeDelayColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("place-delay-color")).description("Color to render place delays in.")).defaultValue(new SettingColor(110, 0, 255, 40)).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<Double> placeDelayFadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-delay-fade-time")).description("How long to fade the box.")).defaultValue(0.7).min(0.0).sliderMax(2.0).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<ShapeMode> breakDelayShapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("break-delay-shape-mode")).description("How the shapes are rendered in DelayDraw.")).defaultValue(ShapeMode.Both)).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<SettingColor> breakDelayColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("break-delay-color")).description("Color to render break delays in.")).defaultValue(new SettingColor(0, 0, 0, 0)).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<Double> breakDelayFadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-delay-fade-time")).description("How long to fade the box.")).defaultValue(0.4).min(0.0).sliderMax(2.0).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<Double> breakDelayFadeExponent = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-delay-fade-exponent")).description("Adds an exponent to the fade.")).defaultValue(1.6).min(0.2).sliderMax(4.0).visible(() -> this.renderMode.get() == RenderMode.DelayDraw)).build());
    private final Setting<Boolean> renderBreak = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-break")).description("Renders the block under the crystal when breaking.")).defaultValue(false)).build());
    private final Setting<Integer> breakRenderTime = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-render-time")).description("How long to render breaking (in ticks).")).defaultValue(10)).min(0).sliderMax(20).visible(this.renderBreak::get)).build());
    private final Setting<Boolean> renderDamageText = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-text")).description("Renders text overlay on the block.")).defaultValue(true)).visible(() -> this.renderMode.get() != RenderMode.None)).build());
    private final Setting<RenderTextMode> textMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("text-mode")).description("What info to render on the block.")).defaultValue(RenderTextMode.Damage)).visible(() -> this.renderMode.get() != RenderMode.None && this.renderDamageText.get() != false)).build());
    private final Setting<SettingColor> damageColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("text-color")).description("The color of the text.")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> this.renderMode.get() != RenderMode.None && this.renderDamageText.get() != false)).build());
    private final Setting<Double> damageTextScale = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("text-scale")).description("How big the text should be.")).defaultValue(1.25).min(1.0).sliderMax(4.0).visible(() -> this.renderMode.get() != RenderMode.None && this.renderDamageText.get() != false)).build());
    public final Setting<Boolean> renderPlace = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-place")).description("Visually holds a crystal for one tick when placing.")).defaultValue(true)).build());
    private final BlockPos.Mutable placeRenderPos = new BlockPos.Mutable();
    private final BlockPos.Mutable breakRenderPos = new BlockPos.Mutable();
    private long placeRenderTimeStart = 0L;
    private long breakRenderTimeStart = 0L;
    private double renderDamage = 0.0;
    private final Vector3d vec3 = new Vector3d();
    private Box renderBoxOne;
    private Box renderBoxTwo;
    private final Map<BlockPos, Long> crystalRenderPlaceDelays = new HashMap<BlockPos, Long>();
    private final Map<CrystalBreakRender, Long> crystalRenderBreakDelays = new HashMap<CrystalBreakRender, Long>();

    public AutoCrystalRenderer(AutoCrystal ac) {
        this.autoCrystal = ac;
        ac.settings.groups.addAll(this.settings.groups);
    }

    public void onActivate() {
        this.placeRenderTimeStart = 0L;
        this.breakRenderTimeStart = 0L;
        this.renderBoxOne = null;
        this.renderBoxTwo = null;
        this.crystalRenderPlaceDelays.clear();
        this.crystalRenderBreakDelays.clear();
    }

    public void onRender3D(Render3DEvent event) {
        boolean isPlaceRenderValid;
        if (this.renderMode.get() == RenderMode.None) {
            return;
        }
        long time = System.currentTimeMillis();
        boolean bl = isPlaceRenderValid = time - this.placeRenderTimeStart < (long)this.renderTime.get().intValue() * 50L;
        if (this.renderMode.get() != RenderMode.DelayDraw && isPlaceRenderValid) {
            switch (this.renderMode.get().ordinal()) {
                case 0: {
                    event.renderer.box((BlockPos)this.placeRenderPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                }
                case 1: {
                    if (this.renderBoxOne == null) {
                        this.renderBoxOne = new Box((BlockPos)this.placeRenderPos);
                    }
                    this.renderBoxTwo = this.renderBoxTwo == null ? new Box((BlockPos)this.placeRenderPos) : new Box((BlockPos)this.placeRenderPos);
                    double offsetX = (this.renderBoxTwo.minX - this.renderBoxOne.minX) / (double)this.smoothness.get().intValue();
                    double offsetY = (this.renderBoxTwo.minY - this.renderBoxOne.minY) / (double)this.smoothness.get().intValue();
                    double offsetZ = (this.renderBoxTwo.minZ - this.renderBoxOne.minZ) / (double)this.smoothness.get().intValue();
                    this.renderBoxOne = new Box(this.renderBoxOne.minX + offsetX, this.renderBoxOne.minY + offsetY, this.renderBoxOne.minZ + offsetZ, this.renderBoxOne.maxX + offsetX, this.renderBoxOne.maxY + offsetY, this.renderBoxOne.maxZ + offsetZ);
                    event.renderer.box(this.renderBoxOne, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                }
                case 3: {
                    Color bottom = new Color(0, 0, 0, 0);
                    int n = this.placeRenderPos.getX();
                    int y = this.placeRenderPos.getY() + 1;
                    int z = this.placeRenderPos.getZ();
                    if (this.shapeMode.get().sides()) {
                        event.renderer.quadHorizontal(n, y, z, n + 1, z + 1, this.sideColor.get());
                        event.renderer.gradientQuadVertical(n, y, z, n + 1, (double)y - this.height.get(), z, bottom, this.sideColor.get());
                        event.renderer.gradientQuadVertical(n, y, z, n, (double)y - this.height.get(), z + 1, bottom, this.sideColor.get());
                        event.renderer.gradientQuadVertical(n + 1, y, z, n + 1, (double)y - this.height.get(), z + 1, bottom, this.sideColor.get());
                        event.renderer.gradientQuadVertical(n, y, z + 1, n + 1, (double)y - this.height.get(), z + 1, bottom, this.sideColor.get());
                    }
                    if (!this.shapeMode.get().lines()) break;
                    event.renderer.line(n, y, z, n + 1, y, z, this.lineColor.get());
                    event.renderer.line(n, y, z, n, y, z + 1, this.lineColor.get());
                    event.renderer.line(n + 1, y, z, n + 1, y, z + 1, this.lineColor.get());
                    event.renderer.line(n, y, z + 1, n + 1, y, z + 1, this.lineColor.get());
                    event.renderer.line(n, y, z, n, (double)y - this.height.get(), z, this.lineColor.get(), bottom);
                    event.renderer.line(n + 1, y, z, n + 1, (double)y - this.height.get(), z, this.lineColor.get(), bottom);
                    event.renderer.line(n, y, z + 1, n, (double)y - this.height.get(), z + 1, this.lineColor.get(), bottom);
                    event.renderer.line(n + 1, y, z + 1, n + 1, (double)y - this.height.get(), z + 1, this.lineColor.get(), bottom);
                    break;
                }
                case 4: {
                    if (this.renderBoxOne == null) {
                        this.renderBoxOne = new Box((BlockPos)this.placeRenderPos);
                    }
                    this.renderBoxTwo = this.renderBoxTwo == null ? new Box((BlockPos)this.placeRenderPos) : new Box((BlockPos)this.placeRenderPos);
                    double offsetX = (this.renderBoxTwo.minX - this.renderBoxOne.minX) / (double)this.smoothness.get().intValue();
                    double offsetY = (this.renderBoxTwo.minY - this.renderBoxOne.minY) / (double)this.smoothness.get().intValue();
                    double offsetZ = (this.renderBoxTwo.minZ - this.renderBoxOne.minZ) / (double)this.smoothness.get().intValue();
                    this.renderBoxOne = new Box(this.renderBoxOne.minX + offsetX, this.renderBoxOne.minY + offsetY, this.renderBoxOne.minZ + offsetZ, this.renderBoxOne.maxX + offsetX, this.renderBoxOne.maxY + offsetY, this.renderBoxOne.maxZ + offsetZ);
                    Color bottom = new Color(0, 0, 0, 0);
                    double x1 = this.renderBoxOne.minX;
                    double topY = this.renderBoxOne.maxY;
                    double z1 = this.renderBoxOne.minZ;
                    double x2 = this.renderBoxOne.maxX;
                    double z2 = this.renderBoxOne.maxZ;
                    if (this.shapeMode.get().sides()) {
                        event.renderer.quadHorizontal(x1, topY, z1, x2, z2, this.sideColor.get());
                        event.renderer.gradientQuadVertical(x1, topY, z1, x2, topY - this.height.get(), z1, bottom, this.sideColor.get());
                        event.renderer.gradientQuadVertical(x1, topY, z1, x1, topY - this.height.get(), z2, bottom, this.sideColor.get());
                        event.renderer.gradientQuadVertical(x2, topY, z1, x2, topY - this.height.get(), z2, bottom, this.sideColor.get());
                        event.renderer.gradientQuadVertical(x1, topY, z2, x2, topY - this.height.get(), z2, bottom, this.sideColor.get());
                    }
                    if (!this.shapeMode.get().lines()) break;
                    event.renderer.line(x1, topY, z1, x2, topY, z1, this.lineColor.get());
                    event.renderer.line(x1, topY, z1, x1, topY, z2, this.lineColor.get());
                    event.renderer.line(x2, topY, z1, x2, topY, z2, this.lineColor.get());
                    event.renderer.line(x1, topY, z2, x2, topY, z2, this.lineColor.get());
                    event.renderer.line(x1, topY, z1, x1, topY - this.height.get(), z1, this.lineColor.get(), bottom);
                    event.renderer.line(x2, topY, z1, x2, topY - this.height.get(), z1, this.lineColor.get(), bottom);
                    event.renderer.line(x1, topY, z2, x1, topY - this.height.get(), z2, this.lineColor.get(), bottom);
                    event.renderer.line(x2, topY, z2, x2, topY - this.height.get(), z2, this.lineColor.get(), bottom);
                    break;
                }
                case 2: {
                    break;
                }
            }
        }
        if (this.renderMode.get() == RenderMode.DelayDraw) {
            for (Map.Entry<BlockPos, Long> entry : this.crystalRenderPlaceDelays.entrySet()) {
                if ((double)(time - entry.getValue()) > this.placeDelayFadeTime.get() * 1000.0) continue;
                double delayTime = (double)(time - entry.getValue()) / 1000.0;
                double timeCompletion = delayTime / this.placeDelayFadeTime.get();
                this.renderBoxSized(event, entry.getKey(), 1.0, 1.0 - timeCompletion, this.placeDelayColor.get(), this.placeDelayColor.get(), this.placeDelayShapeMode.get());
            }
            for (Map.Entry<Object, Long> entry : this.crystalRenderBreakDelays.entrySet()) {
                if ((double)(time - entry.getValue()) > this.breakDelayFadeTime.get() * 1000.0) continue;
                CrystalBreakRender render = (CrystalBreakRender)entry.getKey();
                if (render.parts == null && render.entity != null) {
                    render.parts = WireframeEntityRenderer.cloneEntityForRendering(event, render.entity, render.pos);
                    render.entity = null;
                }
                double delayTime = (double)(time - entry.getValue()) / 1000.0;
                double timeCompletion = delayTime / this.breakDelayFadeTime.get();
                Color color = this.breakDelayColor.get().copy().a((int)((double)this.breakDelayColor.get().a * Math.pow(1.0 - timeCompletion, this.breakDelayFadeExponent.get())));
                if (render.parts == null) continue;
                WireframeEntityRenderer.render(event, render.pos, render.parts, 1.0, color, color, this.breakDelayShapeMode.get());
            }
        }
        if (this.renderBreak.get().booleanValue()) {
            boolean isBreakRenderValid;
            boolean bl2 = isBreakRenderValid = time - this.breakRenderTimeStart < (long)this.breakRenderTime.get().intValue() * 50L;
            if (isBreakRenderValid) {
                event.renderer.box((BlockPos)this.breakRenderPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
            }
        }
    }

    public void onRender2D(Render2DEvent event) {
        boolean isPlaceRenderValid;
        if (this.renderMode.get() == RenderMode.None || !this.renderDamageText.get().booleanValue()) {
            return;
        }
        long time = System.currentTimeMillis();
        boolean bl = isPlaceRenderValid = time - this.placeRenderTimeStart < (long)this.renderTime.get().intValue() * 50L;
        if (this.renderMode.get() != RenderMode.DelayDraw && !isPlaceRenderValid) {
            return;
        }
        if (this.renderMode.get() == RenderMode.Smooth || this.renderMode.get() == RenderMode.GradientSmooth) {
            if (this.renderBoxOne == null) {
                return;
            }
            this.vec3.set(this.renderBoxOne.minX + 0.5, this.renderBoxOne.minY + 0.5, this.renderBoxOne.minZ + 0.5);
        } else {
            this.vec3.set((double)this.placeRenderPos.getX() + 0.5, (double)this.placeRenderPos.getY() + 0.5, (double)this.placeRenderPos.getZ() + 0.5);
        }
        if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
            NametagUtils.begin(this.vec3);
            TextRenderer.get().begin(1.0, false, true);
            String text = this.textMode.get() == RenderTextMode.Damage ? String.format("%.1f", this.renderDamage) : String.valueOf(this.autoCrystal.getCPS());
            double w = TextRenderer.get().getWidth(text) / 2.0;
            TextRenderer.get().render(text, -w, 0.0, this.damageColor.get(), true);
            TextRenderer.get().end();
            NametagUtils.end();
        }
    }

    public void onPlaceCrystal(BlockPos pos, double damage) {
        this.placeRenderPos.set((Vec3i)pos);
        this.renderDamage = damage;
        this.placeRenderTimeStart = System.currentTimeMillis();
        if (this.renderMode.get() == RenderMode.Fading) {
            RenderUtils.renderTickingBlock((BlockPos)this.placeRenderPos, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0, this.renderTime.get(), true, false);
        }
        if (this.renderMode.get() == RenderMode.DelayDraw) {
            this.crystalRenderPlaceDelays.put(pos, System.currentTimeMillis());
        }
    }

    public void onBreakCrystal(Entity entity) {
        if (this.renderBreak.get().booleanValue()) {
            this.breakRenderPos.set((Vec3i)entity.getBlockPos().down());
            this.breakRenderTimeStart = System.currentTimeMillis();
        }
        if (this.renderMode.get() == RenderMode.DelayDraw) {
            CrystalBreakRender breakRender = new CrystalBreakRender(this);
            breakRender.pos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            breakRender.entity = entity;
            this.crystalRenderBreakDelays.put(breakRender, System.currentTimeMillis());
        }
    }

    private void renderBoxSized(Render3DEvent event, BlockPos blockPos, double size, double alpha, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        Box orig = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        double shrinkFactor = 1.0 - size;
        Box box = orig.shrink(orig.getLengthX() * shrinkFactor, orig.getLengthY() * shrinkFactor, orig.getLengthZ() * shrinkFactor);
        double xShrink = orig.getLengthX() * shrinkFactor / 2.0;
        double yShrink = orig.getLengthY() * shrinkFactor / 2.0;
        double zShrink = orig.getLengthZ() * shrinkFactor / 2.0;
        double x1 = (double)blockPos.getX() + box.minX + xShrink;
        double y1 = (double)blockPos.getY() + box.minY + yShrink;
        double z1 = (double)blockPos.getZ() + box.minZ + zShrink;
        double x2 = (double)blockPos.getX() + box.maxX + xShrink;
        double y2 = (double)blockPos.getY() + box.maxY + yShrink;
        double z2 = (double)blockPos.getZ() + box.maxZ + zShrink;
        event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor.copy().a((int)((double)sideColor.a * alpha)), lineColor.copy().a((int)((double)lineColor.a * alpha)), shapeMode, 0);
    }

    public static enum RenderMode {
        Normal,
        Smooth,
        Fading,
        Gradient,
        GradientSmooth,
        DelayDraw,
        None;

    }

    public static enum RenderTextMode {
        Damage,
        CPS;

    }

    private class CrystalBreakRender {
        public Vec3d pos;
        public List<WireframeEntityRenderer.RenderablePart> parts;
        public Entity entity;

        private CrystalBreakRender(AutoCrystalRenderer autoCrystalRenderer) {
        }
    }
}

