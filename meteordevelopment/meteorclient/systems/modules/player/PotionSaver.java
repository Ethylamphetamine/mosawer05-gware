/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffects
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

public class PotionSaver
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<StatusEffect>> effects;
    public final Setting<Boolean> onlyWhenStationary;

    public PotionSaver() {
        super(Categories.Player, "potion-saver", "Stops potion effects ticking when you stand still.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.effects = this.sgGeneral.add(((StatusEffectListSetting.Builder)((StatusEffectListSetting.Builder)new StatusEffectListSetting.Builder().name("effects")).description("The effects to preserve.")).defaultValue((StatusEffect)StatusEffects.STRENGTH.comp_349(), (StatusEffect)StatusEffects.ABSORPTION.comp_349(), (StatusEffect)StatusEffects.RESISTANCE.comp_349(), (StatusEffect)StatusEffects.FIRE_RESISTANCE.comp_349(), (StatusEffect)StatusEffects.SPEED.comp_349(), (StatusEffect)StatusEffects.HASTE.comp_349(), (StatusEffect)StatusEffects.REGENERATION.comp_349(), (StatusEffect)StatusEffects.WATER_BREATHING.comp_349(), (StatusEffect)StatusEffects.SATURATION.comp_349(), (StatusEffect)StatusEffects.LUCK.comp_349(), (StatusEffect)StatusEffects.SLOW_FALLING.comp_349(), (StatusEffect)StatusEffects.DOLPHINS_GRACE.comp_349(), (StatusEffect)StatusEffects.CONDUIT_POWER.comp_349(), (StatusEffect)StatusEffects.HERO_OF_THE_VILLAGE.comp_349()).build());
        this.onlyWhenStationary = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-when-stationary")).description("Only freezes effects when you aren't moving.")).defaultValue(false)).build());
    }

    public boolean shouldFreeze(StatusEffect effect) {
        return this.isActive() && (this.onlyWhenStationary.get() == false || !PlayerUtils.isMoving()) && !this.mc.player.getStatusEffects().isEmpty() && this.effects.get().contains(effect);
    }
}

