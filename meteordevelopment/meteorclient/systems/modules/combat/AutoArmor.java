/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class AutoArmor
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Protection> preferredProtection;
    private final Setting<Integer> delay;
    private final Setting<Set<RegistryKey<Enchantment>>> avoidedEnchantments;
    private final Setting<Boolean> blastLeggings;
    private final Setting<Boolean> antiBreak;
    private final Setting<Boolean> ignoreElytra;
    private final Object2IntMap<RegistryEntry<Enchantment>> enchantments;
    private final ArmorPiece[] armorPieces;
    private final ArmorPiece helmet;
    private final ArmorPiece chestplate;
    private final ArmorPiece leggings;
    private final ArmorPiece boots;
    private int timer;

    public AutoArmor() {
        super(Categories.Combat, "auto-armor", "Automatically equips armor.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.preferredProtection = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("preferred-protection")).description("Which type of protection to prefer.")).defaultValue(Protection.Protection)).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("swap-delay")).description("The delay between equipping armor pieces.")).defaultValue(1)).min(0).sliderMax(5).build());
        this.avoidedEnchantments = this.sgGeneral.add(((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)new EnchantmentListSetting.Builder().name("avoided-enchantments")).description("Enchantments that should be avoided.")).defaultValue(Enchantments.BINDING_CURSE, Enchantments.FROST_WALKER).build());
        this.blastLeggings = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blast-prot-leggings")).description("Uses blast protection for leggings regardless of preferred protection.")).defaultValue(true)).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Takes off armor if it is about to break.")).defaultValue(false)).build());
        this.ignoreElytra = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-elytra")).description("Will not replace your elytra if you have it equipped.")).defaultValue(true)).build());
        this.enchantments = new Object2IntOpenHashMap();
        this.armorPieces = new ArmorPiece[4];
        this.helmet = new ArmorPiece(3);
        this.chestplate = new ArmorPiece(2);
        this.leggings = new ArmorPiece(1);
        this.boots = new ArmorPiece(0);
        this.armorPieces[0] = this.helmet;
        this.armorPieces[1] = this.chestplate;
        this.armorPieces[2] = this.leggings;
        this.armorPieces[3] = this.boots;
    }

    @Override
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.reset();
        }
        block7: for (int i = 0; i < this.mc.player.getInventory().main.size(); ++i) {
            ItemStack itemStack = this.mc.player.getInventory().getStack(i);
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ArmorItem) || this.antiBreak.get().booleanValue() && itemStack.isDamageable() && itemStack.getMaxDamage() - itemStack.getDamage() <= 10) continue;
            Utils.getEnchantments(itemStack, this.enchantments);
            if (this.hasAvoidedEnchantment()) continue;
            switch (this.getItemSlotId(itemStack)) {
                case 0: {
                    this.boots.add(itemStack, i);
                    continue block7;
                }
                case 1: {
                    this.leggings.add(itemStack, i);
                    continue block7;
                }
                case 2: {
                    this.chestplate.add(itemStack, i);
                    continue block7;
                }
                case 3: {
                    this.helmet.add(itemStack, i);
                }
            }
        }
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.calculate();
        }
        Arrays.sort(this.armorPieces, Comparator.comparingInt(ArmorPiece::getSortScore));
        for (ArmorPiece armorPiece : this.armorPieces) {
            armorPiece.apply();
        }
    }

    private boolean hasAvoidedEnchantment() {
        for (RegistryEntry enchantment : this.enchantments.keySet()) {
            if (!enchantment.matches(this.avoidedEnchantments.get()::contains)) continue;
            return true;
        }
        return false;
    }

    private int getItemSlotId(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ElytraItem) {
            return 2;
        }
        return ((ArmorItem)itemStack.getItem()).getSlotType().getEntitySlotId();
    }

    private int getScore(ItemStack itemStack) {
        int n;
        int n2;
        ArmorItem armorItem;
        if (itemStack.isEmpty()) {
            return 0;
        }
        int score = 0;
        RegistryKey protection = this.preferredProtection.get().enchantment;
        if (itemStack.getItem() instanceof ArmorItem && this.blastLeggings.get().booleanValue() && this.getItemSlotId(itemStack) == 1) {
            protection = Enchantments.BLAST_PROTECTION;
        }
        score += 3 * Utils.getEnchantmentLevel(this.enchantments, protection);
        score += Utils.getEnchantmentLevel(this.enchantments, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (RegistryKey<Enchantment>)Enchantments.BLAST_PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (RegistryKey<Enchantment>)Enchantments.FIRE_PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (RegistryKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION);
        score += Utils.getEnchantmentLevel(this.enchantments, (RegistryKey<Enchantment>)Enchantments.UNBREAKING);
        score += 2 * Utils.getEnchantmentLevel(this.enchantments, (RegistryKey<Enchantment>)Enchantments.MENDING);
        Item item = itemStack.getItem();
        if (item instanceof ArmorItem) {
            armorItem = (ArmorItem)item;
            n2 = armorItem.getProtection();
        } else {
            n2 = 0;
        }
        score += n2;
        item = itemStack.getItem();
        if (item instanceof ArmorItem) {
            armorItem = (ArmorItem)item;
            n = (int)armorItem.getToughness();
        } else {
            n = 0;
        }
        return score += n;
    }

    private boolean cannotSwap() {
        return this.timer > 0;
    }

    private void swap(int from, int armorSlotId) {
        InvUtils.move().from(from).toArmor(armorSlotId);
        this.timer = this.delay.get();
    }

    private void moveToEmpty(int armorSlotId) {
        for (int i = 0; i < this.mc.player.getInventory().main.size(); ++i) {
            if (!this.mc.player.getInventory().getStack(i).isEmpty()) continue;
            InvUtils.move().fromArmor(armorSlotId).to(i);
            this.timer = this.delay.get();
            break;
        }
    }

    public static enum Protection {
        Protection((RegistryKey<Enchantment>)Enchantments.PROTECTION),
        BlastProtection((RegistryKey<Enchantment>)Enchantments.BLAST_PROTECTION),
        FireProtection((RegistryKey<Enchantment>)Enchantments.FIRE_PROTECTION),
        ProjectileProtection((RegistryKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION);

        private final RegistryKey<Enchantment> enchantment;

        private Protection(RegistryKey<Enchantment> enchantment) {
            this.enchantment = enchantment;
        }
    }

    private class ArmorPiece {
        private final int id;
        private int bestSlot;
        private int bestScore;
        private int score;
        private int durability;

        public ArmorPiece(int id) {
            this.id = id;
        }

        public void reset() {
            this.bestSlot = -1;
            this.bestScore = -1;
            this.score = -1;
            this.durability = Integer.MAX_VALUE;
        }

        public void add(ItemStack itemStack, int slot) {
            int score = AutoArmor.this.getScore(itemStack);
            if (score > this.bestScore) {
                this.bestScore = score;
                this.bestSlot = slot;
            }
        }

        public void calculate() {
            if (AutoArmor.this.cannotSwap()) {
                return;
            }
            ItemStack itemStack = ((AutoArmor)AutoArmor.this).mc.player.getInventory().getArmorStack(this.id);
            if ((AutoArmor.this.ignoreElytra.get().booleanValue() || Modules.get().isActive(ChestSwap.class)) && itemStack.getItem() == Items.ELYTRA) {
                this.score = Integer.MAX_VALUE;
                return;
            }
            Utils.getEnchantments(itemStack, AutoArmor.this.enchantments);
            if (AutoArmor.this.enchantments.containsKey((Object)Enchantments.BINDING_CURSE)) {
                this.score = Integer.MAX_VALUE;
                return;
            }
            this.score = AutoArmor.this.getScore(itemStack);
            this.score = this.decreaseScoreByAvoidedEnchantments(this.score);
            this.score = this.applyAntiBreakScore(this.score, itemStack);
            if (!itemStack.isEmpty()) {
                this.durability = itemStack.getMaxDamage() - itemStack.getDamage();
            }
        }

        public int getSortScore() {
            if (AutoArmor.this.antiBreak.get().booleanValue() && this.durability <= 10) {
                return -1;
            }
            return this.bestScore;
        }

        public void apply() {
            if (AutoArmor.this.cannotSwap() || this.score == Integer.MAX_VALUE) {
                return;
            }
            if (this.bestScore > this.score) {
                AutoArmor.this.swap(this.bestSlot, this.id);
            } else if (AutoArmor.this.antiBreak.get().booleanValue() && this.durability <= 10) {
                AutoArmor.this.moveToEmpty(this.id);
            }
        }

        private int decreaseScoreByAvoidedEnchantments(int score) {
            for (RegistryKey<Enchantment> enchantment : AutoArmor.this.avoidedEnchantments.get()) {
                score -= 2 * AutoArmor.this.enchantments.getInt(enchantment);
            }
            return score;
        }

        private int applyAntiBreakScore(int score, ItemStack itemStack) {
            if (AutoArmor.this.antiBreak.get().booleanValue() && itemStack.isDamageable() && itemStack.getMaxDamage() - itemStack.getDamage() <= 10) {
                return -1;
            }
            return score;
        }
    }
}

