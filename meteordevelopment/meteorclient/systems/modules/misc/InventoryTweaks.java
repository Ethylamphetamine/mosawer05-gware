/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractSkullBlock
 *  net.minecraft.block.CarvedPumpkinBlock
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Equipment
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.ScreenHandlerType
 *  net.minecraft.screen.slot.Slot
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.CloseHandledScreenC2SPacketAccessor;
import meteordevelopment.meteorclient.mixin.HandledScreenAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ScreenHandlerListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.InventorySorter;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class InventoryTweaks
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSorting;
    private final SettingGroup sgAutoDrop;
    private final SettingGroup sgStealDump;
    private final SettingGroup sgAutoSteal;
    private final Setting<Boolean> mouseDragItemMove;
    private final Setting<List<Item>> antiDropItems;
    private final Setting<Boolean> xCarry;
    private final Setting<Boolean> armorStorage;
    private final Setting<Boolean> sortingEnabled;
    private final Setting<Keybind> sortingKey;
    private final Setting<Integer> sortingDelay;
    private final Setting<List<Item>> autoDropItems;
    private final Setting<Boolean> autoDropExcludeEquipped;
    private final Setting<Boolean> autoDropExcludeHotbar;
    private final Setting<Boolean> autoDropOnlyFullStacks;
    public final Setting<List<ScreenHandlerType<?>>> stealScreens;
    private final Setting<Boolean> buttons;
    private final Setting<Boolean> stealDrop;
    private final Setting<Boolean> dropBackwards;
    private final Setting<ListMode> dumpFilter;
    private final Setting<List<Item>> dumpItems;
    private final Setting<ListMode> stealFilter;
    private final Setting<List<Item>> stealItems;
    private final Setting<Boolean> autoSteal;
    private final Setting<Boolean> autoDump;
    private final Setting<Integer> autoStealDelay;
    private final Setting<Integer> autoStealInitDelay;
    private final Setting<Integer> autoStealRandomDelay;
    private InventorySorter sorter;
    private boolean invOpened;

    public InventoryTweaks() {
        super(Categories.Misc, "inventory-tweaks", "Various inventory related utilities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSorting = this.settings.createGroup("Sorting");
        this.sgAutoDrop = this.settings.createGroup("Auto Drop");
        this.sgStealDump = this.settings.createGroup("Steal and Dump");
        this.sgAutoSteal = this.settings.createGroup("Auto Steal");
        this.mouseDragItemMove = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mouse-drag-item-move")).description("Moving mouse over items while holding shift will transfer it to the other container.")).defaultValue(true)).build());
        this.antiDropItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("anti-drop-items")).description("Items to prevent dropping. Doesn't work in creative inventory screen.")).build());
        this.xCarry = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("xcarry")).description("Allows you to store four extra item stacks in your crafting grid.")).defaultValue(true)).onChanged(v -> {
            if (v.booleanValue() || !Utils.canUpdate()) {
                return;
            }
            this.mc.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(this.mc.player.playerScreenHandler.syncId));
            this.invOpened = false;
        })).build());
        this.armorStorage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("armor-storage")).description("Allows you to put normal items in your armor slots.")).defaultValue(true)).build());
        this.sortingEnabled = this.sgSorting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sorting-enabled")).description("Automatically sorts stacks in inventory.")).defaultValue(true)).build());
        this.sortingKey = this.sgSorting.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("sorting-key")).description("Key to trigger the sort.")).visible(this.sortingEnabled::get)).defaultValue(Keybind.fromButton(2))).build());
        this.sortingDelay = this.sgSorting.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("sorting-delay")).description("Delay in ticks between moving items when sorting.")).visible(this.sortingEnabled::get)).defaultValue(1)).min(0).build());
        this.autoDropItems = this.sgAutoDrop.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("auto-drop-items")).description("Items to drop.")).build());
        this.autoDropExcludeEquipped = this.sgAutoDrop.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("exclude-equipped")).description("Whether or not to drop items equipped in armor slots.")).defaultValue(true)).build());
        this.autoDropExcludeHotbar = this.sgAutoDrop.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("exclude-hotbar")).description("Whether or not to drop items from your hotbar.")).defaultValue(false)).build());
        this.autoDropOnlyFullStacks = this.sgAutoDrop.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-full-stacks")).description("Only drops the items if the stack is full.")).defaultValue(false)).build());
        this.stealScreens = this.sgStealDump.add(((ScreenHandlerListSetting.Builder)((ScreenHandlerListSetting.Builder)((ScreenHandlerListSetting.Builder)new ScreenHandlerListSetting.Builder().name("steal-screens")).description("Select the screens to display buttons and auto steal.")).defaultValue(List.of(ScreenHandlerType.GENERIC_9X3, ScreenHandlerType.GENERIC_9X6))).build());
        this.buttons = this.sgStealDump.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("inventory-buttons")).description("Shows steal and dump buttons in container guis.")).defaultValue(true)).build());
        this.stealDrop = this.sgStealDump.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("steal-drop")).description("Drop items to the ground instead of stealing them.")).defaultValue(false)).build());
        this.dropBackwards = this.sgStealDump.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("drop-backwards")).description("Drop items behind you.")).defaultValue(false)).visible(this.stealDrop::get)).build());
        this.dumpFilter = this.sgStealDump.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("dump-filter")).description("Dump mode.")).defaultValue(ListMode.None)).build());
        this.dumpItems = this.sgStealDump.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("dump-items")).description("Items to dump.")).build());
        this.stealFilter = this.sgStealDump.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("steal-filter")).description("Steal mode.")).defaultValue(ListMode.None)).build());
        this.stealItems = this.sgStealDump.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("steal-items")).description("Items to steal.")).build());
        this.autoSteal = this.sgAutoSteal.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-steal")).description("Automatically removes all possible items when you open a container.")).defaultValue(false)).onChanged(val -> this.checkAutoStealSettings())).build());
        this.autoDump = this.sgAutoSteal.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-dump")).description("Automatically dumps all possible items when you open a container.")).defaultValue(false)).onChanged(val -> this.checkAutoStealSettings())).build());
        this.autoStealDelay = this.sgAutoSteal.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The minimum delay between stealing the next stack in milliseconds.")).defaultValue(20)).sliderMax(1000).build());
        this.autoStealInitDelay = this.sgAutoSteal.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("initial-delay")).description("The initial delay before stealing in milliseconds. 0 to use normal delay instead.")).defaultValue(50)).sliderMax(1000).build());
        this.autoStealRandomDelay = this.sgAutoSteal.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("random")).description("Randomly adds a delay of up to the specified time in milliseconds.")).min(0).sliderMax(1000).defaultValue(50)).build());
    }

    @Override
    public void onActivate() {
        this.invOpened = false;
    }

    @Override
    public void onDeactivate() {
        this.sorter = null;
        if (this.invOpened) {
            this.mc.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(this.mc.player.playerScreenHandler.syncId));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press) {
            return;
        }
        if (this.sortingKey.get().matches(true, event.key, event.modifiers) && this.sort()) {
            event.cancel();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press) {
            return;
        }
        if (this.sortingKey.get().matches(false, event.button, 0) && this.sort()) {
            event.cancel();
        }
    }

    private boolean sort() {
        Slot focusedSlot;
        HandledScreen screen;
        block8: {
            block7: {
                Screen screen2;
                if (!this.sortingEnabled.get().booleanValue() || !((screen2 = this.mc.currentScreen) instanceof HandledScreen)) break block7;
                screen = (HandledScreen)screen2;
                if (this.sorter == null) break block8;
            }
            return false;
        }
        if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            FindItemResult empty = InvUtils.findEmpty();
            if (!empty.found()) {
                InvUtils.click().slot(-999);
            } else {
                InvUtils.click().slot(empty.slot());
            }
        }
        if ((focusedSlot = ((HandledScreenAccessor)screen).getFocusedSlot()) == null) {
            return false;
        }
        this.sorter = new InventorySorter(screen, focusedSlot);
        return true;
    }

    private boolean isWearable(ItemStack itemStack) {
        BlockItem blockItem;
        Item item = itemStack.getItem();
        if (item instanceof Equipment) {
            return true;
        }
        return item instanceof BlockItem && ((blockItem = (BlockItem)item).getBlock() instanceof AbstractSkullBlock || blockItem.getBlock() instanceof CarvedPumpkinBlock);
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        this.sorter = null;
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (this.sorter != null && this.sorter.tick(this.sortingDelay.get())) {
            this.sorter = null;
        }
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        int i;
        if (this.mc.currentScreen instanceof HandledScreen || this.autoDropItems.get().isEmpty()) {
            return;
        }
        int n = i = this.autoDropExcludeHotbar.get() != false ? 9 : 0;
        while (i < this.mc.player.getInventory().size()) {
            ItemStack itemStack = this.mc.player.getInventory().getStack(i);
            if (!(!this.autoDropItems.get().contains(itemStack.getItem()) || this.autoDropOnlyFullStacks.get().booleanValue() && itemStack.getCount() != itemStack.getMaxCount() || this.autoDropExcludeEquipped.get().booleanValue() && SlotUtils.isArmor(i))) {
                InvUtils.drop().slot(i);
            }
            ++i;
        }
    }

    @EventHandler
    private void onDropItems(DropItemsEvent event) {
        if (this.antiDropItems.get().contains(event.itemStack.getItem())) {
            event.cancel();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!this.xCarry.get().booleanValue() || !(event.packet instanceof CloseHandledScreenC2SPacket)) {
            return;
        }
        if (((CloseHandledScreenC2SPacketAccessor)event.packet).getSyncId() == this.mc.player.playerScreenHandler.syncId) {
            this.invOpened = true;
            event.cancel();
        }
    }

    private void checkAutoStealSettings() {
        if (this.autoSteal.get().booleanValue() && this.autoDump.get().booleanValue()) {
            this.error("You can't enable Auto Steal and Auto Dump at the same time!", new Object[0]);
            this.autoDump.set(false);
        }
    }

    private int getSleepTime() {
        return this.autoStealDelay.get() + (this.autoStealRandomDelay.get() > 0 ? ThreadLocalRandom.current().nextInt(0, this.autoStealRandomDelay.get()) : 0);
    }

    private void moveSlots(ScreenHandler handler, int start, int end, boolean steal) {
        boolean initial = this.autoStealInitDelay.get() != 0;
        for (int i = start; i < end; ++i) {
            int sleep;
            if (!handler.getSlot(i).hasStack()) continue;
            if (initial) {
                sleep = this.autoStealInitDelay.get();
                initial = false;
            } else {
                sleep = this.getSleepTime();
            }
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (this.mc.currentScreen == null || !Utils.canUpdate()) break;
            Item item = handler.getSlot(i).getStack().getItem();
            if (!steal ? this.dumpFilter.get() == ListMode.Whitelist && !this.dumpItems.get().contains(item) || this.dumpFilter.get() == ListMode.Blacklist && this.dumpItems.get().contains(item) : this.stealFilter.get() == ListMode.Whitelist && !this.stealItems.get().contains(item) || this.stealFilter.get() == ListMode.Blacklist && this.stealItems.get().contains(item)) continue;
            if (steal && this.stealDrop.get().booleanValue()) {
                if (!this.dropBackwards.get().booleanValue()) continue;
                int iCopy = i;
                Rotations.rotate((double)(this.mc.player.getYaw() - 180.0f), (double)this.mc.player.getPitch(), () -> InvUtils.drop().slotId(iCopy));
                continue;
            }
            InvUtils.shiftClick().slotId(i);
        }
    }

    public void steal(ScreenHandler handler) {
        MeteorExecutor.execute(() -> this.moveSlots(handler, 0, SlotUtils.indexToId(9), true));
    }

    public void dump(ScreenHandler handler) {
        int playerInvOffset = SlotUtils.indexToId(9);
        MeteorExecutor.execute(() -> this.moveSlots(handler, playerInvOffset, playerInvOffset + 36, false));
    }

    public boolean showButtons() {
        return this.isActive() && this.buttons.get() != false;
    }

    public boolean mouseDragItemMove() {
        return this.isActive() && this.mouseDragItemMove.get() != false;
    }

    public boolean armorStorage() {
        return this.isActive() && this.armorStorage.get() != false;
    }

    public boolean canSteal(ScreenHandler handler) {
        try {
            return this.stealScreens.get().contains(handler.getType());
        }
        catch (UnsupportedOperationException e) {
            return false;
        }
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        ScreenHandler handler = this.mc.player.currentScreenHandler;
        if (this.canSteal(handler) && event.packet.getSyncId() == handler.syncId) {
            if (this.autoSteal.get().booleanValue()) {
                this.steal(handler);
            } else if (this.autoDump.get().booleanValue()) {
                this.dump(handler);
            }
        }
    }

    public static enum ListMode {
        Whitelist,
        Blacklist,
        None;

    }
}

