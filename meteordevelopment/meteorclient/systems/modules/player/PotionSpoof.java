/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.registry.Registries
 */
package meteordevelopment.meteorclient.systems.modules.player;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.StatusEffectInstanceAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;

public class PotionSpoof
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Reference2IntMap<StatusEffect>> spoofPotions;
    private final Setting<Boolean> clearEffects;
    private final Setting<List<StatusEffect>> antiPotion;
    private final Setting<Integer> effectDuration;

    public PotionSpoof() {
        super(Categories.Player, "potion-spoof", "Spoofs potion statuses for you. SOME effects DO NOT work.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.spoofPotions = this.sgGeneral.add(((StatusEffectAmplifierMapSetting.Builder)((StatusEffectAmplifierMapSetting.Builder)((StatusEffectAmplifierMapSetting.Builder)new StatusEffectAmplifierMapSetting.Builder().name("spoofed-potions")).description("Potions to add.")).defaultValue(Utils.createStatusEffectMap())).build());
        this.clearEffects = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("clear-effects")).description("Clears effects on module disable.")).defaultValue(true)).build());
        this.antiPotion = this.sgGeneral.add(((StatusEffectListSetting.Builder)((StatusEffectListSetting.Builder)new StatusEffectListSetting.Builder().name("blocked-potions")).description("Potions to block.")).defaultValue((StatusEffect)StatusEffects.LEVITATION.comp_349(), (StatusEffect)StatusEffects.JUMP_BOOST.comp_349(), (StatusEffect)StatusEffects.SLOW_FALLING.comp_349(), (StatusEffect)StatusEffects.DOLPHINS_GRACE.comp_349()).build());
        this.effectDuration = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("effect-duration")).description("How many ticks to spoof the effect for.")).range(1, Short.MAX_VALUE).sliderRange(20, 500).defaultValue(420)).build());
    }

    @Override
    public void onDeactivate() {
        if (!this.clearEffects.get().booleanValue() || !Utils.canUpdate()) {
            return;
        }
        for (Reference2IntMap.Entry entry : this.spoofPotions.get().reference2IntEntrySet()) {
            if (entry.getIntValue() <= 0 || !this.mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)entry.getKey())))) continue;
            this.mc.player.removeStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)entry.getKey())));
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (Reference2IntMap.Entry entry : this.spoofPotions.get().reference2IntEntrySet()) {
            int level = entry.getIntValue();
            if (level <= 0) continue;
            if (this.mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)entry.getKey())))) {
                StatusEffectInstance instance = this.mc.player.getStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)entry.getKey())));
                ((StatusEffectInstanceAccessor)instance).setAmplifier(level - 1);
                if (instance.getDuration() >= this.effectDuration.get()) continue;
                ((StatusEffectInstanceAccessor)instance).setDuration(this.effectDuration.get());
                continue;
            }
            this.mc.player.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)entry.getKey())), this.effectDuration.get().intValue(), level - 1));
        }
    }

    public boolean shouldBlock(StatusEffect effect) {
        return this.isActive() && this.antiPotion.get().contains(effect);
    }
}

