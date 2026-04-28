/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.registry.Registries
 *  net.minecraft.world.LightType
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.StatusEffectInstanceAccessor;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.world.LightType;

public class Fullbright
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    public final Setting<LightType> lightType;
    private final Setting<Integer> minimumLightLevel;

    public Fullbright() {
        super(Categories.Render, "fullbright", "Lights up your world!");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode to use for Fullbright.")).defaultValue(Mode.Gamma)).onChanged(mode -> {
            if (this.isActive()) {
                if (mode != Mode.Potion) {
                    this.disableNightVision();
                }
                if (this.mc.worldRenderer != null) {
                    this.mc.worldRenderer.reload();
                }
            }
        })).build());
        this.lightType = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("light-type")).description("Which type of light to use for Luminance mode.")).defaultValue(LightType.BLOCK)).visible(() -> this.mode.get() == Mode.Luminance)).onChanged(integer -> {
            if (this.mc.worldRenderer != null && this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
        this.minimumLightLevel = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-light-level")).description("Minimum light level when using Luminance mode.")).visible(() -> this.mode.get() == Mode.Luminance)).defaultValue(8)).range(0, 15).sliderMax(15).onChanged(integer -> {
            if (this.mc.worldRenderer != null && this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
    }

    @Override
    public void onActivate() {
        if (this.mode.get() == Mode.Luminance) {
            this.mc.worldRenderer.reload();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.mode.get() == Mode.Luminance) {
            this.mc.worldRenderer.reload();
        } else if (this.mode.get() == Mode.Potion) {
            this.disableNightVision();
        }
    }

    public int getLuminance(LightType type) {
        if (!this.isActive() || this.mode.get() != Mode.Luminance || type != this.lightType.get()) {
            return 0;
        }
        return this.minimumLightLevel.get();
    }

    public boolean getGamma() {
        return this.isActive() && this.mode.get() == Mode.Gamma;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.player == null || !this.mode.get().equals((Object)Mode.Potion)) {
            return;
        }
        if (this.mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)StatusEffects.NIGHT_VISION.comp_349())))) {
            StatusEffectInstance instance = this.mc.player.getStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)StatusEffects.NIGHT_VISION.comp_349())));
            if (instance != null && instance.getDuration() < 420) {
                ((StatusEffectInstanceAccessor)instance).setDuration(420);
            }
        } else {
            this.mc.player.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)StatusEffects.NIGHT_VISION.comp_349())), 420, 0));
        }
    }

    private void disableNightVision() {
        if (this.mc.player == null) {
            return;
        }
        if (this.mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)StatusEffects.NIGHT_VISION.comp_349())))) {
            this.mc.player.removeStatusEffect(Registries.STATUS_EFFECT.getEntry((Object)((StatusEffect)StatusEffects.NIGHT_VISION.comp_349())));
        }
    }

    public static enum Mode {
        Gamma,
        Potion,
        Luminance;

    }
}

