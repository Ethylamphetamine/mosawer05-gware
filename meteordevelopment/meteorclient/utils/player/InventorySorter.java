/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.inventory.SimpleInventory
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.Registries
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.util.Pair
 */
package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.mixininterface.ISlot;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;

public class InventorySorter {
    private final HandledScreen<?> screen;
    private final InvPart originInvPart;
    private boolean invalid;
    private List<Action> actions;
    private int timer;
    private int currentActionI;

    public InventorySorter(HandledScreen<?> screen, Slot originSlot) {
        this.screen = screen;
        this.originInvPart = this.getInvPart(originSlot);
        if (this.originInvPart == InvPart.Invalid || this.originInvPart == InvPart.Hotbar || screen instanceof PeekScreen) {
            this.invalid = true;
            return;
        }
        this.actions = new ArrayList<Action>();
        this.generateActions();
    }

    public boolean tick(int delay) {
        if (this.invalid) {
            return true;
        }
        if (this.currentActionI >= this.actions.size()) {
            return true;
        }
        if (this.timer < delay) {
            ++this.timer;
            return false;
        }
        this.timer = 0;
        Action action = this.actions.get(this.currentActionI);
        InvUtils.move().fromId(action.from).toId(action.to);
        ++this.currentActionI;
        return false;
    }

    private void generateActions() {
        ArrayList<MySlot> slots = new ArrayList<MySlot>();
        for (Slot slot : this.screen.getScreenHandler().slots) {
            if (this.getInvPart(slot) != this.originInvPart) continue;
            slots.add(new MySlot(((ISlot)slot).getId(), slot.getStack()));
        }
        slots.sort(Comparator.comparingInt(value -> value.id));
        this.generateStackingActions(slots);
        this.generateSortingActions(slots);
    }

    private void generateStackingActions(List<MySlot> slots) {
        SlotMap slotMap = new SlotMap();
        for (MySlot mySlot : slots) {
            if (mySlot.itemStack.isEmpty() || !mySlot.itemStack.isStackable() || mySlot.itemStack.getCount() >= mySlot.itemStack.getMaxCount()) continue;
            slotMap.get(mySlot.itemStack).add(mySlot);
        }
        for (Pair pair : slotMap.map) {
            List slotsToStack = (List)pair.getRight();
            MySlot slotToStackTo = null;
            for (int i = 0; i < slotsToStack.size(); ++i) {
                MySlot slot = (MySlot)slotsToStack.get(i);
                if (slotToStackTo == null) {
                    slotToStackTo = slot;
                    continue;
                }
                this.actions.add(new Action(slot.id, slotToStackTo.id));
                if (slotToStackTo.itemStack.getCount() + slot.itemStack.getCount() <= slotToStackTo.itemStack.getMaxCount()) {
                    slotToStackTo.itemStack = new ItemStack((ItemConvertible)slotToStackTo.itemStack.getItem(), slotToStackTo.itemStack.getCount() + slot.itemStack.getCount());
                    slot.itemStack = ItemStack.EMPTY;
                    if (slotToStackTo.itemStack.getCount() < slotToStackTo.itemStack.getMaxCount()) continue;
                    slotToStackTo = null;
                    continue;
                }
                int needed = slotToStackTo.itemStack.getMaxCount() - slotToStackTo.itemStack.getCount();
                slotToStackTo.itemStack = new ItemStack((ItemConvertible)slotToStackTo.itemStack.getItem(), slotToStackTo.itemStack.getMaxCount());
                slot.itemStack = new ItemStack((ItemConvertible)slot.itemStack.getItem(), slot.itemStack.getCount() - needed);
                slotToStackTo = null;
                --i;
            }
        }
    }

    private void generateSortingActions(List<MySlot> slots) {
        for (int i = 0; i < slots.size(); ++i) {
            MySlot bestSlot = null;
            for (int j = i; j < slots.size(); ++j) {
                MySlot slot = slots.get(j);
                if (bestSlot == null) {
                    bestSlot = slot;
                    continue;
                }
                if (!this.isSlotBetter(bestSlot, slot)) continue;
                bestSlot = slot;
            }
            if (bestSlot.itemStack.isEmpty()) continue;
            MySlot toSlot = slots.get(i);
            int from = bestSlot.id;
            int to = toSlot.id;
            if (from == to) continue;
            ItemStack temp = bestSlot.itemStack;
            bestSlot.itemStack = toSlot.itemStack;
            toSlot.itemStack = temp;
            this.actions.add(new Action(from, to));
        }
    }

    private boolean isSlotBetter(MySlot best, MySlot slot) {
        ItemStack bestI = best.itemStack;
        ItemStack slotI = slot.itemStack;
        if (bestI.isEmpty() && !slotI.isEmpty()) {
            return true;
        }
        if (!bestI.isEmpty() && slotI.isEmpty()) {
            return false;
        }
        int c = Registries.ITEM.getId((Object)bestI.getItem()).compareTo(Registries.ITEM.getId((Object)slotI.getItem()));
        if (c == 0) {
            return slotI.getCount() > bestI.getCount();
        }
        return c > 0;
    }

    private InvPart getInvPart(Slot slot) {
        int i = ((ISlot)slot).getIndex();
        if (slot.inventory instanceof PlayerInventory && (!(this.screen instanceof CreativeInventoryScreen) || ((ISlot)slot).getId() > 8)) {
            if (SlotUtils.isHotbar(i)) {
                return InvPart.Hotbar;
            }
            if (SlotUtils.isMain(i)) {
                return InvPart.Player;
            }
        } else if ((this.screen instanceof GenericContainerScreen || this.screen instanceof ShulkerBoxScreen) && slot.inventory instanceof SimpleInventory) {
            return InvPart.Main;
        }
        return InvPart.Invalid;
    }

    private static enum InvPart {
        Hotbar,
        Player,
        Main,
        Invalid;

    }

    private record Action(int from, int to) {
    }

    private static class MySlot {
        public final int id;
        public ItemStack itemStack;

        public MySlot(int id, ItemStack itemStack) {
            this.id = id;
            this.itemStack = itemStack;
        }
    }

    private static class SlotMap {
        private final List<Pair<ItemStack, List<MySlot>>> map = new ArrayList<Pair<ItemStack, List<MySlot>>>();

        private SlotMap() {
        }

        public List<MySlot> get(ItemStack itemStack) {
            for (Pair<ItemStack, List<MySlot>> entry : this.map) {
                if (!ItemStack.areItemsEqual((ItemStack)itemStack, (ItemStack)((ItemStack)entry.getLeft()))) continue;
                return (List)entry.getRight();
            }
            ArrayList<MySlot> list = new ArrayList<MySlot>();
            this.map.add((Pair<ItemStack, List<MySlot>>)new Pair((Object)itemStack, list));
            return list;
        }
    }
}

