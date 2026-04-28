/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractChestBlock
 *  net.minecraft.block.AbstractFurnaceBlock
 *  net.minecraft.block.AnvilBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.CraftingTableBlock
 *  net.minecraft.block.DispenserBlock
 *  net.minecraft.block.DropperBlock
 *  net.minecraft.block.EnderChestBlock
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.block.entity.Hopper
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.GameMenuScreen
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.Hopper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Offhand
extends Module {
    private final SettingGroup sgTotem;
    private final Setting<Boolean> antiGhost;
    private final Setting<Boolean> mainHandTotem;
    private final Setting<Integer> mainHandTotemSlot;
    private final Setting<Boolean> mainHandAutoselect;
    private final Setting<Integer> mainHandAutoselectHealth;
    private final Setting<Boolean> swordGapple;
    private int ignoreTicks;
    private int lastSwitchedSlot;
    private boolean isEatingGappleOnMainHand;
    private int mhOriginalSlot;
    private int mhMovedFromSlot;
    private boolean isEatingSword;
    private int swordOriginalSlot;
    private int swordMovedFromSlot;

    public Offhand() {
        super(Categories.Combat, "offhand", "Allows you to hold specified items in your offhand.");
        this.sgTotem = this.settings.createGroup("Totem");
        this.antiGhost = this.sgTotem.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-ghost")).description("Deletes your totem client side when you pop.")).defaultValue(false)).build());
        this.mainHandTotem = this.sgTotem.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("main-hand-totem")).description("Whether or not to hold a totem in your main hand.")).defaultValue(true)).build());
        this.mainHandTotemSlot = this.sgTotem.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("main-hand-totem-slot")).description("The slot in your hotbar to hold your main hand totem.")).defaultValue(3)).range(1, 9).visible(this.mainHandTotem::get)).build());
        this.mainHandAutoselect = this.sgTotem.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("main-hand-autoselect")).description("Automatically selects the main-hand totem slot at a certain health.")).defaultValue(false)).visible(this.mainHandTotem::get)).build());
        this.mainHandAutoselectHealth = this.sgTotem.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("main-hand-autoselect-health")).description("The health to automatically select the main-hand totem slot.")).defaultValue(10)).range(0, 20).sliderMax(20).visible(() -> this.mainHandTotem.get() != false && this.mainHandAutoselect.get() != false)).build());
        this.swordGapple = this.sgTotem.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sword-gapple")).description("Lets you right click while holding a sword to eat a golden apple (Swaps to Mainhand).")).defaultValue(true)).build());
        this.ignoreTicks = 0;
        this.lastSwitchedSlot = -1;
        this.isEatingGappleOnMainHand = false;
        this.mhOriginalSlot = -1;
        this.mhMovedFromSlot = -1;
        this.isEatingSword = false;
        this.swordOriginalSlot = -1;
        this.swordMovedFromSlot = -1;
    }

    @EventHandler(priority=1199)
    private void onTick(TickEvent.Pre event) {
        int totalTotems;
        if (this.ignoreTicks > 0) {
            --this.ignoreTicks;
        }
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (!(this.mc.currentScreen == null || this.mc.currentScreen instanceof ChatScreen || this.mc.currentScreen instanceof InventoryScreen || this.mc.currentScreen instanceof GameMenuScreen)) {
            this.isEatingSword = false;
            return;
        }
        if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            return;
        }
        FindItemResult totemCheck = InvUtils.find(Items.TOTEM_OF_UNDYING);
        boolean hasTotems = totemCheck.found();
        boolean holdingWeapon = this.mc.player.getMainHandStack().getItem() instanceof SwordItem || this.mc.player.getMainHandStack().getItem() instanceof PickaxeItem;
        boolean rightClickPressed = this.mc.options.useKey.isPressed();
        boolean inBedrock = this.isTrappedInBedrock() && this.mc.player.getHealth() > 10.0f;
        boolean shouldEatOffhand = this.swordGapple.get() != false && holdingWeapon && rightClickPressed && inBedrock && !this.willInteractWithChestBlock();
        this.updateOffhandSlot(hasTotems, shouldEatOffhand);
        if (!hasTotems) {
            this.isEatingSword = false;
            this.isEatingGappleOnMainHand = false;
            return;
        }
        if (this.swordGapple.get().booleanValue() && !shouldEatOffhand) {
            FindItemResult gapple;
            if (!this.isEatingSword && rightClickPressed && holdingWeapon && !this.willInteractWithChestBlock() && (gapple = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)).found()) {
                this.swordOriginalSlot = this.mc.player.getInventory().selectedSlot;
                if (gapple.isHotbar()) {
                    InvUtils.swap(gapple.slot(), false);
                    this.swordMovedFromSlot = -1;
                } else {
                    this.swordMovedFromSlot = gapple.slot();
                    InvUtils.move().from(gapple.slot()).toHotbar(this.swordOriginalSlot);
                }
                this.isEatingSword = true;
            }
            if (this.isEatingSword && !rightClickPressed) {
                if (this.swordMovedFromSlot != -1) {
                    InvUtils.move().from(this.swordOriginalSlot).to(this.swordMovedFromSlot);
                    this.swordMovedFromSlot = -1;
                }
                if (this.swordOriginalSlot != -1) {
                    InvUtils.swap(this.swordOriginalSlot, false);
                } else {
                    FindItemResult weapon = InvUtils.findInHotbar(item -> item.getItem() instanceof SwordItem || item.getItem() instanceof PickaxeItem);
                    if (weapon.found()) {
                        InvUtils.swap(weapon.slot(), false);
                    }
                }
                this.isEatingSword = false;
                this.swordOriginalSlot = -1;
            }
        } else if (this.isEatingSword) {
            if (this.swordMovedFromSlot != -1) {
                InvUtils.move().from(this.swordOriginalSlot).to(this.swordMovedFromSlot);
                this.swordMovedFromSlot = -1;
            }
            if (this.swordOriginalSlot != -1) {
                InvUtils.swap(this.swordOriginalSlot, false);
            }
            this.isEatingSword = false;
            this.swordOriginalSlot = -1;
        }
        boolean stopMainHand = false;
        if (this.mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING && (totalTotems = InvUtils.find(Items.TOTEM_OF_UNDYING).count()) <= 1) {
            stopMainHand = true;
        }
        if (this.mainHandTotem.get().booleanValue() && !this.isEatingSword && !this.isEatingGappleOnMainHand && !stopMainHand) {
            this.updateMainHandTotem();
        }
        if (this.mainHandTotem.get().booleanValue() && this.mainHandAutoselect.get().booleanValue() && !this.isEatingSword && !this.isEatingGappleOnMainHand && !stopMainHand) {
            this.updateMainHandAutoselect();
        }
        if (this.mainHandTotem.get().booleanValue() && !this.isEatingSword && !stopMainHand) {
            this.updateMainHandGapple();
        }
    }

    @EventHandler(priority=100)
    private void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet;
        if (this.ignoreTicks > 0 && (packet = event.packet) instanceof ScreenHandlerSlotUpdateS2CPacket) {
            ScreenHandlerSlotUpdateS2CPacket pkt = (ScreenHandlerSlotUpdateS2CPacket)packet;
            if (this.antiGhost.get().booleanValue() && (pkt.getSlot() == 45 || pkt.getSlot() == this.lastSwitchedSlot) && this.ignoreTicks > 0) {
                event.cancel();
            }
            return;
        }
        packet = event.packet;
        if (packet instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket p = (EntityStatusS2CPacket)packet;
            if (p.getStatus() != 35) {
                return;
            }
            Entity entity = p.getEntity((World)this.mc.world);
            if (entity == null || !entity.equals((Object)this.mc.player)) {
                return;
            }
            if (this.antiGhost.get().booleanValue()) {
                this.mc.player.getInventory().removeStack(45);
                this.ignoreTicks = 6;
                this.updateOffhandSlot(InvUtils.find(Items.TOTEM_OF_UNDYING).found(), false);
            }
        }
    }

    private void updateMainHandTotem() {
        if (this.isEatingGappleOnMainHand) {
            return;
        }
        FindItemResult totemResult = this.findTotem();
        if (!totemResult.found() || totemResult.isOffhand()) {
            return;
        }
        int targetSlot = this.mainHandTotemSlot.get() - 1;
        if (this.mc.player.getInventory().getStack(targetSlot).getItem() != Items.TOTEM_OF_UNDYING) {
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, SlotUtils.indexToId(totemResult.slot()), targetSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        }
    }

    private void updateMainHandAutoselect() {
        boolean isLowHealth = this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions(true, true) <= (float)this.mainHandAutoselectHealth.get().intValue();
        int targetSlot = this.mainHandTotemSlot.get() - 1;
        if (isLowHealth && this.mc.player.getInventory().selectedSlot != targetSlot && this.mc.player.getInventory().getStack(targetSlot).getItem() == Items.TOTEM_OF_UNDYING) {
            this.mc.player.getInventory().selectedSlot = targetSlot;
        }
    }

    private void updateMainHandGapple() {
        int totemSlotIndex = this.mainHandTotemSlot.get() - 1;
        if (this.isEatingGappleOnMainHand) {
            if (!this.mc.options.useKey.isPressed()) {
                if (this.mhMovedFromSlot != -1) {
                    InvUtils.move().from(this.mhOriginalSlot).to(this.mhMovedFromSlot);
                    this.mhMovedFromSlot = -1;
                }
                if (this.mhOriginalSlot != -1) {
                    InvUtils.swap(this.mhOriginalSlot, false);
                }
                this.isEatingGappleOnMainHand = false;
                this.mhOriginalSlot = -1;
            }
            return;
        }
        boolean isTotemSlotSelected = this.mc.player.getInventory().selectedSlot == totemSlotIndex;
        boolean isTotemInSlot = this.mc.player.getInventory().getStack(totemSlotIndex).getItem() == Items.TOTEM_OF_UNDYING;
        boolean usePressed = this.mc.options.useKey.isPressed();
        if (isTotemSlotSelected && isTotemInSlot && usePressed) {
            if (this.willInteractWithChestBlock()) {
                return;
            }
            FindItemResult gappleResult = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE);
            if (gappleResult.found()) {
                this.mhOriginalSlot = this.mc.player.getInventory().selectedSlot;
                if (gappleResult.isHotbar()) {
                    InvUtils.swap(gappleResult.slot(), false);
                    this.mhMovedFromSlot = -1;
                } else {
                    this.mhMovedFromSlot = gappleResult.slot();
                    InvUtils.move().from(gappleResult.slot()).toHotbar(this.mhOriginalSlot);
                }
                this.isEatingGappleOnMainHand = true;
            }
        }
    }

    private void updateOffhandSlot(boolean hasTotems, boolean forceGapple) {
        if (forceGapple) {
            FindItemResult gapple;
            Item currentOffhand = this.mc.player.getOffHandStack().getItem();
            if (currentOffhand != Items.ENCHANTED_GOLDEN_APPLE && currentOffhand != Items.GOLDEN_APPLE && (gapple = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)).found()) {
                this.moveItemToOffhand(gapple);
            }
        } else if (hasTotems) {
            FindItemResult totem;
            if (this.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING && (totem = this.findTotem()).found()) {
                this.moveItemToOffhand(totem);
            }
        } else {
            FindItemResult gapple;
            Item currentOffhand = this.mc.player.getOffHandStack().getItem();
            if (currentOffhand != Items.ENCHANTED_GOLDEN_APPLE && currentOffhand != Items.GOLDEN_APPLE && (gapple = InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE)).found()) {
                this.moveItemToOffhand(gapple);
            }
        }
    }

    private void updateOffhandSlot(boolean hasTotems) {
        this.updateOffhandSlot(hasTotems, false);
    }

    private void moveItemToOffhand(FindItemResult result) {
        this.lastSwitchedSlot = result.slot();
        if (result.isHotbar()) {
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, 45, result.slot(), SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        } else {
            InvUtils.move().from(result.slot()).toOffhand();
        }
    }

    private boolean isTrappedInBedrock() {
        boolean feetInBedrock;
        BlockPos pos = this.mc.player.getBlockPos();
        boolean bl = feetInBedrock = this.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
        if (!feetInBedrock) {
            return false;
        }
        if (this.mc.player.isInSwimmingPose() || this.mc.player.isCrawling()) {
            return true;
        }
        return this.mc.world.getBlockState(pos.up()).getBlock() == Blocks.BEDROCK;
    }

    private boolean willInteractWithChestBlock() {
        if (this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)this.mc.crosshairTarget;
            BlockState blockState = this.mc.world.getBlockState(blockHitResult.getBlockPos());
            Block block = blockState.getBlock();
            return block instanceof ShulkerBoxBlock || block instanceof AbstractChestBlock || block instanceof EnderChestBlock || block instanceof AbstractFurnaceBlock || block instanceof DropperBlock || block instanceof Hopper || block instanceof DispenserBlock || block instanceof CraftingTableBlock || block instanceof AnvilBlock;
        }
        return false;
    }

    private FindItemResult findTotem() {
        return InvUtils.find(Items.TOTEM_OF_UNDYING);
    }
}

