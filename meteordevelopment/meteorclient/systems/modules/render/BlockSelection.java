/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class BlockSelection
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> advanced;
    private final Setting<Boolean> oneSide;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> hideInside;

    public BlockSelection() {
        super(Categories.Render, "block-selection", "Modifies how your block selection is rendered.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.advanced = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("advanced")).description("Shows a more advanced outline on different types of shape blocks.")).defaultValue(true)).build());
        this.oneSide = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("single-side")).description("Only renders the side you are looking at.")).defaultValue(false)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
        this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
        this.hideInside = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hide-when-inside-block")).description("Hide selection when inside target block.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockHitResult result;
        HitResult hitResult;
        if (this.mc.crosshairTarget == null || !((hitResult = this.mc.crosshairTarget) instanceof BlockHitResult) || (result = (BlockHitResult)hitResult).getType() == HitResult.Type.MISS) {
            return;
        }
        if (this.hideInside.get().booleanValue() && result.isInsideBlock()) {
            return;
        }
        BlockPos bp = result.getBlockPos();
        Direction side = result.getSide();
        VoxelShape shape = this.mc.world.getBlockState(bp).getOutlineShape((BlockView)this.mc.world, bp);
        if (shape.isEmpty()) {
            return;
        }
        Box box = shape.getBoundingBox();
        if (this.oneSide.get().booleanValue()) {
            if (side == Direction.UP || side == Direction.DOWN) {
                event.renderer.sideHorizontal((double)bp.getX() + box.minX, (double)bp.getY() + (side == Direction.DOWN ? box.minY : box.maxY), (double)bp.getZ() + box.minZ, (double)bp.getX() + box.maxX, (double)bp.getZ() + box.maxZ, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get());
            } else if (side == Direction.SOUTH || side == Direction.NORTH) {
                double z = side == Direction.NORTH ? box.minZ : box.maxZ;
                event.renderer.sideVertical((double)bp.getX() + box.minX, (double)bp.getY() + box.minY, (double)bp.getZ() + z, (double)bp.getX() + box.maxX, (double)bp.getY() + box.maxY, (double)bp.getZ() + z, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get());
            } else {
                double x = side == Direction.WEST ? box.minX : box.maxX;
                event.renderer.sideVertical((double)bp.getX() + x, (double)bp.getY() + box.minY, (double)bp.getZ() + box.minZ, (double)bp.getX() + x, (double)bp.getY() + box.maxY, (double)bp.getZ() + box.maxZ, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get());
            }
        } else if (this.advanced.get().booleanValue()) {
            if (this.shapeMode.get() == ShapeMode.Both || this.shapeMode.get() == ShapeMode.Lines) {
                shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> event.renderer.line((double)bp.getX() + minX, (double)bp.getY() + minY, (double)bp.getZ() + minZ, (double)bp.getX() + maxX, (double)bp.getY() + maxY, (double)bp.getZ() + maxZ, this.lineColor.get()));
            }
            if (this.shapeMode.get() == ShapeMode.Both || this.shapeMode.get() == ShapeMode.Sides) {
                for (Box b : shape.getBoundingBoxes()) {
                    this.render(event, bp, b);
                }
            }
        } else {
            this.render(event, bp, box);
        }
    }

    private void render(Render3DEvent event, BlockPos bp, Box box) {
        event.renderer.box((double)bp.getX() + box.minX, (double)bp.getY() + box.minY, (double)bp.getZ() + box.minZ, (double)bp.getX() + box.maxX, (double)bp.getY() + box.maxY, (double)bp.getZ() + box.maxZ, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
    }
}

