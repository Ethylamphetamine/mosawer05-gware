/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class StatusEffectListSetting
extends Setting<List<StatusEffect>> {
    public StatusEffectListSetting(String name, String description, List<StatusEffect> defaultValue, Consumer<List<StatusEffect>> onChanged, Consumer<Setting<List<StatusEffect>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<StatusEffect> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<StatusEffect> effects = new ArrayList<StatusEffect>(values.length);
        try {
            for (String value : values) {
                StatusEffect effect = (StatusEffect)StatusEffectListSetting.parseId(Registries.STATUS_EFFECT, value);
                if (effect == null) continue;
                effects.add(effect);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return effects;
    }

    @Override
    protected boolean isValueValid(List<StatusEffect> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.STATUS_EFFECT.getIds();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (StatusEffect effect : (List)this.get()) {
            Identifier id = Registries.STATUS_EFFECT.getId((Object)effect);
            if (id == null) continue;
            valueTag.add((Object)NbtString.of((String)id.toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public List<StatusEffect> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            StatusEffect effect = (StatusEffect)Registries.STATUS_EFFECT.get(Identifier.of((String)tagI.asString()));
            if (effect == null) continue;
            ((List)this.get()).add(effect);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<StatusEffect>, StatusEffectListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(StatusEffect ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public StatusEffectListSetting build() {
            return new StatusEffectListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

