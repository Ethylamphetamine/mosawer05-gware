/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

public class AutoPortal
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> buildBind;
    private final Setting<Boolean> lightPortal;
    private final Setting<Boolean> baritonePathToPortal;
    private boolean active;
    private boolean keyUnpressed;
    private List<BlockPos> bestPortalFrameBlocks;
    private BlockPos ignitionPos;

    public AutoPortal() {
        super(Categories.World, "auto-portal", "Automatically builds and paths to a portal");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.buildBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("key-bind")).description("Build a portal on keybind press")).build());
        this.lightPortal = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("light-portal")).description("Whether or not to light the portal")).defaultValue(true)).build());
        this.baritonePathToPortal = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("baritone-to-portal")).description("Baritones to the portal after finishing building")).defaultValue(true)).visible(() -> BaritoneUtils.IS_AVAILABLE)).build());
        this.active = false;
        this.keyUnpressed = false;
        this.bestPortalFrameBlocks = null;
        this.ignitionPos = null;
    }

    private void activate() {
        this.active = true;
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.update();
    }

    private void deactivate(boolean built) {
        this.bestPortalFrameBlocks = null;
        this.ignitionPos = null;
        this.active = false;
        if (built) {
            this.info("Built portal", new Object[0]);
        }
    }

    private void update() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (!this.active) {
            return;
        }
        if (!InvUtils.find(Items.OBSIDIAN).found()) {
            this.deactivate(false);
            return;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.active) {
            return;
        }
        if (this.bestPortalFrameBlocks == null || this.bestPortalFrameBlocks.isEmpty()) {
            this.bestPortalFrameBlocks = this.findBestPortalFrame();
        }
        if (this.bestPortalFrameBlocks == null || this.bestPortalFrameBlocks.isEmpty()) {
            this.deactivate(false);
            return;
        }
        if (this.mc.player.isUsingItem()) {
            return;
        }
        List<BlockPos> placesLeft = this.bestPortalFrameBlocks.stream().filter(blockPos -> this.mc.world.isAir(blockPos)).toList();
        if (placesLeft.isEmpty()) {
            if (this.lightPortal.get().booleanValue()) {
                if (MeteorClient.SWAP.beginSwap(Items.FLINT_AND_STEEL, true)) {
                    this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, new BlockHitResult(this.ignitionPos.down().toCenterPos(), Direction.UP, this.ignitionPos.down(), false));
                    MeteorClient.SWAP.endSwap(true);
                    if (this.baritonePathToPortal.get().booleanValue() && BaritoneUtils.IS_AVAILABLE) {
                        PathManagers.get().moveToBlockPos(this.ignitionPos);
                    }
                } else {
                    this.info("Failed to light portal", new Object[0]);
                }
                this.deactivate(true);
                return;
            }
            this.deactivate(true);
            return;
        }
        if (MeteorClient.BLOCK.beginPlacement(placesLeft, Items.OBSIDIAN)) {
            placesLeft.forEach(blockPos -> MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, (BlockPos)blockPos));
            MeteorClient.BLOCK.endPlacement();
        }
    }

    @EventHandler(priority=200)
    private void onRender(Render3DEvent event) {
        if (!this.buildBind.get().isPressed()) {
            this.keyUnpressed = true;
        }
        if (this.buildBind.get().isPressed() && this.keyUnpressed && !(this.mc.currentScreen instanceof ChatScreen)) {
            this.activate();
            this.keyUnpressed = false;
        }
        this.update();
        if (this.ignitionPos != null) {
            event.renderer.box(this.ignitionPos, Color.RED, Color.RED, ShapeMode.Both, 0);
        }
    }

    private List<BlockPos> findBestPortalFrame() {
        BlockPos startPos = this.mc.player.getBlockPos();
        List<BlockPos> bestFrame = new ArrayList<BlockPos>();
        BlockPos bestIgnitionPos = null;
        double bestPortalScore = 0.0;
        for (int x = -10; x <= 10; ++x) {
            for (int y = -5; y <= 5; ++y) {
                for (int z = -10; z <= 10; ++z) {
                    BlockPos pos = startPos.add(x, y, z);
                    if (!this.canBuildPortalAtPosition(pos)) continue;
                    int distance = pos.add(1, 2, 0).getManhattanDistance((Vec3i)this.mc.player.getBlockPos());
                    double score = 1.0 / (double)distance;
                    if (this.mc.world.getBlockState(pos.down()).isSolidBlock((BlockView)this.mc.world, pos.down())) {
                        score += 10.0;
                    }
                    if (!(score > bestPortalScore)) continue;
                    bestPortalScore = score;
                    bestFrame = this.getPortalFramePositions(pos);
                    bestIgnitionPos = pos.add(1, 1, 0);
                }
            }
        }
        this.ignitionPos = !bestFrame.isEmpty() ? bestIgnitionPos : null;
        return bestFrame;
    }

    private List<BlockPos> getPortalFramePositions(BlockPos basePos) {
        ArrayList<BlockPos> framePositions = new ArrayList<BlockPos>();
        for (int y = 1; y < 4; ++y) {
            framePositions.add(basePos.add(0, y, 0));
            framePositions.add(basePos.add(3, y, 0));
        }
        for (int x = 1; x < 3; ++x) {
            framePositions.add(basePos.add(x, 0, 0));
            framePositions.add(basePos.add(x, 4, 0));
        }
        return framePositions;
    }

    private boolean canBuildPortalAtPosition(BlockPos pos) {
        for (int y = 0; y < 5; ++y) {
            for (int x = 0; x < 4; ++x) {
                BlockPos checkPos = pos.add(x, y, 0);
                if (BlockUtils.canPlace(checkPos, true) && !(this.mc.player.getEyePos().distanceTo(checkPos.toCenterPos()) > 6.0)) continue;
                return false;
            }
        }
        return true;
    }
}

