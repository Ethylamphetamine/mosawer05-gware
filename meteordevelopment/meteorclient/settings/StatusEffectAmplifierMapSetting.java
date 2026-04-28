/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class StatusEffectAmplifierMapSetting
extends Setting<Reference2IntMap<StatusEffect>> {
    public static final Reference2IntMap<StatusEffect> EMPTY_STATUS_EFFECT_MAP = StatusEffectAmplifierMapSetting.createStatusEffectMap();

    public StatusEffectAmplifierMapSetting(String name, String description, Reference2IntMap<StatusEffect> defaultValue, Consumer<Reference2IntMap<StatusEffect>> onChanged, Consumer<Setting<Reference2IntMap<StatusEffect>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new Reference2IntOpenHashMap((Reference2IntMap)this.defaultValue);
    }

    @Override
    protected Reference2IntMap<StatusEffect> parseImpl(String str) {
        String[] values = str.split(",");
        Reference2IntOpenHashMap effects = new Reference2IntOpenHashMap(EMPTY_STATUS_EFFECT_MAP);
        try {
            for (String value : values) {
                String[] split = value.split(" ");
                StatusEffect effect = (StatusEffect)StatusEffectAmplifierMapSetting.parseId(Registries.STATUS_EFFECT, split[0]);
                int level = Integer.parseInt(split[1]);
                effects.put((Object)effect, level);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return effects;
    }

    @Override
    protected boolean isValueValid(Reference2IntMap<StatusEffect> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (StatusEffect statusEffect : ((Reference2IntMap)this.get()).keySet()) {
            Identifier id = Registries.STATUS_EFFECT.getId((Object)statusEffect);
            if (id == null) continue;
            valueTag.putInt(id.toString(), ((Reference2IntMap)this.get()).getInt((Object)statusEffect));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    private static Reference2IntMap<StatusEffect> createStatusEffectMap() {
        Reference2IntArrayMap map = new Reference2IntArrayMap(Registries.STATUS_EFFECT.getIds().size());
        Registries.STATUS_EFFECT.forEach(arg_0 -> StatusEffectAmplifierMapSetting.lambda$createStatusEffectMap$0((Reference2IntMap)map, arg_0));
        return map;
    }

    @Override
    public Reference2IntMap<StatusEffect> load(NbtCompound tag) {
        ((Reference2IntMap)this.get()).clear();
        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            StatusEffect statusEffect = (StatusEffect)Registries.STATUS_EFFECT.get(Identifier.of((String)key));
            if (statusEffect == null) continue;
            ((Reference2IntMap)this.get()).put((Object)statusEffect, valueTag.getInt(key));
        }
        return (Reference2IntMap)this.get();
    }

    private static /* synthetic */ void lambda$createStatusEffectMap$0(Reference2IntMap map, StatusEffect potion) {
        map.put((Object)potion, 0);
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Reference2IntMap<StatusEffect>, StatusEffectAmplifierMapSetting> {
        public Builder() {
            super(new Reference2IntOpenHashMap(0));
        }

        @Override
        public StatusEffectAmplifierMapSetting build() {
            return new StatusEffectAmplifierMapSetting(this.name, this.description, (Reference2IntMap<StatusEffect>)((Reference2IntMap)this.defaultValue), this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

