/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BambooBlock
 *  net.minecraft.block.BambooShootBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.CropBlock
 *  net.minecraft.block.LeavesBlock
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.ToolComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ShearsItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.ToolItem
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.tag.BlockTags
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;

public class AutoTool
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final Setting<EnchantPreference> prefer;
    private final Setting<Boolean> silkTouchForEnderChest;
    private final Setting<Boolean> fortuneForOresCrops;
    private final Setting<Boolean> antiBreak;
    private final Setting<Integer> breakDurability;
    private final Setting<Boolean> switchBack;
    private final Setting<Integer> switchDelay;
    private final Setting<ListMode> listMode;
    private final Setting<List<Item>> whitelist;
    private final Setting<List<Item>> blacklist;
    private boolean wasPressed;
    private boolean shouldSwitch;
    private int ticks;
    private int bestSlot;

    public AutoTool() {
        super(Categories.Player, "auto-tool", "Automatically switches to the most effective tool when performing an action.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.prefer = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("prefer")).description("Either to prefer Silk Touch, Fortune, or none.")).defaultValue(EnchantPreference.Fortune)).build());
        this.silkTouchForEnderChest = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silk-touch-for-ender-chest")).description("Mines Ender Chests only with the Silk Touch enchantment.")).defaultValue(true)).build());
        this.fortuneForOresCrops = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fortune-for-ores-and-crops")).description("Mines Ores and crops only with the Fortune enchantment.")).defaultValue(false)).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Stops you from breaking your tool.")).defaultValue(false)).build());
        this.breakDurability = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("anti-break-percentage")).description("The durability percentage to stop using a tool.")).defaultValue(10)).range(1, 100).sliderRange(1, 100).visible(this.antiBreak::get)).build());
        this.switchBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("switch-back")).description("Switches your hand to whatever was selected when releasing your attack key.")).defaultValue(false)).build());
        this.switchDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("switch-delay")).description("Delay in ticks before switching tools.")).defaultValue(0)).build());
        this.listMode = this.sgWhitelist.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue(ListMode.Blacklist)).build());
        this.whitelist = this.sgWhitelist.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("whitelist")).description("The tools you want to use.")).visible(() -> this.listMode.get() == ListMode.Whitelist)).filter(AutoTool::isTool).build());
        this.blacklist = this.sgWhitelist.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("blacklist")).description("The tools you don't want to use.")).visible(() -> this.listMode.get() == ListMode.Blacklist)).filter(AutoTool::isTool).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Modules.get().isActive(InfinityMiner.class)) {
            return;
        }
        if (this.switchBack.get().booleanValue() && !this.mc.options.attackKey.isPressed() && this.wasPressed && InvUtils.previousSlot != -1) {
            InvUtils.swapBack();
            this.wasPressed = false;
            return;
        }
        if (this.ticks <= 0 && this.shouldSwitch && this.bestSlot != -1) {
            InvUtils.swap(this.bestSlot, this.switchBack.get());
            this.shouldSwitch = false;
        } else {
            --this.ticks;
        }
        this.wasPressed = this.mc.options.attackKey.isPressed();
    }

    @EventHandler(priority=100)
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (Modules.get().isActive(InfinityMiner.class)) {
            return;
        }
        BlockState blockState = this.mc.world.getBlockState(event.blockPos);
        if (!BlockUtils.canBreak(event.blockPos, blockState)) {
            return;
        }
        ItemStack currentStack = this.mc.player.getMainHandStack();
        double bestScore = -1.0;
        this.bestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            double score;
            ItemStack itemStack3 = this.mc.player.getInventory().getStack(i);
            if (this.listMode.get() == ListMode.Whitelist && !this.whitelist.get().contains(itemStack3.getItem()) || this.listMode.get() == ListMode.Blacklist && this.blacklist.get().contains(itemStack3.getItem()) || (score = AutoTool.getScore(itemStack3, blockState, this.silkTouchForEnderChest.get(), this.fortuneForOresCrops.get(), this.prefer.get(), itemStack2 -> !this.shouldStopUsing((ItemStack)itemStack2))) < 0.0 || !(score > bestScore)) continue;
            bestScore = score;
            this.bestSlot = i;
        }
        if (this.bestSlot != -1 && bestScore > AutoTool.getScore(currentStack, blockState, this.silkTouchForEnderChest.get(), this.fortuneForOresCrops.get(), this.prefer.get(), itemStack -> !this.shouldStopUsing((ItemStack)itemStack)) || this.shouldStopUsing(currentStack) || !AutoTool.isTool(currentStack)) {
            this.ticks = this.switchDelay.get();
            if (this.ticks == 0) {
                InvUtils.swap(this.bestSlot, true);
            } else {
                this.shouldSwitch = true;
            }
        }
        if (this.shouldStopUsing(currentStack = this.mc.player.getMainHandStack()) && AutoTool.isTool(currentStack)) {
            this.mc.options.attackKey.setPressed(false);
            event.cancel();
        }
    }

    private boolean shouldStopUsing(ItemStack itemStack) {
        return this.antiBreak.get() != false && itemStack.getMaxDamage() - itemStack.getDamage() < itemStack.getMaxDamage() * this.breakDurability.get() / 100;
    }

    public static double getScore(ItemStack itemStack, BlockState state, boolean silkTouchEnderChest, boolean fortuneOre, EnchantPreference enchantPreference, Predicate<ItemStack> good) {
        Item item;
        if (!good.test(itemStack) || !AutoTool.isTool(itemStack)) {
            return -1.0;
        }
        if (!(itemStack.isSuitableFor(state) || itemStack.getItem() instanceof SwordItem && (state.getBlock() instanceof BambooBlock || state.getBlock() instanceof BambooShootBlock) || itemStack.getItem() instanceof ShearsItem && state.getBlock() instanceof LeavesBlock || state.isIn(BlockTags.WOOL))) {
            return -1.0;
        }
        if (silkTouchEnderChest && state.getBlock() == Blocks.ENDER_CHEST && !Utils.hasEnchantments(itemStack, Enchantments.SILK_TOUCH)) {
            return -1.0;
        }
        if (fortuneOre && AutoTool.isFortunable(state.getBlock()) && !Utils.hasEnchantments(itemStack, Enchantments.FORTUNE)) {
            return -1.0;
        }
        double score = 0.0;
        score += (double)(itemStack.getMiningSpeedMultiplier(state) * 1000.0f);
        score += (double)Utils.getEnchantmentLevel(itemStack, (RegistryKey<Enchantment>)Enchantments.UNBREAKING);
        score += (double)Utils.getEnchantmentLevel(itemStack, (RegistryKey<Enchantment>)Enchantments.EFFICIENCY);
        score += (double)Utils.getEnchantmentLevel(itemStack, (RegistryKey<Enchantment>)Enchantments.MENDING);
        if (enchantPreference == EnchantPreference.Fortune) {
            score += (double)Utils.getEnchantmentLevel(itemStack, (RegistryKey<Enchantment>)Enchantments.FORTUNE);
        }
        if (enchantPreference == EnchantPreference.SilkTouch) {
            score += (double)Utils.getEnchantmentLevel(itemStack, (RegistryKey<Enchantment>)Enchantments.SILK_TOUCH);
        }
        if ((item = itemStack.getItem()) instanceof SwordItem) {
            SwordItem item2 = (SwordItem)item;
            if (state.getBlock() instanceof BambooBlock || state.getBlock() instanceof BambooShootBlock) {
                score += (double)(9000.0f + ((ToolComponent)item2.getComponents().get(DataComponentTypes.TOOL)).getSpeed(state) * 1000.0f);
            }
        }
        return score;
    }

    public static boolean isTool(Item item) {
        return item instanceof ToolItem || item instanceof ShearsItem;
    }

    public static boolean isTool(ItemStack itemStack) {
        return AutoTool.isTool(itemStack.getItem());
    }

    private static boolean isFortunable(Block block) {
        if (block == Blocks.ANCIENT_DEBRIS) {
            return false;
        }
        return Xray.ORES.contains(block) || block instanceof CropBlock;
    }

    public static enum EnchantPreference {
        None,
        Fortune,
        SilkTouch;

    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}

