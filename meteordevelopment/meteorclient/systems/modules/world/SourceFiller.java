/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.fluid.Fluid
 *  net.minecraft.fluid.Fluids
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class SourceFiller
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> places;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> grimBypass;
    private final Setting<Double> placeTime;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> normalSideColor;
    private final Setting<SettingColor> normalLineColor;
    private long lastPlaceTimeMS;
    private List<BlockPos> placePoses;

    public SourceFiller() {
        super(Categories.World, "source-filler", "Places blocks in water and lava source blocks around you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.places = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("places")).description("Places to do each tick.")).min(1).defaultValue(1)).build());
        this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat")).description("Pauses while eating.")).defaultValue(true)).build());
        this.grimBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-bypass")).description("Bypasses Grim for airplace.")).defaultValue(true)).build());
        this.placeTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-time")).description("Time between places")).defaultValue(0.06).min(0.0).max(0.5).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders a block overlay where the obsidian will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.normalSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("normal-side-color")).description("The side color for normal blocks.")).defaultValue(new SettingColor(0, 255, 238, 12)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Lines)).build());
        this.normalLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("normal-line-color")).description("The line color for normal blocks.")).defaultValue(new SettingColor(0, 255, 238, 100)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Sides)).build());
        this.lastPlaceTimeMS = 0L;
        this.placePoses = new ArrayList<BlockPos>();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        this.update();
        if (this.render.get().booleanValue()) {
            this.draw(event);
        }
    }

    private void draw(Render3DEvent event) {
        for (BlockPos pos : this.placePoses) {
            event.renderer.box(pos, (Color)this.normalSideColor.get(), (Color)this.normalLineColor.get(), this.shapeMode.get(), 0);
        }
    }

    private void update() {
        this.placePoses.clear();
        int r = 5;
        long currentTime = System.currentTimeMillis();
        if (!((double)(currentTime - this.lastPlaceTimeMS) / 1000.0 > this.placeTime.get())) {
            return;
        }
        this.lastPlaceTimeMS = currentTime;
        if (this.pauseEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        for (int y = r; y > -r; --y) {
            for (int x = -r; x <= r; ++x) {
                for (int z = -r; z <= r; ++z) {
                    BlockPos pos = eyePos.add(x, y, z);
                    if (this.placePoses.size() >= 2 || !pos.toCenterPos().isInRange((Position)eyePos.toCenterPos(), 5.0) || !this.isWaterOrLavaSource(pos)) continue;
                    this.placePoses.add(pos);
                }
            }
        }
        Iterator<BlockPos> iterator = this.placePoses.iterator();
        boolean needSwapBack = false;
        int placed = 0;
        while (placed < this.places.get() && iterator.hasNext()) {
            BlockPos placePos = iterator.next();
            if (!BlockUtils.canPlace(placePos, true)) continue;
            FindItemResult result = InvUtils.findInHotbar(Items.NETHERRACK);
            if (!result.found()) break;
            if (!needSwapBack && this.mc.player.getInventory().selectedSlot != result.slot()) {
                InvUtils.swap(result.slot(), true);
                needSwapBack = true;
            }
            this.place(placePos);
        }
        if (needSwapBack) {
            InvUtils.swapBack();
        }
    }

    private boolean place(BlockPos blockPos) {
        if (!BlockUtils.canPlace(blockPos, true)) {
            return false;
        }
        Object dir = null;
        Hand hand = Hand.MAIN_HAND;
        if (dir == null && this.grimBypass.get().booleanValue()) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            hand = Hand.OFF_HAND;
        }
        Vec3d eyes = this.mc.player.getEyePos();
        boolean inside = eyes.x > (double)blockPos.getX() && eyes.x < (double)(blockPos.getX() + 1) && eyes.y > (double)blockPos.getY() && eyes.y < (double)(blockPos.getY() + 1) && eyes.z > (double)blockPos.getZ() && eyes.z < (double)(blockPos.getZ() + 1);
        int s = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockPos.toCenterPos(), (Direction)(dir == null ? Direction.DOWN : dir), blockPos, inside), s));
        if (dir == null && this.grimBypass.get().booleanValue()) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
        }
        return true;
    }

    public boolean isWaterOrLavaSource(BlockPos pos) {
        BlockState blockState = this.mc.world.getBlockState(pos);
        return (blockState.getFluidState().getFluid().matchesType((Fluid)Fluids.LAVA) || blockState.getFluidState().getFluid().matchesType((Fluid)Fluids.WATER)) && blockState.getFluidState().getLevel() == 8;
    }
}

