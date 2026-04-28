/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractPressurePlateBlock
 *  net.minecraft.block.AirBlock
 *  net.minecraft.block.AnvilBlock
 *  net.minecraft.block.BedBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.BlockWithEntity
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ButtonBlock
 *  net.minecraft.block.CartographyTableBlock
 *  net.minecraft.block.CraftingTableBlock
 *  net.minecraft.block.DoorBlock
 *  net.minecraft.block.FenceGateBlock
 *  net.minecraft.block.GrindstoneBlock
 *  net.minecraft.block.LoomBlock
 *  net.minecraft.block.NoteBlock
 *  net.minecraft.block.ShapeContext
 *  net.minecraft.block.SlabBlock
 *  net.minecraft.block.StairsBlock
 *  net.minecraft.block.StonecutterBlock
 *  net.minecraft.block.TrapdoorBlock
 *  net.minecraft.block.enums.BlockHalf
 *  net.minecraft.block.enums.SlabType
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.effect.StatusEffectUtil
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.tag.FluidTags
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.util.shape.VoxelShapes
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.LightType
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.utils.world;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class BlockUtils {
    public static boolean breaking;
    private static boolean breakingThisTick;
    private static final ThreadLocal<BlockPos.Mutable> EXPOSED_POS;

    private BlockUtils() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BlockUtils.class);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority) {
        return BlockUtils.place(blockPos, findItemResult, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority) {
        return BlockUtils.place(blockPos, findItemResult, rotate, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {
        return BlockUtils.place(blockPos, findItemResult, rotate, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority, boolean checkEntities) {
        return BlockUtils.place(blockPos, findItemResult, true, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities) {
        return BlockUtils.place(blockPos, findItemResult, rotate, rotationPriority, swingHand, checkEntities, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (findItemResult.isOffhand()) {
            return BlockUtils.place(blockPos, Hand.OFF_HAND, MeteorClient.mc.player.getInventory().selectedSlot, rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        if (findItemResult.isHotbar()) {
            return BlockUtils.place(blockPos, Hand.MAIN_HAND, findItemResult.slot(), rotate, rotationPriority, swingHand, checkEntities, swapBack);
        }
        return false;
    }

    public static boolean place(BlockPos blockPos, Hand hand, int slot, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        BlockPos neighbour;
        if (slot < 0 || slot > 8) {
            return false;
        }
        Block toPlace = Blocks.OBSIDIAN;
        ItemStack i = hand == Hand.MAIN_HAND ? MeteorClient.mc.player.getInventory().getStack(slot) : MeteorClient.mc.player.getInventory().getStack(45);
        Item item = i.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            toPlace = blockItem.getBlock();
        }
        if (!BlockUtils.canPlaceBlock(blockPos, checkEntities, toPlace)) {
            return false;
        }
        Vec3d hitPos = Vec3d.ofCenter((Vec3i)blockPos);
        Direction side = BlockUtils.getPlaceSide(blockPos);
        if (side == null) {
            side = Direction.UP;
            neighbour = blockPos;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add((double)side.getOffsetX() * 0.5, (double)side.getOffsetY() * 0.5, (double)side.getOffsetZ() * 0.5);
        }
        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), rotationPriority, () -> {
                InvUtils.swap(slot, swapBack);
                BlockUtils.interact(bhr, hand, swingHand);
                if (swapBack) {
                    InvUtils.swapBack();
                }
            });
        } else {
            InvUtils.swap(slot, swapBack);
            BlockUtils.interact(bhr, hand, swingHand);
            if (swapBack) {
                InvUtils.swapBack();
            }
        }
        return true;
    }

    public static void interact(BlockHitResult blockHitResult, Hand hand, boolean swing) {
        boolean wasSneaking = MeteorClient.mc.player.input.sneaking;
        MeteorClient.mc.player.input.sneaking = false;
        ActionResult result = MeteorClient.mc.interactionManager.interactBlock(MeteorClient.mc.player, hand, blockHitResult);
        if (result.shouldSwingHand()) {
            if (swing) {
                MeteorClient.mc.player.swingHand(hand);
            } else {
                MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(hand));
            }
        }
        MeteorClient.mc.player.input.sneaking = wasSneaking;
    }

    public static boolean canPlaceBlock(BlockPos blockPos, boolean checkEntities, Block block) {
        if (blockPos == null) {
            return false;
        }
        if (!World.isValid((BlockPos)blockPos)) {
            return false;
        }
        if (!MeteorClient.mc.world.getBlockState(blockPos).isReplaceable()) {
            return false;
        }
        return !checkEntities || MeteorClient.mc.world.canPlace(block.getDefaultState(), blockPos, ShapeContext.absent());
    }

    public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        return BlockUtils.canPlaceBlock(blockPos, checkEntities, Blocks.OBSIDIAN);
    }

    public static boolean canPlace(BlockPos blockPos) {
        return BlockUtils.canPlace(blockPos, true);
    }

    public static Direction getPlaceSide(BlockPos blockPos) {
        Vec3d lookVec = blockPos.toCenterPos().subtract(MeteorClient.mc.player.getEyePos());
        double bestRelevancy = -1.7976931348623157E308;
        Direction bestSide = null;
        for (Direction side : Direction.values()) {
            double relevancy;
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = MeteorClient.mc.world.getBlockState(neighbor);
            if (state.isAir() || BlockUtils.isClickable(state.getBlock()) || !state.getFluidState().isEmpty() || !((relevancy = side.getAxis().choose(lookVec.getX(), lookVec.getY(), lookVec.getZ()) * (double)side.getDirection().offset()) > bestRelevancy)) continue;
            bestRelevancy = relevancy;
            bestSide = side;
        }
        return bestSide;
    }

    public static Direction getClosestPlaceSide(BlockPos blockPos) {
        return BlockUtils.getClosestPlaceSide(blockPos, MeteorClient.mc.player.getEyePos());
    }

    public static Direction getClosestPlaceSide(BlockPos blockPos, Vec3d pos) {
        Direction closestSide = null;
        double closestDistance = Double.MAX_VALUE;
        for (Direction side : Direction.values()) {
            double distance;
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = MeteorClient.mc.world.getBlockState(neighbor);
            if (state.isAir() || BlockUtils.isClickable(state.getBlock()) || !state.getFluidState().isEmpty() || !((distance = pos.squaredDistanceTo((double)neighbor.getX(), (double)neighbor.getY(), (double)neighbor.getZ())) < closestDistance)) continue;
            closestDistance = distance;
            closestSide = side;
        }
        return closestSide;
    }

    @EventHandler(priority=300)
    private static void onTickPre(TickEvent.Pre event) {
        breakingThisTick = false;
    }

    @EventHandler(priority=-300)
    private static void onTickPost(TickEvent.Post event) {
        if (!breakingThisTick && breaking) {
            breaking = false;
            if (MeteorClient.mc.interactionManager != null) {
                MeteorClient.mc.interactionManager.cancelBlockBreaking();
            }
        }
    }

    public static boolean breakBlock(BlockPos blockPos, boolean swing) {
        if (!BlockUtils.canBreak(blockPos, MeteorClient.mc.world.getBlockState(blockPos))) {
            return false;
        }
        BlockPos pos = blockPos instanceof BlockPos.Mutable ? new BlockPos((Vec3i)blockPos) : blockPos;
        InstantRebreak ir = Modules.get().get(InstantRebreak.class);
        if (ir != null && ir.isActive() && ir.blockPos.equals((Object)pos) && ir.shouldMine()) {
            ir.sendPacket();
            return true;
        }
        if (MeteorClient.mc.interactionManager.isBreakingBlock()) {
            MeteorClient.mc.interactionManager.updateBlockBreakingProgress(pos, BlockUtils.getDirection(blockPos));
        } else {
            MeteorClient.mc.interactionManager.attackBlock(pos, BlockUtils.getDirection(blockPos));
        }
        if (swing) {
            MeteorClient.mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        breaking = true;
        breakingThisTick = true;
        return true;
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!MeteorClient.mc.player.isCreative() && state.getHardness((BlockView)MeteorClient.mc.world, blockPos) < 0.0f) {
            return false;
        }
        return state.getOutlineShape((BlockView)MeteorClient.mc.world, blockPos) != VoxelShapes.empty();
    }

    public static boolean canBreak(BlockPos blockPos) {
        return BlockUtils.canBreak(blockPos, MeteorClient.mc.world.getBlockState(blockPos));
    }

    public static boolean canInstaBreak(BlockPos blockPos, float breakSpeed) {
        return MeteorClient.mc.player.isCreative() || BlockUtils.calcBlockBreakingDelta2(blockPos, breakSpeed) >= 1.0f;
    }

    public static boolean canInstaBreak(BlockPos blockPos) {
        BlockState state = MeteorClient.mc.world.getBlockState(blockPos);
        return BlockUtils.canInstaBreak(blockPos, MeteorClient.mc.player.getBlockBreakingSpeed(state));
    }

    public static float calcBlockBreakingDelta2(BlockPos blockPos, float breakSpeed) {
        BlockState state = MeteorClient.mc.world.getBlockState(blockPos);
        float f = state.getHardness((BlockView)MeteorClient.mc.world, blockPos);
        if (f == -1.0f) {
            return 0.0f;
        }
        int i = MeteorClient.mc.player.canHarvest(state) ? 30 : 100;
        return breakSpeed / f / (float)i;
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock || block instanceof GrindstoneBlock || block instanceof StonecutterBlock || block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock || block instanceof BlockWithEntity || block instanceof BedBlock || block instanceof FenceGateBlock || block instanceof DoorBlock || block instanceof NoteBlock || block instanceof TrapdoorBlock;
    }

    public static MobSpawn isValidMobSpawn(BlockPos blockPos, boolean newMobSpawnLightLevel) {
        return BlockUtils.isValidMobSpawn(blockPos, MeteorClient.mc.world.getBlockState(blockPos), newMobSpawnLightLevel ? 0 : 7);
    }

    public static MobSpawn isValidMobSpawn(BlockPos blockPos, BlockState blockState, int spawnLightLimit) {
        if (!(blockState.getBlock() instanceof AirBlock)) {
            return MobSpawn.Never;
        }
        BlockPos down = blockPos.down();
        BlockState downState = MeteorClient.mc.world.getBlockState(down);
        if (downState.getBlock() == Blocks.BEDROCK) {
            return MobSpawn.Never;
        }
        if (!BlockUtils.topSurface(downState)) {
            if (downState.getCollisionShape((BlockView)MeteorClient.mc.world, down) != VoxelShapes.fullCube()) {
                return MobSpawn.Never;
            }
            if (downState.isTransparent((BlockView)MeteorClient.mc.world, down)) {
                return MobSpawn.Never;
            }
        }
        if (MeteorClient.mc.world.getLightLevel(LightType.BLOCK, blockPos) > spawnLightLimit) {
            return MobSpawn.Never;
        }
        if (MeteorClient.mc.world.getLightLevel(LightType.SKY, blockPos) > spawnLightLimit) {
            return MobSpawn.Potential;
        }
        return MobSpawn.Always;
    }

    public static boolean topSurface(BlockState blockState) {
        if (blockState.getBlock() instanceof SlabBlock && blockState.get((Property)SlabBlock.TYPE) == SlabType.TOP) {
            return true;
        }
        return blockState.getBlock() instanceof StairsBlock && blockState.get((Property)StairsBlock.HALF) == BlockHalf.TOP;
    }

    public static Direction getDirection(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()), MeteorClient.mc.player.getZ());
        if ((double)pos.getY() > eyesPos.y) {
            if (MeteorClient.mc.world.getBlockState(pos.add(0, -1, 0)).isReplaceable()) {
                return Direction.DOWN;
            }
            return MeteorClient.mc.player.getHorizontalFacing().getOpposite();
        }
        if (!MeteorClient.mc.world.getBlockState(pos.add(0, 1, 0)).isReplaceable()) {
            return MeteorClient.mc.player.getHorizontalFacing().getOpposite();
        }
        return Direction.UP;
    }

    public static boolean isExposed(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (MeteorClient.mc.world.getBlockState((BlockPos)EXPOSED_POS.get().set((Vec3i)blockPos, direction)).isOpaque()) continue;
            return true;
        }
        return false;
    }

    public static double getBreakDelta(int slot, BlockState state) {
        float hardness = state.getHardness(null, null);
        if (hardness == -1.0f) {
            return 0.0;
        }
        return BlockUtils.getBlockBreakingSpeed(slot, state, MeteorClient.mc.player.isOnGround()) / (double)hardness / (double)(!state.isToolRequired() || MeteorClient.mc.player.getInventory().getStack(slot).isSuitableFor(state) ? 30 : 100);
    }

    public static double getBreakDelta(double breakingSpeed, BlockState state) {
        float hardness = state.getHardness(null, null);
        if (hardness == -1.0f) {
            return 0.0;
        }
        return breakingSpeed / (double)hardness / 30.0;
    }

    public static double getBlockBreakingSpeed(int slot, BlockState block, boolean isOnGround) {
        ItemStack tool;
        int efficiency;
        double speed = MeteorClient.mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(block);
        if (speed > 1.0 && (efficiency = Utils.getEnchantmentLevel(tool = MeteorClient.mc.player.getInventory().getStack(slot), (RegistryKey<Enchantment>)Enchantments.EFFICIENCY)) > 0 && !tool.isEmpty()) {
            speed += (double)(efficiency * efficiency + 1);
        }
        if (StatusEffectUtil.hasHaste((LivingEntity)MeteorClient.mc.player)) {
            speed *= (double)(1.0f + (float)(StatusEffectUtil.getHasteAmplifier((LivingEntity)MeteorClient.mc.player) + 1) * 0.2f);
        }
        if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k = switch (MeteorClient.mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1E-4f;
            };
            speed *= (double)k;
        }
        if (MeteorClient.mc.player.isSubmergedIn(FluidTags.WATER)) {
            speed *= MeteorClient.mc.player.getAttributeValue(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED);
        }
        if (!isOnGround) {
            speed /= 5.0;
        }
        return speed;
    }

    public static BlockPos.Mutable mutateAround(BlockPos.Mutable mutable, BlockPos origin, int xOffset, int yOffset, int zOffset) {
        return mutable.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
    }

    public static Iterable<BlockPos> iterate(Box box) {
        return BlockPos.iterate((int)MathHelper.floor((double)box.minX), (int)MathHelper.floor((double)box.minY), (int)MathHelper.floor((double)box.minZ), (int)MathHelper.floor((double)box.maxX), (int)MathHelper.floor((double)box.maxY), (int)MathHelper.floor((double)box.maxZ));
    }

    static {
        EXPOSED_POS = ThreadLocal.withInitial(BlockPos.Mutable::new);
    }

    public static enum MobSpawn {
        Never,
        Potential,
        Always;

    }
}

