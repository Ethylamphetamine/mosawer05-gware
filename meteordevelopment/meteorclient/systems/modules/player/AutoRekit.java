/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.ContainerComponent
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.registry.Registries
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.ShulkerBoxScreenHandler
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.collection.DefaultedList
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.rekit.RekitSystem;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class AutoRekit
extends Module {
    private final SettingGroup sg;
    private final SettingGroup sgShulker;
    private final Setting<Integer> delayMs;
    private final Setting<Integer> itemsPerTick;
    private final Setting<Integer> kitId;
    private final Setting<Mode> mode;
    private final Setting<Boolean> closeShulkerOnDone;
    private final Setting<TriggerMode> triggerMode;
    private final Setting<Keybind> shulkerBind;
    private final Setting<Boolean> middleClickTrigger;
    private final Setting<Integer> shulkerSlot;
    private final Setting<Integer> threshold;
    private long lastMoveAt;
    private final Deque<Move> queue;
    private int memorySlot;
    private boolean hasAutoTriggered;
    private int syncTimer;
    private boolean needsSync;

    public AutoRekit() {
        super(Categories.Player, "auto-rekit", "Restores a saved inventory kit efficiently.");
        this.sg = this.settings.getDefaultGroup();
        this.sgShulker = this.settings.createGroup("Shulker Select");
        this.delayMs = this.sg.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay-ms")).description("Delay per move (ms)")).defaultValue(40)).min(0).sliderMax(500).build());
        this.itemsPerTick = this.sg.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("items-per-tick")).description("How many items to move per tick.")).defaultValue(39)).min(1).sliderMax(39).build());
        this.kitId = this.sg.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("kit")).description("Kit id to apply (1-24)")).defaultValue(1)).min(1).sliderMax(24).build());
        this.mode = this.sg.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Smart uses Shift-Click when possible. Standard is precise.")).defaultValue(Mode.Standard)).build());
        this.closeShulkerOnDone = this.sg.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("close-shulker-on-done")).description("Instantly closes the shulker box after finishing regearing.")).defaultValue(false)).build());
        this.triggerMode = this.sgShulker.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("trigger-mode")).description("Automatic tries to cycle shulkers as soon as EChest opens.")).defaultValue(TriggerMode.Manual)).build());
        this.shulkerBind = this.sgShulker.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("trigger-bind")).description("Key to press inside EChest to cycle shulkers.")).defaultValue(Keybind.none())).visible(() -> this.triggerMode.get() == TriggerMode.Manual)).build());
        this.middleClickTrigger = this.sgShulker.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("middle-click-trigger")).description("Use Middle Click inside EChest to cycle shulkers.")).defaultValue(true)).visible(() -> this.triggerMode.get() == TriggerMode.Manual)).build());
        this.shulkerSlot = this.sgShulker.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hotbar-slot")).description("The hotbar slot (1-9) to equip the Shulker into.")).defaultValue(1)).min(1).max(9).build());
        this.threshold = this.sgShulker.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("refill-threshold")).description("Only select a shulker if a stack is below this percentage.")).defaultValue(90)).min(50).max(100).sliderMin(50).sliderMax(100).build());
        this.lastMoveAt = 0L;
        this.queue = new ArrayDeque<Move>();
        this.memorySlot = -1;
        this.hasAutoTriggered = false;
        this.syncTimer = 0;
        this.needsSync = false;
    }

    @Override
    public void onActivate() {
        this.queue.clear();
        this.memorySlot = -1;
        this.hasAutoTriggered = false;
        this.syncTimer = 0;
        this.needsSync = false;
        if (this.isShulkerOpen()) {
            this.planMoves();
            if (this.queue.isEmpty()) {
                ChatUtils.info("AutoRekit: nothing to move for kit (highlight)%d(default).", this.kitId.get());
                this.closeIfDone();
            }
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (this.triggerMode.get() != TriggerMode.Manual || !this.middleClickTrigger.get().booleanValue()) {
            return;
        }
        if (event.action != KeyAction.Press || event.button != 2) {
            return;
        }
        Screen screen = this.mc.currentScreen;
        if (screen instanceof GenericContainerScreen) {
            GenericContainerScreen containerScreen = (GenericContainerScreen)screen;
            this.handleShulkerCycle((GenericContainerScreenHandler)containerScreen.getScreenHandler(), ((GenericContainerScreenHandler)containerScreen.getScreenHandler()).getInventory().size());
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (this.triggerMode.get() != TriggerMode.Manual) {
            return;
        }
        if (event.action != KeyAction.Press || this.shulkerBind.get().getValue() != event.key) {
            return;
        }
        Screen screen = this.mc.currentScreen;
        if (screen instanceof GenericContainerScreen) {
            GenericContainerScreen containerScreen = (GenericContainerScreen)screen;
            this.handleShulkerCycle((GenericContainerScreenHandler)containerScreen.getScreenHandler(), ((GenericContainerScreenHandler)containerScreen.getScreenHandler()).getInventory().size());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if (this.mc.player == null || this.mc.interactionManager == null) {
            return;
        }
        if (this.needsSync) {
            ++this.syncTimer;
            if (this.syncTimer > 2) {
                this.forceSync();
                this.needsSync = false;
                this.syncTimer = 0;
            }
        }
        if (this.triggerMode.get() == TriggerMode.Automatic) {
            Screen screen = this.mc.currentScreen;
            if (screen instanceof GenericContainerScreen) {
                GenericContainerScreen containerScreen = (GenericContainerScreen)screen;
                String title = containerScreen.getTitle().getString().toLowerCase();
                if (title.contains("ender") && !this.hasAutoTriggered) {
                    this.handleShulkerCycle((GenericContainerScreenHandler)containerScreen.getScreenHandler(), ((GenericContainerScreenHandler)containerScreen.getScreenHandler()).getInventory().size());
                    this.hasAutoTriggered = true;
                }
            } else {
                this.hasAutoTriggered = false;
            }
        } else {
            this.hasAutoTriggered = false;
        }
        if (this.isContainerOpen()) {
            if (this.mc.currentScreen != null && this.mc.currentScreen.getTitle().getString().toLowerCase().contains("ender")) {
                return;
            }
            if (this.queue.isEmpty()) {
                this.planMoves();
                if (this.queue.isEmpty()) {
                    this.closeIfDone();
                    return;
                }
            }
            this.processRekitQueue();
        }
    }

    private void forceSync() {
        if (this.mc.player.currentScreenHandler != null && !this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.mc.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
    }

    private boolean isShulkerBox(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock;
    }

    private void handleShulkerCycle(GenericContainerScreenHandler handler, int containerSize) {
        int targetSlotIndex = this.shulkerSlot.get() - 1;
        int rawHotbarSlotId = containerSize + 27 + targetSlotIndex;
        int ignoreSlot = -1;
        boolean didPutBack = false;
        ItemStack stackInHotbar = handler.getSlot(rawHotbarSlotId).getStack();
        if (this.memorySlot != -1 && this.memorySlot < containerSize && !stackInHotbar.isEmpty()) {
            this.click(handler.syncId, rawHotbarSlotId, 0, SlotActionType.PICKUP);
            this.click(handler.syncId, this.memorySlot, 0, SlotActionType.PICKUP);
            ignoreSlot = this.memorySlot;
            didPutBack = true;
        }
        this.memorySlot = -1;
        Set<Item> neededItems = this.getNeededItemsForKit();
        if (neededItems.isEmpty()) {
            if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                this.click(handler.syncId, rawHotbarSlotId, 0, SlotActionType.PICKUP);
            }
            this.safeClose();
            return;
        }
        int bestSlot = -1;
        for (int i = 0; i < containerSize; ++i) {
            ItemStack stack;
            if (i == ignoreSlot || (stack = handler.getSlot(i).getStack()).isEmpty() || !this.isShulkerBox(stack) || !this.containsNeededItem(stack, neededItems)) continue;
            bestSlot = i;
            break;
        }
        if (bestSlot != -1) {
            this.click(handler.syncId, bestSlot, 0, SlotActionType.PICKUP);
            this.click(handler.syncId, rawHotbarSlotId, 0, SlotActionType.PICKUP);
            if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                this.click(handler.syncId, bestSlot, 0, SlotActionType.PICKUP);
            }
            this.memorySlot = bestSlot;
            this.mc.player.getInventory().selectedSlot = targetSlotIndex;
            this.mc.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(targetSlotIndex));
            this.safeClose();
        } else {
            if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                this.click(handler.syncId, rawHotbarSlotId, 0, SlotActionType.PICKUP);
            }
            if (didPutBack || this.triggerMode.get() == TriggerMode.Automatic) {
                this.safeClose();
            }
        }
    }

    private void safeClose() {
        if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            int containerSize = this.mc.player.currentScreenHandler.slots.size() - 36;
            int targetSlotIndex = this.shulkerSlot.get() - 1;
            int rawHotbarSlotId = containerSize + 27 + targetSlotIndex;
            this.click(this.mc.player.currentScreenHandler.syncId, rawHotbarSlotId, 0, SlotActionType.PICKUP);
        }
        if (!this.mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.mc.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
        this.mc.player.closeHandledScreen();
        this.needsSync = true;
        this.syncTimer = 0;
    }

    private Set<Item> getNeededItemsForKit() {
        HashSet<Item> needed = new HashSet<Item>();
        RekitSystem.Kit kit = RekitSystem.get().getKit(this.kitId.get());
        if (kit == null) {
            return needed;
        }
        DefaultedList inv = this.mc.player.getInventory().main;
        for (int i = 0; i < kit.slots.size(); ++i) {
            Item targetItem;
            RekitSystem.SavedStack saved = kit.slots.get(i);
            if (saved == null || (targetItem = this.getItemById(saved.itemId)) == null || i >= inv.size() || i == this.shulkerSlot.get() - 1) continue;
            ItemStack current = (ItemStack)inv.get(i);
            int maxStack = current.isEmpty() ? targetItem.getMaxCount() : current.getMaxCount();
            int currentCount = current.isEmpty() ? 0 : current.getCount();
            double percent = (double)currentCount / (double)maxStack * 100.0;
            if (percent >= (double)this.threshold.get().intValue() || !current.isEmpty() && current.getItem() == targetItem && current.getCount() >= saved.count) continue;
            needed.add(targetItem);
        }
        return needed;
    }

    private boolean containsNeededItem(ItemStack shulker, Set<Item> needed) {
        ContainerComponent containerData = (ContainerComponent)shulker.get(DataComponentTypes.CONTAINER);
        if (containerData == null) {
            return false;
        }
        for (ItemStack content : containerData.iterateNonEmpty()) {
            if (!needed.contains(content.getItem())) continue;
            return true;
        }
        return false;
    }

    private void processRekitQueue() {
        long now = System.currentTimeMillis();
        if (now - this.lastMoveAt < (long)this.delayMs.get().intValue()) {
            return;
        }
        this.lastMoveAt = now;
        for (int i = 0; i < this.itemsPerTick.get() && PacketManager.INSTANCE.isClickAllowed(); ++i) {
            Move m;
            if (this.queue.isEmpty()) {
                this.planMoves();
                if (this.queue.isEmpty()) break;
            }
            if ((m = this.queue.pollFirst()) == null) break;
            this.performMove(m);
        }
        if (this.queue.isEmpty()) {
            this.planMoves();
            if (this.queue.isEmpty()) {
                this.closeIfDone();
            }
        }
    }

    private void planMoves() {
        if (!this.isContainerOpen()) {
            return;
        }
        if (this.mode.get() == Mode.Alternative) {
            this.planMovesAlternative();
        } else {
            this.planMovesStandard();
        }
    }

    private void planMovesStandard() {
        ItemStack cur;
        Item desiredItem;
        RekitSystem.SavedStack desired;
        int target;
        RekitSystem.Kit kit = RekitSystem.get().getKit(this.kitId.get());
        if (kit == null) {
            return;
        }
        ScreenHandler h = this.mc.player.currentScreenHandler;
        int total = h.slots.size();
        int containerCount = Math.max(0, total - 36);
        DefaultedList inv = this.mc.player.getInventory().main;
        HashSet<Integer> usedContainerSlots = new HashSet<Integer>();
        for (target = 0; target < 36; ++target) {
            int src;
            boolean curMatches;
            if (target == this.shulkerSlot.get() - 1 || (desired = kit.slots.get(target)) == null || (desiredItem = this.getItemById(desired.itemId)) == this.getItemById("elytra")) continue;
            cur = (ItemStack)inv.get(target);
            boolean bl = curMatches = !cur.isEmpty() && cur.getItem() == desiredItem;
            if (curMatches || (src = this.findContainerSourceSlotAnyName(h, containerCount, desiredItem, usedContainerSlots)) == -1) continue;
            this.queue.addLast(new Move(src, target, desired.itemId, desired.customName));
            usedContainerSlots.add(src);
        }
        block1: for (target = 0; target < 36; ++target) {
            int need;
            if (target == this.shulkerSlot.get() - 1 || (desired = kit.slots.get(target)) == null || (desiredItem = this.getItemById(desired.itemId)) == this.getItemById("elytra") || (cur = (ItemStack)inv.get(target)).isEmpty() || cur.getItem() != desiredItem || !this.nameMatches(cur, desired.customName) || !cur.isStackable() || (need = cur.getMaxCount() - cur.getCount()) <= 0) continue;
            List<Integer> sources = this.findAllContainerSourcesStrict(h, containerCount, desiredItem, desired.customName, usedContainerSlots);
            for (int src : sources) {
                if (need <= 0) continue block1;
                ItemStack s = h.getSlot(src).getStack();
                this.queue.addLast(new Move(src, target, desired.itemId, desired.customName));
                need -= s.getCount();
                usedContainerSlots.add(src);
            }
        }
    }

    private void planMovesAlternative() {
        RekitSystem.Kit kit = RekitSystem.get().getKit(this.kitId.get());
        if (kit == null) {
            return;
        }
        ScreenHandler h = this.mc.player.currentScreenHandler;
        int total = h.slots.size();
        int containerCount = Math.max(0, total - 36);
        DefaultedList inv = this.mc.player.getInventory().main;
        HashSet<Integer> usedContainerSlots = new HashSet<Integer>();
        for (int target = 0; target < 36; ++target) {
            int src;
            Item desiredItem;
            RekitSystem.SavedStack desired;
            if (target == this.shulkerSlot.get() - 1 || (desired = kit.slots.get(target)) == null || (desiredItem = this.getItemById(desired.itemId)) == this.getItemById("elytra")) continue;
            int have = 0;
            for (int i = 0; i < 36; ++i) {
                ItemStack s = (ItemStack)inv.get(i);
                if (s.isEmpty() || s.getItem() != desiredItem) continue;
                have += s.getCount();
            }
            if (have >= desired.count || (src = this.findContainerSourceSlotAnyName(h, containerCount, desiredItem, usedContainerSlots)) == -1) continue;
            this.queue.addLast(new Move(src, -1, desired.itemId, desired.customName));
            usedContainerSlots.add(src);
        }
    }

    private void performMove(Move m) {
        int simulatedTargetId;
        int toId;
        ScreenHandler h = this.mc.player.currentScreenHandler;
        if (h == null) {
            return;
        }
        int sid = h.syncId;
        int fromId = m.fromSlotId;
        int n = toId = m.toInvIndex >= 0 ? SlotUtils.indexToId(m.toInvIndex) : -1;
        if (fromId == -1) {
            return;
        }
        ItemStack sourceStack = h.getSlot(fromId).getStack();
        if (sourceStack.isEmpty()) {
            return;
        }
        if (this.mode.get() == Mode.Smart && (simulatedTargetId = this.simulateQuickMove(h, sourceStack)) != -1 && simulatedTargetId == toId) {
            this.click(sid, fromId, 0, SlotActionType.QUICK_MOVE);
            return;
        }
        if (this.mode.get() == Mode.Alternative) {
            this.click(sid, fromId, 0, SlotActionType.QUICK_MOVE);
            Item want = this.getItemById(m.desiredItemId);
            int haveIdx = this.findSourceSlotStrict((DefaultedList<ItemStack>)this.mc.player.getInventory().main, want, m.desiredName);
            if (haveIdx == -1) {
                return;
            }
            if (m.toInvIndex < 0 || haveIdx == m.toInvIndex) {
                return;
            }
            int invFromId = SlotUtils.indexToId(haveIdx);
            int invToId = SlotUtils.indexToId(m.toInvIndex);
            if (invFromId == -1 || invToId == -1) {
                return;
            }
            boolean targetEmpty = h.getSlot(invToId).getStack().isEmpty();
            this.click(sid, invFromId, 0, SlotActionType.PICKUP);
            this.click(sid, invToId, 0, SlotActionType.PICKUP);
            if (!targetEmpty) {
                this.click(sid, invFromId, 0, SlotActionType.PICKUP);
            }
        } else {
            boolean targetEmpty = toId != -1 && h.getSlot(toId).getStack().isEmpty();
            this.click(sid, fromId, 0, SlotActionType.PICKUP);
            if (toId != -1) {
                this.click(sid, toId, 0, SlotActionType.PICKUP);
            }
            if (!targetEmpty && toId != -1) {
                this.click(sid, fromId, 0, SlotActionType.PICKUP);
            }
        }
    }

    private int simulateQuickMove(ScreenHandler h, ItemStack source) {
        int i;
        int total = h.slots.size();
        if (total < 36) {
            return -1;
        }
        int startSlot = total - 36;
        int endSlot = total;
        for (i = startSlot; i < endSlot; ++i) {
            ItemStack targetStack = h.getSlot(i).getStack();
            if (!this.canMerge(source, targetStack)) continue;
            return i;
        }
        for (i = startSlot; i < endSlot; ++i) {
            if (!h.getSlot(i).getStack().isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private boolean canMerge(ItemStack source, ItemStack target) {
        if (target.isEmpty()) {
            return false;
        }
        if (!ItemStack.areItemsEqual((ItemStack)source, (ItemStack)target)) {
            return false;
        }
        return ItemStack.areItemsAndComponentsEqual((ItemStack)source, (ItemStack)target) && target.getCount() < target.getMaxCount();
    }

    private boolean nameMatches(ItemStack stack, String desiredName) {
        boolean custom = this.isCustomNamed(stack);
        if (desiredName == null || desiredName.isEmpty()) {
            return !custom;
        }
        return custom && stack.getName().getString().equals(desiredName);
    }

    private int findSourceSlotStrict(DefaultedList<ItemStack> inv, Item item, String desiredName) {
        boolean requireNamed = desiredName != null && !desiredName.isEmpty();
        for (int i = 0; i < 36; ++i) {
            ItemStack s = (ItemStack)inv.get(i);
            if (s.isEmpty() || s.getItem() != item || !(requireNamed ? this.isCustomNamed(s) && s.getName().getString().equals(desiredName) : !this.isCustomNamed(s))) continue;
            return i;
        }
        return -1;
    }

    private void click(int syncId, int slot, int button, SlotActionType type) {
        PacketManager.INSTANCE.incrementClick();
        this.mc.interactionManager.clickSlot(syncId, slot, button, type, (PlayerEntity)this.mc.player);
    }

    private Item getItemById(String id) {
        if (id == null) {
            return null;
        }
        Identifier ident = id.contains(":") ? Identifier.of((String)id) : Identifier.of((String)"minecraft", (String)id);
        return (Item)Registries.ITEM.get(ident);
    }

    private boolean isCustomNamed(ItemStack stack) {
        String base;
        String display = stack.getName().getString();
        return !display.equals(base = stack.getItem().getName().getString());
    }

    private boolean isContainerOpen() {
        if (this.mc.currentScreen == null) {
            return false;
        }
        if (this.mc.currentScreen instanceof InventoryScreen) {
            return false;
        }
        ScreenHandler h = this.mc.player.currentScreenHandler;
        return h != null && h.slots.size() > 36;
    }

    private boolean isShulkerOpen() {
        if (this.mc.player == null) {
            return false;
        }
        ScreenHandler h = this.mc.player.currentScreenHandler;
        return h instanceof ShulkerBoxScreenHandler;
    }

    private void closeIfDone() {
        if (!this.closeShulkerOnDone.get().booleanValue()) {
            return;
        }
        if (!this.isShulkerOpen()) {
            return;
        }
        this.safeClose();
    }

    private int findContainerSourceSlotAnyName(ScreenHandler h, int containerCount, Item item, Set<Integer> exclude) {
        for (int i = 0; i < containerCount; ++i) {
            ItemStack s;
            if (exclude.contains(i) || (s = h.getSlot(i).getStack()).isEmpty() || s.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private List<Integer> findAllContainerSourcesStrict(ScreenHandler h, int containerCount, Item item, String desiredName, Set<Integer> exclude) {
        boolean requireNamed = desiredName != null && !desiredName.isEmpty();
        ArrayList<Integer> out = new ArrayList<Integer>();
        for (int i = 0; i < containerCount; ++i) {
            ItemStack s;
            if (exclude.contains(i) || (s = h.getSlot(i).getStack()).isEmpty() || s.getItem() != item) continue;
            if (requireNamed) {
                if (!this.isCustomNamed(s) || !s.getName().getString().equals(desiredName)) continue;
                out.add(i);
                continue;
            }
            if (this.isCustomNamed(s)) continue;
            out.add(i);
        }
        out.sort(Comparator.comparingInt(idx -> h.getSlot(idx.intValue()).getStack().getCount()).reversed());
        return out;
    }

    private static enum Mode {
        Standard,
        Alternative,
        Smart;

    }

    public static enum TriggerMode {
        Manual,
        Automatic;

    }

    private record Move(int fromSlotId, int toInvIndex, String desiredItemId, String desiredName) {
    }
}

