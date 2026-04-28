/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
 *  net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen$CreativeScreenHandler
 *  net.minecraft.entity.mob.SkeletonHorseEntity
 *  net.minecraft.entity.mob.ZombieHorseEntity
 *  net.minecraft.entity.passive.AbstractDonkeyEntity
 *  net.minecraft.entity.passive.AbstractHorseEntity
 *  net.minecraft.entity.passive.HorseEntity
 *  net.minecraft.entity.passive.LlamaEntity
 *  net.minecraft.item.ItemGroups
 *  net.minecraft.registry.Registries
 *  net.minecraft.screen.AnvilScreenHandler
 *  net.minecraft.screen.BeaconScreenHandler
 *  net.minecraft.screen.BlastFurnaceScreenHandler
 *  net.minecraft.screen.BrewingStandScreenHandler
 *  net.minecraft.screen.CartographyTableScreenHandler
 *  net.minecraft.screen.CraftingScreenHandler
 *  net.minecraft.screen.EnchantmentScreenHandler
 *  net.minecraft.screen.FurnaceScreenHandler
 *  net.minecraft.screen.Generic3x3ContainerScreenHandler
 *  net.minecraft.screen.GenericContainerScreenHandler
 *  net.minecraft.screen.GrindstoneScreenHandler
 *  net.minecraft.screen.HopperScreenHandler
 *  net.minecraft.screen.HorseScreenHandler
 *  net.minecraft.screen.LecternScreenHandler
 *  net.minecraft.screen.LoomScreenHandler
 *  net.minecraft.screen.MerchantScreenHandler
 *  net.minecraft.screen.PlayerScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.ShulkerBoxScreenHandler
 *  net.minecraft.screen.SmokerScreenHandler
 *  net.minecraft.screen.StonecutterScreenHandler
 */
package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.CreativeInventoryScreenAccessor;
import meteordevelopment.meteorclient.mixin.HorseScreenHandlerAccessor;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;

public class SlotUtils {
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int OFFHAND = 45;
    public static final int MAIN_START = 9;
    public static final int MAIN_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;

    private SlotUtils() {
    }

    public static int indexToId(int i) {
        if (MeteorClient.mc.player == null) {
            return -1;
        }
        ScreenHandler handler = MeteorClient.mc.player.currentScreenHandler;
        if (handler instanceof PlayerScreenHandler) {
            return SlotUtils.survivalInventory(i);
        }
        if (handler instanceof CreativeInventoryScreen.CreativeScreenHandler) {
            return SlotUtils.creativeInventory(i);
        }
        if (handler instanceof GenericContainerScreenHandler) {
            GenericContainerScreenHandler genericContainerScreenHandler = (GenericContainerScreenHandler)handler;
            return SlotUtils.genericContainer(i, genericContainerScreenHandler.getRows());
        }
        if (handler instanceof CraftingScreenHandler) {
            return SlotUtils.craftingTable(i);
        }
        if (handler instanceof FurnaceScreenHandler) {
            return SlotUtils.furnace(i);
        }
        if (handler instanceof BlastFurnaceScreenHandler) {
            return SlotUtils.furnace(i);
        }
        if (handler instanceof SmokerScreenHandler) {
            return SlotUtils.furnace(i);
        }
        if (handler instanceof Generic3x3ContainerScreenHandler) {
            return SlotUtils.generic3x3(i);
        }
        if (handler instanceof EnchantmentScreenHandler) {
            return SlotUtils.enchantmentTable(i);
        }
        if (handler instanceof BrewingStandScreenHandler) {
            return SlotUtils.brewingStand(i);
        }
        if (handler instanceof MerchantScreenHandler) {
            return SlotUtils.villager(i);
        }
        if (handler instanceof BeaconScreenHandler) {
            return SlotUtils.beacon(i);
        }
        if (handler instanceof AnvilScreenHandler) {
            return SlotUtils.anvil(i);
        }
        if (handler instanceof HopperScreenHandler) {
            return SlotUtils.hopper(i);
        }
        if (handler instanceof ShulkerBoxScreenHandler) {
            return SlotUtils.genericContainer(i, 3);
        }
        if (handler instanceof HorseScreenHandler) {
            return SlotUtils.horse(handler, i);
        }
        if (handler instanceof CartographyTableScreenHandler) {
            return SlotUtils.cartographyTable(i);
        }
        if (handler instanceof GrindstoneScreenHandler) {
            return SlotUtils.grindstone(i);
        }
        if (handler instanceof LecternScreenHandler) {
            return SlotUtils.lectern();
        }
        if (handler instanceof LoomScreenHandler) {
            return SlotUtils.loom(i);
        }
        if (handler instanceof StonecutterScreenHandler) {
            return SlotUtils.stonecutter(i);
        }
        return -1;
    }

