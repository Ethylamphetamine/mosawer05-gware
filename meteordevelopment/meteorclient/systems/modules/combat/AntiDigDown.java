/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class AntiDigDown
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

    public AntiDigDown() {
        super(Categories.Combat, "anti-dig-down", "Places blocks directly below other players to stop them from digging down.");
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

    private BlockPos getBelowBlockPos() {
        return this.target.getBlockPos().down();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof BlockUpdateS2CPacket) {
            BlockUpdateS2CPacket packet2 = (BlockUpdateS2CPacket)packet;
            if (this.target == null) {
                return;
            }
            if (this.pauseEat.get().booleanValue() && this.mc.player.isUsingItem()) {
                return;
            }
            Item useItem = this.findUseItem();
            if (useItem == null) {
                return;
            }
            BlockPos belowPos = this.getBelowBlockPos();
            if (belowPos == null) {
                return;
            }
            SilentMine silentMine = Modules.get().get(SilentMine.class);
            if (silentMine.getDelayedDestroyBlockPos() != null && belowPos.equals((Object)silentMine.getDelayedDestroyBlockPos()) || silentMine.getRebreakBlockPos() != null && belowPos.equals((Object)silentMine.getRebreakBlockPos())) {
                return;
            }
            if (packet2.getPos().equals((Object)belowPos) && packet2.getState().isAir()) {
                if (!MeteorClient.BLOCK.beginPlacement(belowPos, packet2.getState(), useItem)) {
                    return;
                }
                MeteorClient.BLOCK.placeBlock(useItem, belowPos, packet2.getState());
                MeteorClient.BLOCK.endPlacement();
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue()) {
            return;
        }
        if (this.target == null) {
            return;
        }
        BlockPos pos = this.getBelowBlockPos();
        if (pos == null) {
            return;
        }
        event.renderer.box(pos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }
}

