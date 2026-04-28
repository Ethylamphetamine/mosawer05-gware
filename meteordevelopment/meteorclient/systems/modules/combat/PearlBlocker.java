/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

public class PearlBlocker
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> offsetDistance;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Mode> mode;
    private final Map<Integer, BlockPos> tracked;
    private int placementsThisTick;

    public PearlBlocker() {
        super(Categories.Combat, "pearl-blocker", "Catch enemy pearls by placing obsidian/crystal in their path.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.offsetDistance = this.sgGeneral.add(((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-distance")).defaultValue(2.0).min(0.5).max(5.0).build());
        this.pauseOnEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-eat")).defaultValue(true)).build());
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).defaultValue(Mode.Hybrid)).build());
        this.tracked = new HashMap<Integer, BlockPos>();
        this.placementsThisTick = 0;
    }

    @Override
    public void onActivate() {
        this.tracked.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.placementsThisTick = 0;
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (this.pauseOnEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        for (Entity e : this.mc.world.getEntities()) {
            boolean nowHasSupport;
            BlockPos target;
            int id;
            Entity owner;
            EnderPearlEntity pearl;
            if (this.placementsThisTick >= 1) break;
            if (!(e instanceof EnderPearlEntity) || (pearl = (EnderPearlEntity)e).isRemoved() || !((owner = pearl.getOwner()) instanceof PlayerEntity)) continue;
            PlayerEntity playerOwner = (PlayerEntity)owner;
            if (owner == this.mc.player || Friends.get() != null && Friends.get().isFriend(playerOwner) || this.tracked.containsKey(id = pearl.getId()) || (target = this.findPlaceablePosInPath(pearl, playerOwner)) == null) continue;
            Mode currentMode = this.mode.get();
            if (currentMode == Mode.Hybrid) {
                currentMode = (double)playerOwner.distanceTo((Entity)this.mc.player) > 12.0 ? Mode.Crystal : Mode.Obsidian;
            }
            if (currentMode == Mode.Obsidian) {
                if (this.placeExactObsidian(target)) {
                    this.tracked.put(id, target);
                    ++this.placementsThisTick;
                    continue;
                }
                if (this.mc.world.getBlockState(target).isAir()) continue;
                this.tracked.put(id, target);
                continue;
            }
            if (currentMode != Mode.Crystal) continue;
            BlockPos support = target.down();
            boolean supportPresent = !this.mc.world.getBlockState(support).isAir() && !this.mc.world.getBlockState(support).getCollisionShape((BlockView)this.mc.world, support).isEmpty();
            boolean placedObs = false;
            if (!supportPresent && BlockUtils.canPlace(support) && (placedObs = this.placeObsidianWithSwap(support))) {
                ++this.placementsThisTick;
            }
            if (!(nowHasSupport = !this.mc.world.getBlockState(support).isAir() && !this.mc.world.getBlockState(support).getCollisionShape((BlockView)this.mc.world, support).isEmpty()) || !this.placeCrystalOnSupport(support)) continue;
            this.tracked.put(id, target);
            ++this.placementsThisTick;
        }
        this.tracked.entrySet().removeIf(entry -> {
            Entity found = this.mc.world.getEntityById(((Integer)entry.getKey()).intValue());
            return found == null || found.isRemoved();
        });
    }

    private boolean placeExactObsidian(BlockPos pos) {
        FindItemResult hot = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (hot.found()) {
            if (!MeteorClient.BLOCK.beginPlacement(List.of(pos), Items.OBSIDIAN)) {
                return false;
            }
            boolean ok = MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, pos);
            MeteorClient.BLOCK.endPlacement();
            return ok;
        }
        FindItemResult inv = InvUtils.find(Items.OBSIDIAN);
        if (!inv.found()) {
            return false;
        }
        int invSlot = inv.slot();
        int hotbarSlot = this.mc.player.getInventory().selectedSlot;
        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        boolean ok = false;
        if (MeteorClient.BLOCK.beginPlacement(List.of(pos), Items.OBSIDIAN)) {
            ok = MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, pos);
            MeteorClient.BLOCK.endPlacement();
        }
        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        return ok;
    }

    private boolean placeObsidianWithSwap(BlockPos pos) {
        return this.placeExactObsidian(pos);
    }

    private boolean placeCrystalOnSupport(BlockPos supportPos) {
        Vec3d hitVec = Vec3d.ofCenter((Vec3i)supportPos).add(0.0, 0.5, 0.0);
        BlockHitResult bhr = new BlockHitResult(hitVec, Direction.UP, supportPos, false);
        FindItemResult hot = InvUtils.findInHotbar(Items.END_CRYSTAL);
        boolean swappedFromInv = false;
        int invSlot = -1;
        int hotbarSlot = -1;
        if (!hot.found()) {
            FindItemResult inv = InvUtils.find(Items.END_CRYSTAL);
            if (!inv.found()) {
                return false;
            }
            invSlot = inv.slot();
            hotbarSlot = this.mc.player.getInventory().selectedSlot;
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
            swappedFromInv = true;
            hot = InvUtils.findInHotbar(Items.END_CRYSTAL);
            if (!hot.found()) {
                if (swappedFromInv) {
                    this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
                }
                return false;
            }
        }
        int oldSlot = this.mc.player.getInventory().selectedSlot;
        InvUtils.swap(hot.slot(), false);
        int seq = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, seq));
        InvUtils.swap(oldSlot, false);
        if (swappedFromInv) {
            hotbarSlot = this.mc.player.getInventory().selectedSlot;
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        }
        return true;
    }

    private BlockPos findPlaceablePosInPath(EnderPearlEntity pearl, PlayerEntity owner) {
        if (this.mc.world == null || this.mc.player == null) {
            return null;
        }
        Vec3d pos = pearl.getPos();
        Vec3d vel = pearl.getVelocity();
        double gravity = 0.03;
        double drag = 0.99;
        double step = 0.1;
        int maxSteps = 400;
        boolean ownerInRange = (double)owner.distanceTo((Entity)this.mc.player) <= 7.0;
        for (int i = 0; i < 400; ++i) {
            BlockPos blockPos = BlockPos.ofFloored((Position)pos);
            boolean withinOurRange = this.mc.player.getEyePos().distanceTo(pos) <= 5.1;
            boolean canPlace = BlockUtils.canPlace(blockPos);
            if (withinOurRange && canPlace) {
                if (ownerInRange) {
                    Vec3d forward = vel.normalize().multiply(this.offsetDistance.get().doubleValue());
                    BlockPos aheadPos = BlockPos.ofFloored((Position)pos.add(forward));
                    if (BlockUtils.canPlace(aheadPos)) {
                        return aheadPos;
                    }
                } else {
                    return blockPos;
                }
            }
            pos = pos.add(vel.multiply(0.1));
            vel = vel.multiply(Math.pow(0.99, 0.1));
            vel = vel.add(0.0, -0.003, 0.0);
            if (pos.y < (double)(this.mc.world.getBottomY() - 5)) break;
        }
        return null;
    }

    public static enum Mode {
        Obsidian,
        Crystal,
        Hybrid;

    }
}

