/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.IBaritone
 *  baritone.api.Settings
 *  baritone.api.pathing.goals.Goal
 *  baritone.api.pathing.goals.GoalBlock
 *  baritone.api.process.ICustomGoalProcess
 *  baritone.api.process.IMineProcess
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.FluidBlock
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.world;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.Settings;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.process.ICustomGoalProcess;
import baritone.api.process.IMineProcess;
import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;

public class InfinityMiner
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhenFull;
    public final Setting<List<Block>> targetBlocks;
    public final Setting<List<Item>> targetItems;
    public final Setting<List<Block>> repairBlocks;
    public final Setting<Double> startRepairing;
    public final Setting<Double> startMining;
    public final Setting<Boolean> walkHome;
    public final Setting<Boolean> logOut;
    private final IBaritone baritone;
    private final Settings baritoneSettings;
    private final BlockPos.Mutable homePos;
    private boolean prevMineScanDroppedItems;
    private boolean repairing;

    public InfinityMiner() {
        super(Categories.World, "infinity-miner", "Allows you to essentially mine forever by mining repair blocks when the durability gets low. Needs a mending pickaxe.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhenFull = this.settings.createGroup("When Full");
        this.targetBlocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("target-blocks")).description("The target blocks to mine.")).defaultValue(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE).filter(this::filterBlocks).build());
        this.targetItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("target-items")).description("The target items to collect.")).defaultValue(Items.DIAMOND).build());
        this.repairBlocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("repair-blocks")).description("The repair blocks to mine.")).defaultValue(Blocks.COAL_ORE, Blocks.REDSTONE_ORE, Blocks.NETHER_QUARTZ_ORE).filter(this::filterBlocks).build());
        this.startRepairing = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("repair-threshold")).description("The durability percentage at which to start repairing.")).defaultValue(20.0).range(1.0, 99.0).sliderRange(1.0, 99.0).build());
        this.startMining = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("mine-threshold")).description("The durability percentage at which to start mining.")).defaultValue(70.0).range(1.0, 99.0).sliderRange(1.0, 99.0).build());
        this.walkHome = this.sgWhenFull.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("walk-home")).description("Will walk 'home' when your inventory is full.")).defaultValue(false)).build());
        this.logOut = this.sgWhenFull.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("log-out")).description("Logs out when your inventory is full. Will walk home FIRST if walk home is enabled.")).defaultValue(false)).build());
        this.baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        this.baritoneSettings = BaritoneAPI.getSettings();
        this.homePos = new BlockPos.Mutable();
    }

    @Override
    public void onActivate() {
        this.prevMineScanDroppedItems = (Boolean)this.baritoneSettings.mineScanDroppedItems.value;
        this.baritoneSettings.mineScanDroppedItems.value = true;
        this.homePos.set((Vec3i)this.mc.player.getBlockPos());
        this.repairing = false;
    }

    @Override
    public void onDeactivate() {
        this.baritone.getPathingBehavior().cancelEverything();
        this.baritoneSettings.mineScanDroppedItems.value = this.prevMineScanDroppedItems;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.isFull()) {
            if (this.walkHome.get().booleanValue()) {
                if (this.isBaritoneNotWalking()) {
                    this.info("Walking home.", new Object[0]);
                    this.baritone.getCustomGoalProcess().setGoalAndPath((Goal)new GoalBlock((BlockPos)this.homePos));
                } else if (this.mc.player.getBlockPos().equals((Object)this.homePos) && this.logOut.get().booleanValue()) {
                    this.logOut();
                }
            } else if (this.logOut.get().booleanValue()) {
                this.logOut();
            } else {
                this.info("Inventory full, stopping process.", new Object[0]);
                this.toggle();
            }
            return;
        }
        if (!this.findPickaxe()) {
            this.error("Could not find a usable mending pickaxe.", new Object[0]);
            this.toggle();
            return;
        }
        if (!this.checkThresholds()) {
            this.error("Start mining value can't be lower than start repairing value.", new Object[0]);
            this.toggle();
            return;
        }
        if (this.repairing) {
            if (!this.needsRepair()) {
                this.warning("Finished repairing, going back to mining.", new Object[0]);
                this.repairing = false;
                this.mineTargetBlocks();
                return;
            }
            if (this.isBaritoneNotMining()) {
                this.mineRepairBlocks();
            }
        } else {
            if (this.needsRepair()) {
                this.warning("Pickaxe needs repair, beginning repair process", new Object[0]);
                this.repairing = true;
                this.mineRepairBlocks();
                return;
            }
            if (this.isBaritoneNotMining()) {
                this.mineTargetBlocks();
            }
        }
    }

    private boolean needsRepair() {
        ItemStack itemStack = this.mc.player.getMainHandStack();
        double toolPercentage = (float)(itemStack.getMaxDamage() - itemStack.getDamage()) * 100.0f / (float)itemStack.getMaxDamage();
        return !(toolPercentage > this.startMining.get() || toolPercentage > this.startRepairing.get() && !this.repairing);
    }

    private boolean findPickaxe() {
        Predicate<ItemStack> pickaxePredicate = stack -> stack.getItem() instanceof PickaxeItem && Utils.hasEnchantment(stack, (RegistryKey<Enchantment>)Enchantments.MENDING) && !Utils.hasEnchantment(stack, (RegistryKey<Enchantment>)Enchantments.SILK_TOUCH);
        FindItemResult bestPick = InvUtils.findInHotbar(pickaxePredicate);
        if (bestPick.isOffhand()) {
            InvUtils.shiftClick().fromOffhand().toHotbar(this.mc.player.getInventory().selectedSlot);
        } else if (bestPick.isHotbar()) {
            InvUtils.swap(bestPick.slot(), false);
        }
        return InvUtils.testInMainHand(pickaxePredicate);
    }

    private boolean checkThresholds() {
        return this.startRepairing.get() < this.startMining.get();
    }

    private void mineTargetBlocks() {
        Block[] array = new Block[this.targetBlocks.get().size()];
        this.baritone.getPathingBehavior().cancelEverything();
        this.baritone.getMineProcess().mine(this.targetBlocks.get().toArray(array));
    }

    private void mineRepairBlocks() {
        Block[] array = new Block[this.repairBlocks.get().size()];
        this.baritone.getPathingBehavior().cancelEverything();
        this.baritone.getMineProcess().mine(this.repairBlocks.get().toArray(array));
    }

    private void logOut() {
        this.toggle();
        this.mc.player.networkHandler.sendPacket((Packet)new DisconnectS2CPacket((Text)Text.literal((String)"[Infinity Miner] Inventory is full.")));
    }

    private boolean isBaritoneNotMining() {
        return !(this.baritone.getPathingControlManager().mostRecentInControl().orElse(null) instanceof IMineProcess);
    }

    private boolean isBaritoneNotWalking() {
        return !(this.baritone.getPathingControlManager().mostRecentInControl().orElse(null) instanceof ICustomGoalProcess);
    }

    private boolean filterBlocks(Block block) {
        return block != Blocks.AIR && block.getDefaultState().getHardness((BlockView)this.mc.world, null) != -1.0f && !(block instanceof FluidBlock);
    }

    private boolean isFull() {
        for (int i = 0; i <= 35; ++i) {
            ItemStack itemStack = this.mc.player.getInventory().getStack(i);
            if (itemStack.isEmpty()) {
                return false;
            }
            for (Item item : this.targetItems.get()) {
                if (itemStack.getItem() != item || itemStack.getCount() >= itemStack.getMaxCount()) continue;
                return false;
            }
        }
        return true;
    }
}

