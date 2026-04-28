/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AnvilBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class Burrow
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Block> block;
    private final Setting<Boolean> instant;
    private final Setting<Boolean> automatic;
    private final Setting<Double> triggerHeight;
    private final Setting<Double> rubberbandHeight;
    private final Setting<Double> timer;
    private final Setting<Boolean> onlyInHole;
    private final Setting<Boolean> center;
    private final Setting<Boolean> rotate;
    private final BlockPos.Mutable blockPos;
    private boolean shouldBurrow;

    public Burrow() {
        super(Categories.Combat, "burrow", "Attempts to clip you into a block.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.block = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("block-to-use")).description("The block to use for Burrow.")).defaultValue(Block.EChest)).build());
        this.instant = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instant")).description("Jumps with packets rather than vanilla jump.")).defaultValue(true)).build());
        this.automatic = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("automatic")).description("Automatically burrows on activate rather than waiting for jump.")).defaultValue(true)).build());
        this.triggerHeight = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("trigger-height")).description("How high you have to jump before a rubberband is triggered.")).defaultValue(1.12).range(0.01, 1.4).sliderRange(0.01, 1.4).build());
        this.rubberbandHeight = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rubberband-height")).description("How far to attempt to cause rubberband.")).defaultValue(12.0).sliderMin(-30.0).sliderMax(30.0).build());
        this.timer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("timer")).description("Timer override.")).defaultValue(1.0).min(0.01).sliderRange(0.01, 10.0).build());
        this.onlyInHole = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-in-holes")).description("Stops you from burrowing when not in a hole.")).defaultValue(false)).build());
        this.center = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("center")).description("Centers you to the middle of the block before burrowing.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Faces the block you place server-side.")).defaultValue(true)).build());
        this.blockPos = new BlockPos.Mutable();
    }

    @Override
    public void onActivate() {
        if (!this.mc.world.getBlockState(this.mc.player.getBlockPos()).isReplaceable()) {
            this.error("Already burrowed, disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (!PlayerUtils.isInHole(false) && this.onlyInHole.get().booleanValue()) {
            this.error("Not in a hole, disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (!this.checkHead()) {
            this.error("Not enough headroom to burrow, disabling.", new Object[0]);
            this.toggle();
            return;
        }
        FindItemResult result = this.getItem();
        if (!result.isHotbar() && !result.isOffhand()) {
            this.error("No burrow block found, disabling.", new Object[0]);
            this.toggle();
            return;
        }
        this.blockPos.set((Vec3i)this.mc.player.getBlockPos());
        Modules.get().get(Timer.class).setOverride(this.timer.get());
        this.shouldBurrow = false;
        if (this.automatic.get().booleanValue()) {
            if (this.instant.get().booleanValue()) {
                this.shouldBurrow = true;
            } else {
                this.mc.player.jump();
            }
        } else {
            this.info("Waiting for manual jump.", new Object[0]);
        }
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(1.0);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.instant.get().booleanValue()) {
            boolean bl = this.shouldBurrow = this.mc.player.getY() > (double)this.blockPos.getY() + this.triggerHeight.get();
        }
        if (!this.shouldBurrow && this.instant.get().booleanValue()) {
            this.blockPos.set((Vec3i)this.mc.player.getBlockPos());
        }
        if (this.shouldBurrow) {
            if (this.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(this.mc.player.getBlockPos()), Rotations.getPitch(this.mc.player.getBlockPos()), 50, this::burrow);
            } else {
                this.burrow();
            }
            this.toggle();
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (this.instant.get().booleanValue() && !this.shouldBurrow) {
            if (event.action == KeyAction.Press && this.mc.options.jumpKey.matchesKey(event.key, 0)) {
                this.shouldBurrow = true;
            }
            this.blockPos.set((Vec3i)this.mc.player.getBlockPos());
        }
    }

    private void burrow() {
        if (this.center.get().booleanValue()) {
            PlayerUtils.centerPlayer();
        }
        if (this.instant.get().booleanValue()) {
            this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 0.4, this.mc.player.getZ(), false));
            this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 0.75, this.mc.player.getZ(), false));
            this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 1.01, this.mc.player.getZ(), false));
            this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 1.15, this.mc.player.getZ(), false));
        }
        FindItemResult block = this.getItem();
        if (!(this.mc.player.getInventory().getStack(block.slot()).getItem() instanceof BlockItem)) {
            return;
        }
        InvUtils.swap(block.slot(), true);
        this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d((BlockPos)this.blockPos), Direction.UP, (BlockPos)this.blockPos, false));
        this.mc.player.networkHandler.sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
        InvUtils.swapBack();
        if (this.instant.get().booleanValue()) {
            this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + this.rubberbandHeight.get(), this.mc.player.getZ(), false));
        } else {
            this.mc.player.updatePosition(this.mc.player.getX(), this.mc.player.getY() + this.rubberbandHeight.get(), this.mc.player.getZ());
        }
    }

    private FindItemResult getItem() {
        return switch (this.block.get().ordinal()) {
            case 0 -> InvUtils.findInHotbar(Items.ENDER_CHEST);
            case 2 -> InvUtils.findInHotbar(itemStack -> net.minecraft.block.Block.getBlockFromItem((Item)itemStack.getItem()) instanceof AnvilBlock);
            case 3 -> new FindItemResult(this.mc.player.getInventory().selectedSlot, this.mc.player.getMainHandStack().getCount());
            default -> InvUtils.findInHotbar(Items.OBSIDIAN, Items.CRYING_OBSIDIAN);
        };
    }

    private boolean checkHead() {
        BlockState blockState1 = this.mc.world.getBlockState((BlockPos)this.blockPos.set(this.mc.player.getX() + 0.3, this.mc.player.getY() + 2.3, this.mc.player.getZ() + 0.3));
        BlockState blockState2 = this.mc.world.getBlockState((BlockPos)this.blockPos.set(this.mc.player.getX() + 0.3, this.mc.player.getY() + 2.3, this.mc.player.getZ() - 0.3));
        BlockState blockState3 = this.mc.world.getBlockState((BlockPos)this.blockPos.set(this.mc.player.getX() - 0.3, this.mc.player.getY() + 2.3, this.mc.player.getZ() - 0.3));
        BlockState blockState4 = this.mc.world.getBlockState((BlockPos)this.blockPos.set(this.mc.player.getX() - 0.3, this.mc.player.getY() + 2.3, this.mc.player.getZ() + 0.3));
        boolean air1 = blockState1.isReplaceable();
        boolean air2 = blockState2.isReplaceable();
        boolean air3 = blockState3.isReplaceable();
        boolean air4 = blockState4.isReplaceable();
        return air1 && air2 && air3 && air4;
    }

    public static enum Block {
        EChest,
        Obsidian,
        Anvil,
        Held;

    }
}

