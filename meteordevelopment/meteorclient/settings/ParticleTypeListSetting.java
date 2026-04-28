/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.particle.ParticleEffect
 *  net.minecraft.particle.ParticleType
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ParticleTypeListSetting
extends Setting<List<ParticleType<?>>> {
    public ParticleTypeListSetting(String name, String description, List<ParticleType<?>> defaultValue, Consumer<List<ParticleType<?>>> onChanged, Consumer<Setting<List<ParticleType<?>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<ParticleType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList particleTypes = new ArrayList(values.length);
        try {
            for (String value : values) {
                ParticleType particleType = (ParticleType)ParticleTypeListSetting.parseId(Registries.PARTICLE_TYPE, value);
                if (!(particleType instanceof ParticleEffect)) continue;
                particleTypes.add(particleType);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return particleTypes;
    }

    @Override
    protected boolean isValueValid(List<ParticleType<?>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.PARTICLE_TYPE.getIds();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (ParticleType particleType : (List)this.get()) {
            Identifier id = Registries.PARTICLE_TYPE.getId((Object)particleType);
            if (id == null) continue;
            valueTag.add((Object)NbtString.of((String)id.toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public List<ParticleType<?>> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            ParticleType particleType = (ParticleType)Registries.PARTICLE_TYPE.get(Identifier.of((String)tagI.asString()));
            if (particleType == null) continue;
            ((List)this.get()).add(particleType);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<ParticleType<?>>, ParticleTypeListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(ParticleType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public ParticleTypeListSetting build() {
            return new ParticleTypeListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