    private static int survivalInventory(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 36 + i;
        }
        if (SlotUtils.isArmor(i)) {
            return 5 + (i - 36);
        }
        return i;
    }

    private static int creativeInventory(int i) {
        if (!(MeteorClient.mc.currentScreen instanceof CreativeInventoryScreen) || CreativeInventoryScreenAccessor.getSelectedTab() != Registries.ITEM_GROUP.get(ItemGroups.INVENTORY)) {
            return -1;
        }
        return SlotUtils.survivalInventory(i);
    }

    private static int genericContainer(int i, int rows) {
        if (SlotUtils.isHotbar(i)) {
            return (rows + 3) * 9 + i;
        }
        if (SlotUtils.isMain(i)) {
            return rows * 9 + (i - 9);
        }
        return -1;
    }

    private static int craftingTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 37 + i;
        }
        if (SlotUtils.isMain(i)) {
            return i + 1;
        }
        return -1;
    }

    private static int furnace(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int generic3x3(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 36 + i;
        }
        if (SlotUtils.isMain(i)) {
            return i;
        }
        return -1;
    }

    private static int enchantmentTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 29 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 2 + (i - 9);
        }
        return -1;
    }

    private static int brewingStand(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 32 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 5 + (i - 9);
        }
        return -1;
    }

    private static int villager(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int beacon(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 28 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 1 + (i - 9);
        }
        return -1;
    }

    private static int anvil(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int hopper(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 32 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 5 + (i - 9);
        }
        return -1;
    }

    private static int horse(ScreenHandler handler, int i) {
        AbstractHorseEntity entity = ((HorseScreenHandlerAccessor)handler).getEntity();
        if (entity instanceof LlamaEntity) {
            LlamaEntity llamaEntity = (LlamaEntity)entity;
            int strength = llamaEntity.getStrength();
            if (SlotUtils.isHotbar(i)) {
                return 2 + 3 * strength + 28 + i;
            }
            if (SlotUtils.isMain(i)) {
                return 2 + 3 * strength + 1 + (i - 9);
            }
        } else if (entity instanceof HorseEntity || entity instanceof SkeletonHorseEntity || entity instanceof ZombieHorseEntity) {
            if (SlotUtils.isHotbar(i)) {
                return 29 + i;
            }
            if (SlotUtils.isMain(i)) {
                return 2 + (i - 9);
            }
        } else if (entity instanceof AbstractDonkeyEntity) {
            AbstractDonkeyEntity abstractDonkeyEntity = (AbstractDonkeyEntity)entity;
            boolean chest = abstractDonkeyEntity.hasChest();
            if (SlotUtils.isHotbar(i)) {
                return (chest ? 44 : 29) + i;
            }
            if (SlotUtils.isMain(i)) {
                return (chest ? 17 : 2) + (i - 9);
            }
        }
        return -1;
    }

    private static int cartographyTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int grindstone(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int lectern() {
        return -1;
    }

    private static int loom(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 31 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 4 + (i - 9);
        }
        return -1;
    }

    private static int stonecutter(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 29 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 2 + (i - 9);
        }
        return -1;
    }

    public static boolean isHotbar(int i) {
        return i >= 0 && i <= 8;
    }

    public static boolean isMain(int i) {
        return i >= 9 && i <= 35;
    }

    public static boolean isArmor(int i) {
        return i >= 36 && i <= 39;
    }
}

