/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class EChestBlocker
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<Boolean> blockShulkers;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Double> friendDetectRange;
    private final Setting<Boolean> pauseOnEat;
    private final Set<BlockPos> knownBlocks;
    private final Set<BlockPos> friendlyBlocks;
    private final Set<BlockPos> blockedBlocks;

    public EChestBlocker() {
        super(Categories.Combat, "e-chest-blocker", "Places obsidian on top of enemy Ender Chests and Shulker Boxes.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The radius to search for and block Ender Chests.")).defaultValue(10.0).min(0.0).max(30.0).build());
        this.blockShulkers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("block-shulkers")).description("Also blocks Shulker Boxes.")).defaultValue(true)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores Ender Chests placed by friends or yourself.")).defaultValue(true)).build());
        this.friendDetectRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("friend-detect-range")).description("The radius around a *new* Ender Chest to check for friends.")).defaultValue(4.5).min(0.0).max(8.0).build());
        this.pauseOnEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-eat")).description("Stops placing obsidian while eating.")).defaultValue(true)).build());
        this.knownBlocks = new HashSet<BlockPos>();
        this.friendlyBlocks = new HashSet<BlockPos>();
        this.blockedBlocks = new HashSet<BlockPos>();
    }

    @Override
    public void onActivate() {
        this.knownBlocks.clear();
        this.friendlyBlocks.clear();
        this.blockedBlocks.clear();
    }

    private boolean isBlockableItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Block block = Block.getBlockFromItem((Item)stack.getItem());
        return block == Blocks.ENDER_CHEST || this.blockShulkers.get() != false && block instanceof ShulkerBoxBlock;
    }

    private boolean isBlockableBlock(Block block) {
        return block == Blocks.ENDER_CHEST || this.blockShulkers.get() != false && block instanceof ShulkerBoxBlock;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        Packet<?> packet;
        if (!this.ignoreFriends.get().booleanValue()) {
            return;
        }
        if (this.mc.player == null || !((packet = event.packet) instanceof PlayerInteractBlockC2SPacket)) {
            return;
        }
        PlayerInteractBlockC2SPacket packet2 = (PlayerInteractBlockC2SPacket)packet;
        if (this.isBlockableItem(this.mc.player.getStackInHand(packet2.getHand()))) {
            BlockHitResult hitResult = packet2.getBlockHitResult();
            BlockPos placedPos = hitResult.getBlockPos().offset(hitResult.getSide());
            this.friendlyBlocks.add(placedPos.toImmutable());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        ItemStack mainHand = this.mc.player.getMainHandStack();
        ItemStack offHand = this.mc.player.getOffHandStack();
        if (mainHand.isEmpty() || this.isBlockableItem(mainHand) || offHand.isEmpty() || this.isBlockableItem(offHand)) {
            return;
        }
        if (this.pauseOnEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        BlockPos playerPos = this.mc.player.getBlockPos();
        double r = this.range.get();
        int rCeil = (int)Math.ceil(r);
        HashSet<BlockPos> currentBlocksInRange = new HashSet<BlockPos>();
        for (BlockPos pos : BlockPos.iterate((BlockPos)playerPos.add(-rCeil, -rCeil, -rCeil), (BlockPos)playerPos.add(rCeil, rCeil, rCeil))) {
            if (!(playerPos.getSquaredDistance((Vec3i)pos) <= r * r) || !this.isBlockableBlock(this.mc.world.getBlockState(pos).getBlock())) continue;
            currentBlocksInRange.add(pos.toImmutable());
        }
        HashSet newBlocks = new HashSet(currentBlocksInRange);
        newBlocks.removeAll(this.knownBlocks);
        if (this.ignoreFriends.get().booleanValue() && !newBlocks.isEmpty()) {
            for (BlockPos newBlockPos : newBlocks) {
                if (this.friendlyBlocks.contains(newBlockPos)) continue;
                boolean isFriendNearby = false;
                for (PlayerEntity player : this.mc.world.getPlayers()) {
                    if (player == this.mc.player || Friends.get() == null || !Friends.get().isFriend(player) || !player.getBlockPos().isWithinDistance((Vec3i)newBlockPos, this.friendDetectRange.get().doubleValue())) continue;
                    isFriendNearby = true;
                    break;
                }
                if (!isFriendNearby) continue;
                this.friendlyBlocks.add(newBlockPos);
            }
        }
        this.knownBlocks.addAll(newBlocks);
        for (BlockPos pos : currentBlocksInRange) {
            BlockPos targetPos;
            if (this.friendlyBlocks.contains(pos) || this.blockedBlocks.contains(pos)) continue;
            BlockState blockState = this.mc.world.getBlockState(pos);
            Block block = blockState.getBlock();
            if (block instanceof ShulkerBoxBlock) {
                Direction facing = (Direction)blockState.get((Property)ShulkerBoxBlock.FACING);
                targetPos = pos.offset(facing);
            } else {
                if (block != Blocks.ENDER_CHEST) continue;
                targetPos = pos.up();
            }
            if (!this.mc.world.getBlockState(targetPos).isAir()) {
                this.blockedBlocks.add(pos);
                continue;
            }
            boolean isEnemyNearby = false;
            double enemyRangeVal = 5.0;
            double enemyRangeSq = 25.0;
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (player == this.mc.player || Friends.get() != null && Friends.get().isFriend(player) || !(player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 25.0)) continue;
                isEnemyNearby = true;
                break;
            }
            if (!isEnemyNearby || !BlockUtils.canPlace(targetPos)) continue;
            FindItemResult hot = InvUtils.findInHotbar(Items.OBSIDIAN);
            FindItemResult inv = InvUtils.find(Items.OBSIDIAN);
            if (!hot.found() && !inv.found() || !this.placeObsidian(targetPos)) continue;
            this.blockedBlocks.add(pos);
        }
        this.pruneSet(this.knownBlocks);
        this.pruneSet(this.friendlyBlocks);
        this.pruneSet(this.blockedBlocks);
    }

    private void pruneSet(Set<BlockPos> set) {
        if (set.isEmpty() || this.mc.world == null) {
            return;
        }
        set.removeIf(pos -> !this.isBlockableBlock(this.mc.world.getBlockState(pos).getBlock()));
    }

    private boolean placeObsidian(BlockPos pos) {
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
        if (!PacketManager.INSTANCE.isClickAllowed()) {
            return false;
        }
        PacketManager.INSTANCE.incrementClick();
        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        boolean ok = false;
        if (MeteorClient.BLOCK.beginPlacement(List.of(pos), Items.OBSIDIAN)) {
            ok = MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, pos);
            MeteorClient.BLOCK.endPlacement();
        }
        PacketManager.INSTANCE.incrementClick();
        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        return ok;
    }
}

