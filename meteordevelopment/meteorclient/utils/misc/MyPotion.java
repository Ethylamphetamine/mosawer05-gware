/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.Potions
 *  net.minecraft.registry.entry.RegistryEntry
 */
package meteordevelopment.meteorclient.utils.misc;

import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;

public enum MyPotion {
    Swiftness((RegistryEntry<Potion>)Potions.SWIFTNESS, Items.NETHER_WART, Items.SUGAR),
    SwiftnessLong((RegistryEntry<Potion>)Potions.LONG_SWIFTNESS, Items.NETHER_WART, Items.SUGAR, Items.REDSTONE),
    SwiftnessStrong((RegistryEntry<Potion>)Potions.STRONG_SWIFTNESS, Items.NETHER_WART, Items.SUGAR, Items.GLOWSTONE_DUST),
    Slowness((RegistryEntry<Potion>)Potions.SLOWNESS, Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE),
    SlownessLong((RegistryEntry<Potion>)Potions.LONG_SLOWNESS, Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE),
    SlownessStrong((RegistryEntry<Potion>)Potions.STRONG_SLOWNESS, Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE, Items.GLOWSTONE_DUST),
    JumpBoost((RegistryEntry<Potion>)Potions.LEAPING, Items.NETHER_WART, Items.RABBIT_FOOT),
    JumpBoostLong((RegistryEntry<Potion>)Potions.LONG_LEAPING, Items.NETHER_WART, Items.RABBIT_FOOT, Items.REDSTONE),
    JumpBoostStrong((RegistryEntry<Potion>)Potions.STRONG_LEAPING, Items.NETHER_WART, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST),
    Strength((RegistryEntry<Potion>)Potions.STRENGTH, Items.NETHER_WART, Items.BLAZE_POWDER),
    StrengthLong((RegistryEntry<Potion>)Potions.LONG_STRENGTH, Items.NETHER_WART, Items.BLAZE_POWDER, Items.REDSTONE),
    StrengthStrong((RegistryEntry<Potion>)Potions.STRONG_STRENGTH, Items.NETHER_WART, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST),
    Healing((RegistryEntry<Potion>)Potions.HEALING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE),
    HealingStrong((RegistryEntry<Potion>)Potions.STRONG_HEALING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.GLOWSTONE_DUST),
    Harming((RegistryEntry<Potion>)Potions.HARMING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.FERMENTED_SPIDER_EYE),
    HarmingStrong((RegistryEntry<Potion>)Potions.STRONG_HARMING, Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.FERMENTED_SPIDER_EYE, Items.GLOWSTONE_DUST),
    Poison((RegistryEntry<Potion>)Potions.POISON, Items.NETHER_WART, Items.SPIDER_EYE),
    PoisonLong((RegistryEntry<Potion>)Potions.LONG_POISON, Items.NETHER_WART, Items.SPIDER_EYE, Items.REDSTONE),
    PoisonStrong((RegistryEntry<Potion>)Potions.STRONG_POISON, Items.NETHER_WART, Items.SPIDER_EYE, Items.GLOWSTONE_DUST),
    Regeneration((RegistryEntry<Potion>)Potions.REGENERATION, Items.NETHER_WART, Items.GHAST_TEAR),
    RegenerationLong((RegistryEntry<Potion>)Potions.LONG_REGENERATION, Items.NETHER_WART, Items.GHAST_TEAR, Items.REDSTONE),
    RegenerationStrong((RegistryEntry<Potion>)Potions.STRONG_REGENERATION, Items.NETHER_WART, Items.GHAST_TEAR, Items.GLOWSTONE_DUST),
    FireResistance((RegistryEntry<Potion>)Potions.FIRE_RESISTANCE, Items.NETHER_WART, Items.MAGMA_CREAM),
    FireResistanceLong((RegistryEntry<Potion>)Potions.LONG_FIRE_RESISTANCE, Items.NETHER_WART, Items.MAGMA_CREAM, Items.REDSTONE),
    WaterBreathing((RegistryEntry<Potion>)Potions.WATER_BREATHING, Items.NETHER_WART, Items.PUFFERFISH),
    WaterBreathingLong((RegistryEntry<Potion>)Potions.LONG_WATER_BREATHING, Items.NETHER_WART, Items.PUFFERFISH, Items.REDSTONE),
    NightVision((RegistryEntry<Potion>)Potions.NIGHT_VISION, Items.NETHER_WART, Items.GOLDEN_CARROT),
    NightVisionLong((RegistryEntry<Potion>)Potions.LONG_NIGHT_VISION, Items.NETHER_WART, Items.GOLDEN_CARROT, Items.REDSTONE),
    Invisibility((RegistryEntry<Potion>)Potions.INVISIBILITY, Items.NETHER_WART, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE),
    InvisibilityLong((RegistryEntry<Potion>)Potions.LONG_INVISIBILITY, Items.NETHER_WART, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE),
    TurtleMaster((RegistryEntry<Potion>)Potions.TURTLE_MASTER, Items.NETHER_WART, Items.TURTLE_HELMET),
    TurtleMasterLong((RegistryEntry<Potion>)Potions.LONG_TURTLE_MASTER, Items.NETHER_WART, Items.TURTLE_HELMET, Items.REDSTONE),
    TurtleMasterStrong((RegistryEntry<Potion>)Potions.STRONG_TURTLE_MASTER, Items.NETHER_WART, Items.TURTLE_HELMET, Items.GLOWSTONE_DUST),
    SlowFalling((RegistryEntry<Potion>)Potions.SLOW_FALLING, Items.NETHER_WART, Items.PHANTOM_MEMBRANE),
    SlowFallingLong((RegistryEntry<Potion>)Potions.LONG_SLOW_FALLING, Items.NETHER_WART, Items.PHANTOM_MEMBRANE, Items.REDSTONE),
    Weakness((RegistryEntry<Potion>)Potions.WEAKNESS, Items.FERMENTED_SPIDER_EYE),
    WeaknessLong((RegistryEntry<Potion>)Potions.LONG_WEAKNESS, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE);

    public final ItemStack potion;
    public final Item[] ingredients;

    private MyPotion(RegistryEntry<Potion> potion, Item ... ingredients) {
        this.potion = PotionContentsComponent.createStack((Item)Items.POTION, potion);
        this.ingredients = ingredients;
    }
}

