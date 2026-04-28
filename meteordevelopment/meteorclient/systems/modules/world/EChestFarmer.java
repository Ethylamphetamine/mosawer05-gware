/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.shape.VoxelShape
 */
package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class EChestFarmer
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> selfToggle;
    private final Setting<Boolean> ignoreExisting;
    private final Setting<Integer> amount;
    private final Setting<Boolean> swingHand;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final VoxelShape SHAPE;
    private BlockPos target;
    private int startCount;

    public EChestFarmer() {
        super(Categories.World, "echest-farmer", "Places and breaks EChests to farm obsidian.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.selfToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("self-toggle")).description("Disables when you reach the desired amount of obsidian.")).defaultValue(false)).build());
        this.ignoreExisting = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-existing")).description("Ignores existing obsidian in your inventory and mines the total target amount.")).defaultValue(true)).visible(this.selfToggle::get)).build());
        this.amount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("amount")).description("The amount of obsidian to farm.")).defaultValue(64)).sliderMax(128).range(8, 512).sliderRange(8, 128).visible(this.selfToggle::get)).build());
        this.swingHand = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing-hand")).description("Swing hand client-side.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders a block overlay where the obsidian will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The color of the sides of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 50)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color of the lines of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.SHAPE = Block.createCuboidShape((double)1.0, (double)0.0, (double)1.0, (double)15.0, (double)14.0, (double)15.0);
    }

    @Override
    public void onActivate() {
        this.target = null;
        this.startCount = InvUtils.find(Items.OBSIDIAN).count();
    }

    @Override
    public void onDeactivate() {
        InvUtils.swapBack();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target == null) {
            if (this.mc.crosshairTarget == null || this.mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
                return;
            }
            BlockPos pos = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos().up();
            BlockState state = this.mc.world.getBlockState(pos);
            if (state.isReplaceable() || state.getBlock() == Blocks.ENDER_CHEST) {
                this.target = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos().up();
            } else {
                return;
            }
        }
        if (!PlayerUtils.isWithinReach(this.target)) {
            this.error("Target block pos out of reach.", new Object[0]);
            this.target = null;
            return;
        }
        if (this.selfToggle.get().booleanValue() && InvUtils.find(Items.OBSIDIAN).count() - (this.ignoreExisting.get() != false ? this.startCount : 0) >= this.amount.get()) {
            InvUtils.swapBack();
            this.toggle();
            return;
        }
        if (this.mc.world.getBlockState(this.target).getBlock() == Blocks.ENDER_CHEST) {
            double bestScore = -1.0;
            int bestSlot = -1;
            for (int i = 0; i < 9; ++i) {
                double score;
                ItemStack itemStack = this.mc.player.getInventory().getStack(i);
                if (Utils.hasEnchantment(itemStack, (RegistryKey<Enchantment>)Enchantments.SILK_TOUCH) || !((score = (double)itemStack.getMiningSpeedMultiplier(Blocks.ENDER_CHEST.getDefaultState())) > bestScore)) continue;
                bestScore = score;
                bestSlot = i;
            }
            if (bestSlot == -1) {
                return;
            }
            InvUtils.swap(bestSlot, true);
            BlockUtils.breakBlock(this.target, this.swingHand.get());
        }
        if (this.mc.world.getBlockState(this.target).isReplaceable()) {
            FindItemResult echest = InvUtils.findInHotbar(Items.ENDER_CHEST);
            if (!echest.found()) {
                this.error("No Echests in hotbar, disabling", new Object[0]);
                this.toggle();
                return;
            }
            BlockUtils.place(this.target, echest, true, 0, true);
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.target == null || !this.render.get().booleanValue()) {
            return;
        }
        Box box = (Box)this.SHAPE.getBoundingBoxes().getFirst();
        event.renderer.box((double)this.target.getX() + box.minX, (double)this.target.getY() + box.minY, (double)this.target.getZ() + box.minZ, (double)this.target.getX() + box.maxX, (double)this.target.getY() + box.maxY, (double)this.target.getZ() + box.maxZ, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
    }
}

