/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.SwordItem
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoWeapon
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Weapon> weapon;
    private final Setting<Integer> threshold;
    private final Setting<Boolean> antiBreak;

    public AutoWeapon() {
        super(Categories.Combat, "auto-weapon", "Finds the best weapon to use in your hotbar.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.weapon = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("weapon")).description("What type of weapon to use.")).defaultValue(Weapon.Sword)).build());
        this.threshold = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("threshold")).description("If the non-preferred weapon produces this much damage this will favor it over your preferred weapon.")).defaultValue(4)).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Prevents you from breaking your weapon.")).defaultValue(false)).build());
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        Entity entity = event.entity;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            InvUtils.swap(this.getBestWeapon(livingEntity), false);
        }
    }

    private int getBestWeapon(LivingEntity target) {
        int slotS = this.mc.player.getInventory().selectedSlot;
        int slotA = this.mc.player.getInventory().selectedSlot;
        double damageS = 0.0;
        double damageA = 0.0;
        for (int i = 0; i < 9; ++i) {
            double currentDamageA;
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem && (!this.antiBreak.get().booleanValue() || stack.getMaxDamage() - stack.getDamage() > 10)) {
                double currentDamageS = DamageUtils.getAttackDamage((LivingEntity)this.mc.player, target, stack);
                if (!(currentDamageS > damageS)) continue;
                damageS = currentDamageS;
                slotS = i;
                continue;
            }
            if (!(stack.getItem() instanceof AxeItem) || this.antiBreak.get().booleanValue() && stack.getMaxDamage() - stack.getDamage() <= 10 || !((currentDamageA = (double)DamageUtils.getAttackDamage((LivingEntity)this.mc.player, target, stack)) > damageA)) continue;
            damageA = currentDamageA;
            slotA = i;
        }
        if (this.weapon.get() == Weapon.Sword && (double)this.threshold.get().intValue() > damageA - damageS) {
            return slotS;
        }
        if (this.weapon.get() == Weapon.Axe && (double)this.threshold.get().intValue() > damageS - damageA) {
            return slotA;
        }
        if (this.weapon.get() == Weapon.Sword && (double)this.threshold.get().intValue() < damageA - damageS) {
            return slotA;
        }
        if (this.weapon.get() == Weapon.Axe && (double)this.threshold.get().intValue() < damageS - damageA) {
            return slotS;
        }
        return this.mc.player.getInventory().selectedSlot;
    }

    public static enum Weapon {
        Sword,
        Axe;

    }
}

