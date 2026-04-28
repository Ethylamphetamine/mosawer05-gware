/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.GameMenuScreen
 *  net.minecraft.item.Item
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IClientPlayerInteractionManager;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;

public class SwapManager {
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private final Object swapLock = new Object();
    private SwapState multiTickSwapState = new SwapState(this);
    private SwapState instantSwapState = new SwapState(this);

    public SwapManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean beginSwap(Item item, boolean instant) {
        FindItemResult result = InvUtils.findInHotbar(item);
        if (this.getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return false;
        }
        if (this.getItemSwapMode() == SwapMode.SilentHotbar && !result.found()) {
            return false;
        }
        if (!result.found()) {
            result = InvUtils.find(item);
        }
        if (!result.found()) {
            return false;
        }
        return this.beginSwap(result, instant);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean beginSwap(FindItemResult result, boolean instant) {
        if (!result.found()) {
            return false;
        }
        if (this.getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return false;
        }
        if (!instant && MeteorClient.mc.player.isUsingItem() && MeteorClient.mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return false;
        }
        Object object = this.swapLock;
        synchronized (object) {
            if (this.instantSwapState.isSwapped) {
                return false;
            }
            if (this.multiTickSwapState.isSwapped && !instant) {
                return false;
            }
            this.getSwapState((boolean)instant).isSwapped = true;
        }
        SwapState swapState = this.getSwapState(instant);
        switch (this.getItemSwapMode().ordinal()) {
            case 2: {
                swapState.hotbarSelectedSlot = MeteorClient.mc.player.getInventory().selectedSlot;
                swapState.hotbarItemSlot = result.slot();
                swapState.didSilentSwap = false;
                MeteorClient.mc.player.getInventory().selectedSlot = result.slot();
                ((IClientPlayerInteractionManager)MeteorClient.mc.interactionManager).meteor$syncSelected();
                break;
            }
            case 1: {
                boolean shouldSilentSwap;
                boolean bl = shouldSilentSwap = !result.isHotbar() || this.multiTickSwapState.isSwapped && instant || MeteorClient.mc.player.isUsingItem() && MeteorClient.mc.player.getActiveHand() == Hand.MAIN_HAND;
                if (shouldSilentSwap) {
                    if (this.antiCheatConfig.swapAntiScreenClose.get().booleanValue() && MeteorClient.mc.currentScreen instanceof GameMenuScreen) {
                        this.getSwapState((boolean)instant).isSwapped = false;
                        return false;
                    }
                    swapState.silentSwapInventorySlot = result.slot();
                    swapState.silentSwapSelectedSlot = MeteorClient.mc.player.getInventory().selectedSlot;
                    swapState.didSilentSwap = true;
                    InvUtils.quickSwap().fromId(MeteorClient.mc.player.getInventory().selectedSlot).to(result.slot());
                    break;
                }
                swapState.hotbarSelectedSlot = MeteorClient.mc.player.getInventory().selectedSlot;
                swapState.hotbarItemSlot = result.slot();
                swapState.didSilentSwap = false;
                MeteorClient.mc.player.getInventory().selectedSlot = result.slot();
                ((IClientPlayerInteractionManager)MeteorClient.mc.interactionManager).meteor$syncSelected();
                break;
            }
            case 3: {
                if (this.antiCheatConfig.swapAntiScreenClose.get().booleanValue() && MeteorClient.mc.currentScreen instanceof GameMenuScreen) {
                    this.getSwapState((boolean)instant).isSwapped = false;
                    return false;
                }
                swapState.silentSwapInventorySlot = result.slot();
                swapState.silentSwapSelectedSlot = MeteorClient.mc.player.getInventory().selectedSlot;
                swapState.didSilentSwap = true;
                InvUtils.quickSwap().fromId(MeteorClient.mc.player.getInventory().selectedSlot).to(result.slot());
                break;
            }
        }
        return true;
    }

    public boolean canSwap(Item item) {
        return this.getSlot(item).found();
    }

    public FindItemResult getSlot(Item item) {
        FindItemResult result = InvUtils.findInHotbar(item);
        if (this.getItemSwapMode() == SwapMode.None && !result.isMainHand()) {
            return new FindItemResult(-1, 0);
        }
        if (this.getItemSwapMode() == SwapMode.SilentHotbar && !result.found()) {
            return new FindItemResult(-1, 0);
        }
        if (!result.found()) {
            result = InvUtils.find(item);
        }
        if (!result.found()) {
            return new FindItemResult(-1, 0);
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void endSwap(boolean instantSwap) {
        Object object = this.swapLock;
        synchronized (object) {
            if (instantSwap && !this.getSwapState((boolean)instantSwap).isSwapped) {
                return;
            }
        }
        SwapState swapState = this.getSwapState(instantSwap);
        if (swapState.didSilentSwap) {
            InvUtils.quickSwap().fromId(swapState.silentSwapSelectedSlot).to(swapState.silentSwapInventorySlot);
        } else {
            MeteorClient.mc.player.getInventory().selectedSlot = swapState.hotbarSelectedSlot;
            ((IClientPlayerInteractionManager)MeteorClient.mc.interactionManager).meteor$syncSelected();
        }
        swapState.isSwapped = false;
    }

    public SwapMode getItemSwapMode() {
        return this.antiCheatConfig.swapMode.get();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (MeteorClient.mc.world == null || MeteorClient.mc.player == null) {
            return;
        }
        if (this.multiTickSwapState.isSwapped) {
            if (this.multiTickSwapState.didSilentSwap) {
                if (this.multiTickSwapState.silentSwapSelectedSlot != MeteorClient.mc.player.getInventory().selectedSlot) {
                    MeteorClient.mc.player.getInventory().selectedSlot = this.multiTickSwapState.silentSwapSelectedSlot;
                    ((IClientPlayerInteractionManager)MeteorClient.mc.interactionManager).meteor$syncSelected();
                }
            } else if (this.multiTickSwapState.hotbarItemSlot != MeteorClient.mc.player.getInventory().selectedSlot) {
                MeteorClient.mc.player.getInventory().selectedSlot = this.multiTickSwapState.hotbarItemSlot;
                ((IClientPlayerInteractionManager)MeteorClient.mc.interactionManager).meteor$syncSelected();
            }
        }
    }

    private SwapState getSwapState(boolean instantSwap) {
        if (instantSwap) {
            return this.instantSwapState;
        }
        return this.multiTickSwapState;
    }

    private class SwapState {
        public boolean isSwapped = false;
        public boolean didSilentSwap = false;
        public int hotbarSelectedSlot = 0;
        public int hotbarItemSlot = 0;
        public int silentSwapSelectedSlot = 0;
        public int silentSwapInventorySlot = 0;

        private SwapState(SwapManager swapManager) {
        }
    }

    public static enum SwapMode {
        None,
        Auto,
        SilentHotbar,
        SilentSwap;

    }
}

