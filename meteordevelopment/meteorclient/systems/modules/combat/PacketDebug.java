/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.StreamSupport;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PacketDebug
extends Module {
    private final SettingGroup sgInteract;
    private final SettingGroup sgInventory;
    private final SettingGroup sgAction;
    private final SettingGroup sgMovement;
    private final SettingGroup sgSimulation;
    private final Setting<Integer> swingPPS;
    private final Setting<Integer> interactItemPPS;
    private final Setting<Integer> interactBlockPPS;
    private final Setting<Integer> interactEntityPPS;
    private final Setting<Integer> updateSlotPPS;
    private final Setting<Integer> hotbarScrollPPS;
    private final Setting<Integer> inventorySwapPPS;
    private final Setting<Integer> closeScreenPPS;
    private final Setting<Integer> actionPPS;
    private final Setting<Integer> commandPPS;
    private final Setting<Integer> rotationPPS;
    private final Setting<Integer> conflictPPS;
    private final Setting<Boolean> includeMining;
    private final Random random;

    public PacketDebug() {
        super(Categories.Combat, "packet-debug", "Spams dummy packets to test anticheat limits/kicks.");
        this.sgInteract = this.settings.createGroup("Interact Packets");
        this.sgInventory = this.settings.createGroup("Inventory Packets");
        this.sgAction = this.settings.createGroup("Action Packets");
        this.sgMovement = this.settings.createGroup("Movement Packets");
        this.sgSimulation = this.settings.createGroup("Conflict Simulation");
        this.swingPPS = this.sgInteract.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("swing-pps")).description("HandSwingC2SPacket per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.interactItemPPS = this.sgInteract.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("interact-item-pps")).description("PlayerInteractItemC2SPacket per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.interactBlockPPS = this.sgInteract.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("interact-block-pps")).description("PlayerInteractBlockC2SPacket per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.interactEntityPPS = this.sgInteract.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("interact-entity-pps")).description("PlayerInteractEntityC2SPacket (Attack Real Entity) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.updateSlotPPS = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("update-slot-pps")).description("UpdateSelectedSlotC2SPacket (Resends current slot) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.hotbarScrollPPS = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hotbar-scroll-pps")).description("UpdateSelectedSlotC2SPacket (Swaps slots like mouse wheel) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.inventorySwapPPS = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("inventory-swap-pps")).description("ClickSlotC2SPacket (Simulates moving items) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.closeScreenPPS = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("close-screen-pps")).description("CloseHandledScreenC2SPacket per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.actionPPS = this.sgAction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("action-pps")).description("PlayerActionC2SPacket (Abort Destroy) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.commandPPS = this.sgAction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("client-command-pps")).description("ClientCommandC2SPacket (Sneak) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.rotationPPS = this.sgMovement.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rotation-pps")).description("PlayerMoveC2SPacket (Look) per second.")).defaultValue(0)).min(0).sliderMax(100).build());
        this.conflictPPS = this.sgSimulation.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("conflict-pps")).description("Simulates the SilentMine+CA Sequence (Swap -> Place -> Swap).")).defaultValue(0)).min(0).sliderMax(20).build());
        this.includeMining = this.sgSimulation.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("include-mining")).description("Includes ABORT_DESTROY_BLOCK to simulate active mining during the swap.")).defaultValue(true)).visible(() -> this.conflictPPS.get() > 0)).build());
        this.random = new Random();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Entity target;
        if (this.mc.getNetworkHandler() == null || this.mc.player == null) {
            return;
        }
        this.sendPackets(this.swingPPS.get(), () -> new HandSwingC2SPacket(Hand.MAIN_HAND));
        this.sendPackets(this.interactItemPPS.get(), () -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.mc.player.getYaw(), this.mc.player.getPitch()));
        this.sendPackets(this.interactBlockPPS.get(), () -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.DOWN, BlockPos.ORIGIN, false), 0));
        if (this.interactEntityPPS.get() > 0 && (target = (Entity)StreamSupport.stream(this.mc.world.getEntities().spliterator(), false).filter(e -> e != this.mc.player && e.isAlive()).min(Comparator.comparingDouble(e -> this.mc.player.squaredDistanceTo(e))).orElse(null)) != null) {
            this.sendPackets(this.interactEntityPPS.get(), () -> PlayerInteractEntityC2SPacket.attack((Entity)target, (boolean)this.mc.player.isSneaking()));
        }
        this.sendPackets(this.updateSlotPPS.get(), () -> new UpdateSelectedSlotC2SPacket(this.mc.player.getInventory().selectedSlot));
        this.sendPackets(this.closeScreenPPS.get(), () -> new CloseHandledScreenC2SPacket(0));
        if (this.hotbarScrollPPS.get() > 0) {
            int current = this.mc.player.getInventory().selectedSlot;
            int next = (current + 1) % 9;
            this.sendPackets(this.hotbarScrollPPS.get(), () -> new UpdateSelectedSlotC2SPacket(this.random.nextBoolean() ? current : next));
        }
        if (this.inventorySwapPPS.get() > 0) {
            this.sendPackets(this.inventorySwapPPS.get(), () -> new ClickSlotC2SPacket(this.mc.player.currentScreenHandler.syncId, this.mc.player.currentScreenHandler.getRevision(), 9, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));
        }
        this.sendPackets(this.actionPPS.get(), () -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.mc.player.getBlockPos(), Direction.UP));
        this.sendPackets(this.commandPPS.get(), () -> new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        this.sendPackets(this.rotationPPS.get(), () -> new PlayerMoveC2SPacket.LookAndOnGround(this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround()));
        if (this.conflictPPS.get() > 0) {
            int currentSlot = this.mc.player.getInventory().selectedSlot;
            int otherSlot = (currentSlot + 1) % 9;
            int pps = this.conflictPPS.get();
            int base = pps / 20;
            int chance = pps % 20;
            int count = base + (this.random.nextInt(20) < chance ? 1 : 0);
            for (int i = 0; i < count; ++i) {
                if (this.includeMining.get().booleanValue()) {
                    this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.mc.player.getBlockPos(), Direction.UP));
                }
                this.mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(otherSlot));
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, this.mc.player.getBlockPos(), false), 0));
                this.mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(currentSlot));
            }
        }
    }

    private void sendPackets(int pps, PacketSupplier supplier) {
        if (pps <= 0) {
            return;
        }
        int base = pps / 20;
        int chance = pps % 20;
        int count = base;
        if (this.random.nextInt(20) < chance) {
            ++count;
        }
        for (int i = 0; i < count; ++i) {
            this.mc.getNetworkHandler().sendPacket(supplier.get());
        }
    }

    @FunctionalInterface
    static interface PacketSupplier {
        public Packet<?> get();
    }
}

