/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.SpawnEggItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AirPlace
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRange;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> grimBypass;
    private final Setting<Boolean> customRange;
    private final Setting<Double> range;
    private HitResult hitResult;

    public AirPlace() {
        super(Categories.Player, "air-place", "Places a block where your crosshair is pointing at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRange = this.settings.createGroup("Range");
        this.render = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders a block overlay where the obsidian will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The color of the sides of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color of the lines of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.grimBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-bypass")).description("Bypass for GrimAC.")).defaultValue(false)).build());
        this.customRange = this.sgRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-range")).description("Use custom range for air place.")).defaultValue(false)).build());
        this.range = this.sgRange.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Custom range to place at.")).visible(this.customRange::get)).defaultValue(5.0).min(0.0).sliderMax(6.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        BlockHitResult blockHitResult;
        block7: {
            block6: {
                double r = this.customRange.get() != false ? this.range.get().doubleValue() : this.mc.player.getBlockInteractionRange();
                this.hitResult = this.mc.getCameraEntity().raycast(r, 0.0f, false);
                HitResult hitResult = this.hitResult;
                if (!(hitResult instanceof BlockHitResult)) break block6;
                blockHitResult = (BlockHitResult)hitResult;
                if (this.mc.player.getMainHandStack().getItem() instanceof BlockItem || this.mc.player.getMainHandStack().getItem() instanceof SpawnEggItem) break block7;
            }
            return;
        }
        if (this.mc.options.useKey.isPressed() && BlockUtils.canPlace(blockHitResult.getBlockPos())) {
            Hand hand = Hand.MAIN_HAND;
            if (this.grimBypass.get().booleanValue()) {
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
                hand = Hand.OFF_HAND;
            }
            BlockUtils.place(blockHitResult.getBlockPos(), hand, this.mc.player.getInventory().selectedSlot, false, 0, true, true, false);
            if (this.grimBypass.get().booleanValue()) {
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockHitResult blockHitResult;
        HitResult hitResult = this.hitResult;
        if (!(hitResult instanceof BlockHitResult && this.mc.world.getBlockState((blockHitResult = (BlockHitResult)hitResult).getBlockPos()).isReplaceable() && (this.mc.player.getMainHandStack().getItem() instanceof BlockItem || this.mc.player.getMainHandStack().getItem() instanceof SpawnEggItem) && this.render.get().booleanValue())) {
            return;
        }
        event.renderer.box(blockHitResult.getBlockPos(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
    }
}

