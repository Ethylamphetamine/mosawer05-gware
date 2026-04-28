/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Type
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class ForceSwim
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<Integer> range;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private PlayerEntity target;

    public ForceSwim() {
        super(Categories.Combat, "force-swim", "Tries to prevent people from standing up while swiming");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("Which blocks to use.")).defaultValue(Blocks.OBSIDIAN, Blocks.NETHERITE_BLOCK).build());
        this.range = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("target-range")).description("The range players can be targeted.")).defaultValue(4)).build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat")).description("Pauses while eating.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232, 10)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232)).build());
    }

    @Override
    public void onActivate() {
        this.target = null;
    }

    @Override
    public void onDeactivate() {
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target == null || TargetUtils.isBadTarget(this.target, this.range.get().intValue())) {
            this.target = TargetUtils.getPlayerTarget(this.range.get().intValue(), this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.range.get().intValue())) {
                return;
            }
        }
        if (this.target == null || !this.target.isCrawling()) {
            return;
        }
        if (this.pauseEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        Item useItem = this.findUseItem();
        if (useItem == null) {
            return;
        }
        List<BlockPos> placePoses = this.getBlockPoses();
        if (!MeteorClient.BLOCK.beginPlacement(placePoses, useItem)) {
            return;
        }
        placePoses.forEach(blockPos -> {
            boolean isCrystalBlock = false;
            for (Direction dir : Direction.Type.HORIZONTAL) {
                if (!blockPos.equals((Object)this.target.getBlockPos().offset(dir))) continue;
                isCrystalBlock = true;
            }
            if (isCrystalBlock) {
                return;
            }
            MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, (BlockPos)blockPos);
        });
        MeteorClient.BLOCK.endPlacement();
    }

    private Item findUseItem() {
        FindItemResult result = InvUtils.findInHotbar(itemStack -> {
            for (Block blocks : this.blocks.get()) {
                if (blocks.asItem() != itemStack.getItem()) continue;
                return true;
            }
            return false;
        });
        if (!result.found()) {
            return null;
        }
        return this.mc.player.getInventory().getStack(result.slot()).getItem();
    }

    private List<BlockPos> getBlockPoses() {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        Box boundingBox = this.target.getBoundingBox().expand(0.7, 0.0, 0.7);
        double feetY = this.target.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        for (BlockPos pos : BlockPos.iterate((int)((int)Math.floor(feetBox.minX)), (int)((int)Math.floor(feetBox.minY)), (int)((int)Math.floor(feetBox.minZ)), (int)((int)Math.floor(feetBox.maxX)), (int)((int)Math.floor(feetBox.maxY)), (int)((int)Math.floor(feetBox.maxZ)))) {
            list.add(pos.add(0, 1, 0));
        }
        return list;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue()) {
            return;
        }
        if (this.target == null || !this.target.isCrawling()) {
            return;
        }
        List<BlockPos> poses = this.getBlockPoses();
        for (BlockPos pos : poses) {
            boolean isCrystalBlock = false;
            for (Direction dir : Direction.Type.HORIZONTAL) {
                if (!pos.equals((Object)this.target.getBlockPos().offset(dir))) continue;
                isCrystalBlock = true;
            }
            if (isCrystalBlock || !BlockUtils.canPlace(pos, true)) continue;
            event.renderer.box(pos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }
}

