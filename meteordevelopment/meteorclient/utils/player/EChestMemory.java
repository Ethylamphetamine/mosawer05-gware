/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.EnderChestBlock
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.inventory.Inventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.util.collection.DefaultedList
 */
package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.BlockActivateEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class EChestMemory {
    public static final DefaultedList<ItemStack> ITEMS = DefaultedList.ofSize((int)27, (Object)ItemStack.EMPTY);
    private static int echestOpenedState;
    private static boolean isKnown;

    private EChestMemory() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(EChestMemory.class);
    }

    @EventHandler
    private static void onBlockActivate(BlockActivateEvent event) {
        if (event.blockState.getBlock() instanceof EnderChestBlock && echestOpenedState == 0) {
            echestOpenedState = 1;
        }
    }

    @EventHandler
    private static void onOpenScreenEvent(OpenScreenEvent event) {
        if (echestOpenedState == 1 && event.screen instanceof GenericContainerScreen) {
            echestOpenedState = 2;
            return;
        }
        if (echestOpenedState == 0) {
            return;
        }
        if (!(MeteorClient.mc.currentScreen instanceof GenericContainerScreen)) {
            return;
        }
        GenericContainerScreenHandler container = (GenericContainerScreenHandler)((GenericContainerScreen)MeteorClient.mc.currentScreen).getScreenHandler();
        if (container == null) {
            return;
        }
        Inventory inv = container.getInventory();
        for (int i = 0; i < 27; ++i) {
            ITEMS.set(i, (Object)inv.getStack(i));
        }
        isKnown = true;
        echestOpenedState = 0;
    }

    @EventHandler
    private static void onLeaveEvent(GameLeftEvent event) {
        ITEMS.clear();
        isKnown = false;
    }

    public static boolean isKnown() {
        return isKnown;
    }

    static {
        isKnown = false;
    }
}

