/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractPressurePlateBlock
 *  net.minecraft.block.AbstractRailBlock
 *  net.minecraft.block.AbstractRedstoneGateBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ButtonBlock
 *  net.minecraft.block.CarpetBlock
 *  net.minecraft.block.LeverBlock
 *  net.minecraft.block.SlabBlock
 *  net.minecraft.block.TransparentBlock
 *  net.minecraft.block.TripwireBlock
 *  net.minecraft.item.Item
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class SpawnProofer
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> range;
    private final Setting<List<Block>> blocks;
    private final Setting<Integer> delay;
    private final Setting<Boolean> rotate;
    private final Setting<Mode> mode;
    private final Setting<Boolean> newMobSpawnLightLevel;
    private final Pool<BlockPos.Mutable> spawnPool;
    private final List<BlockPos.Mutable> spawns;
    private int ticksWaited;

    public SpawnProofer() {
        super(Categories.World, "spawn-proofer", "Automatically spawnproofs unlit areas.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("range")).description("Range for block placement and rendering")).defaultValue(3)).min(0).build());
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Block to use for spawn proofing")).defaultValue(Blocks.TORCH, Blocks.STONE_BUTTON, Blocks.STONE_SLAB).filter(this::filterBlocks).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay in ticks between placing blocks")).defaultValue(0)).min(0).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates towards the blocks being placed.")).defaultValue(true)).build());
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which spawn types should be spawn proofed.")).defaultValue(Mode.Both)).build());
        this.newMobSpawnLightLevel = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("new-mob-spawn-light-level")).description("Use the new (1.18+) mob spawn behavior")).defaultValue(true)).build());
        this.spawnPool = new Pool<BlockPos.Mutable>(BlockPos.Mutable::new);
        this.spawns = new ArrayList<BlockPos.Mutable>();
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (this.delay.get() != 0 && this.ticksWaited < this.delay.get() - 1) {
            return;
        }
        boolean foundBlock = InvUtils.testInHotbar(itemStack -> this.blocks.get().contains(Block.getBlockFromItem((Item)itemStack.getItem())));
        if (!foundBlock) {
            this.error("Found none of the chosen blocks in hotbar", new Object[0]);
            this.toggle();
            return;
        }
        for (BlockPos.Mutable blockPos2 : this.spawns) {
            this.spawnPool.free(blockPos2);
        }
        this.spawns.clear();
        int lightLevel = this.newMobSpawnLightLevel.get() != false ? 0 : 7;
        BlockIterator.register(this.range.get(), this.range.get(), (blockPos, blockState) -> {
            BlockUtils.MobSpawn spawn = BlockUtils.isValidMobSpawn(blockPos, blockState, lightLevel);
            if (spawn == BlockUtils.MobSpawn.Always && (this.mode.get() == Mode.Always || this.mode.get() == Mode.Both) || spawn == BlockUtils.MobSpawn.Potential && (this.mode.get() == Mode.Potential || this.mode.get() == Mode.Both)) {
                this.spawns.add(this.spawnPool.get().set((Vec3i)blockPos));
            }
        });
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (this.delay.get() != 0 && this.ticksWaited < this.delay.get() - 1) {
            ++this.ticksWaited;
            return;
        }
        if (this.spawns.isEmpty()) {
            return;
        }
        FindItemResult block = InvUtils.findInHotbar(itemStack -> this.blocks.get().contains(Block.getBlockFromItem((Item)itemStack.getItem())));
        if (!block.found()) {
            this.error("Found none of the chosen blocks in hotbar", new Object[0]);
            this.toggle();
            return;
        }
        if (this.delay.get() == 0) {
            for (BlockPos blockPos : this.spawns) {
                BlockUtils.place(blockPos, block, this.rotate.get(), -50, false);
            }
        } else if (this.isLightSource(Block.getBlockFromItem((Item)this.mc.player.getInventory().getStack(block.slot()).getItem()))) {
            int lowestLightLevel = 16;
            BlockPos.Mutable mutable = this.spawns.getFirst();
            for (BlockPos blockPos : this.spawns) {
                int lightLevel = this.mc.world.getLightLevel(blockPos);
                if (lightLevel >= lowestLightLevel) continue;
                lowestLightLevel = lightLevel;
                mutable.set((Vec3i)blockPos);
            }
            BlockUtils.place((BlockPos)mutable, block, this.rotate.get(), -50, false);
        } else {
            BlockUtils.place((BlockPos)this.spawns.getFirst(), block, this.rotate.get(), -50, false);
        }
        this.ticksWaited = 0;
    }

    private boolean filterBlocks(Block block) {
        return this.isNonOpaqueBlock(block) || this.isLightSource(block);
    }

    private boolean isNonOpaqueBlock(Block block) {
        return block instanceof ButtonBlock || block instanceof SlabBlock || block instanceof AbstractPressurePlateBlock || block instanceof TransparentBlock || block instanceof TripwireBlock || block instanceof CarpetBlock || block instanceof LeverBlock || block instanceof AbstractRedstoneGateBlock || block instanceof AbstractRailBlock;
    }

    private boolean isLightSource(Block block) {
        return block.getDefaultState().getLuminance() > 0;
    }

    public static enum Mode {
        Always,
        Potential,
        Both,
        None;

    }
}

