/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.collection.DefaultedList
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

public class AutoReplenish
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> delay;
    private final Setting<Integer> threshold;
    private final List<Item> rememberedItems;
    private long lastActionTime;

    public AutoReplenish() {
        super(Categories.Player, "auto-replenish", "Automatically replenishes your hotbar items. (Mio-style)");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay in milliseconds between replenish actions.")).defaultValue(100)).min(0).sliderRange(0, 500).build());
        this.threshold = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("threshold")).description("The threshold of items left before replenishing.")).defaultValue(16)).min(1).sliderRange(1, 63).build());
        this.rememberedItems = DefaultedList.ofSize((int)9, (Object)Items.AIR);
        this.lastActionTime = 0L;
    }

    @Override
    public void onActivate() {
        this.lastActionTime = 0L;
        if (this.mc.player != null) {
            for (int i = 0; i < 9; ++i) {
                this.rememberedItems.set(i, this.mc.player.getInventory().getStack(i).getItem());
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        int i;
        if (this.mc.player == null || this.mc.interactionManager == null) {
            return;
        }
        if (System.currentTimeMillis() - this.lastActionTime < (long)this.delay.get().intValue()) {
            return;
        }
        if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            return;
        }
        boolean inScreen = this.mc.currentScreen instanceof HandledScreen;
        for (i = 0; i < 9; ++i) {
            ItemStack currentStack = this.mc.player.getInventory().getStack(i);
            Item rememberedItem = this.rememberedItems.get(i);
            if (currentStack.isEmpty() && rememberedItem != Items.AIR && !inScreen && this.refillSlotWithItem(rememberedItem, i)) {
                this.lastActionTime = System.currentTimeMillis();
                return;
            }
            this.rememberedItems.set(i, currentStack.getItem());
        }
        if (inScreen && !(this.mc.currentScreen instanceof InventoryScreen)) {
            return;
        }
        for (i = 0; i < 9; ++i) {
            if (!this.replenishSlot(i)) continue;
            this.lastActionTime = System.currentTimeMillis();
            return;
        }
    }

    private boolean refillSlotWithItem(Item item, int hotbarSlot) {
        int sourceSlot = -1;
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.getItem() != item || stack == this.mc.player.getOffHandStack()) continue;
            sourceSlot = i;
            break;
        }
        if (sourceSlot == -1) {
            return false;
        }
        if (this.isHotbarFullExcept(hotbarSlot)) {
            int slotId = SlotUtils.indexToId(sourceSlot);
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slotId, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)this.mc.player);
        } else {
            InvUtils.move().from(sourceSlot).to(hotbarSlot);
        }
        return true;
    }

    private boolean replenishSlot(int hotbarSlot) {
        float thresholdRatio;
        ItemStack hotbarStack = this.mc.player.getInventory().getStack(hotbarSlot);
        if (hotbarStack.isEmpty() || hotbarStack.getItem() == Items.AIR || !hotbarStack.isStackable()) {
            return false;
        }
        float ratio = (float)hotbarStack.getCount() / (float)hotbarStack.getMaxCount();
        if (ratio >= (thresholdRatio = (float)this.threshold.get().intValue() / 64.0f)) {
            return false;
        }
        int bestSlot = -1;
        int bestCount = 0;
        for (int i = 9; i < 36; ++i) {
            ItemStack invStack = this.mc.player.getInventory().getStack(i);
            if (invStack.isEmpty() || !ItemStack.areItemsAndComponentsEqual((ItemStack)invStack, (ItemStack)hotbarStack) || invStack.getCount() <= bestCount) continue;
            bestSlot = i;
            bestCount = invStack.getCount();
        }
        InventoryTweaks inventoryTweaks = Modules.get().get(InventoryTweaks.class);
        if (inventoryTweaks != null && inventoryTweaks.isActive() && !(this.mc.currentScreen instanceof InventoryScreen)) {
            for (int i = 0; i <= 3; ++i) {
                ItemStack craftStack = this.mc.player.playerScreenHandler.getSlot(i + 1).getStack();
                if (craftStack.isEmpty() || !ItemStack.areItemsAndComponentsEqual((ItemStack)craftStack, (ItemStack)hotbarStack) || craftStack.getCount() <= bestCount) continue;
                bestSlot = i + 1;
                bestCount = craftStack.getCount();
            }
        }
        if (bestSlot == -1) {
            return false;
        }
        int syncId = this.mc.player.currentScreenHandler.syncId;
        if (this.isHotbarFullExcept(-1)) {
            int slotId = bestSlot <= 4 && bestSlot >= 1 ? bestSlot : SlotUtils.indexToId(bestSlot);
            this.mc.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)this.mc.player);
        } else if (bestSlot <= 4 && bestSlot >= 1) {
            this.mc.interactionManager.clickSlot(syncId, bestSlot, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
            int destId = SlotUtils.indexToId(hotbarSlot);
            this.mc.interactionManager.clickSlot(syncId, destId, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
        } else {
            InvUtils.move().from(bestSlot).to(hotbarSlot);
        }
        return true;
    }

    private boolean isHotbarFullExcept(int excludeSlot) {
        for (int i = 0; i < 9; ++i) {
            if (i == excludeSlot || !this.mc.player.getInventory().getStack(i).isEmpty()) continue;
            return false;
        }
        return true;
    }
}

