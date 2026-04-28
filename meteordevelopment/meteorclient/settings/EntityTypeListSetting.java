/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.SpawnGroup
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EntityTypeListSetting
extends Setting<Set<EntityType<?>>> {
    public final Predicate<EntityType<?>> filter;
    private List<String> suggestions;
    private static final List<String> groups = List.of("animal", "wateranimal", "monster", "ambient", "misc");

    public EntityTypeListSetting(String name, String description, Set<EntityType<?>> defaultValue, Consumer<Set<EntityType<?>>> onChanged, Consumer<Setting<Set<EntityType<?>>>> onModuleActivated, IVisible visible, Predicate<EntityType<?>> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection)this.defaultValue);
    }

    @Override
    protected Set<EntityType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet entities = new ObjectOpenHashSet(values.length);
        try {
            for (String value : values) {
                EntityType entity = (EntityType)EntityTypeListSetting.parseId(Registries.ENTITY_TYPE, value);
                if (entity != null) {
                    entities.add(entity);
                    continue;
                }
                String lowerValue = value.trim().toLowerCase();
                if (!groups.contains(lowerValue)) continue;
                for (EntityType entityType : Registries.ENTITY_TYPE) {
                    if (this.filter != null && !this.filter.test(entityType)) continue;
                    switch (lowerValue) {
                        case "animal": {
                            if (entityType.getSpawnGroup() != SpawnGroup.CREATURE) break;
                            entities.add(entityType);
                            break;
                        }
                        case "wateranimal": {
                            if (entityType.getSpawnGroup() != SpawnGroup.WATER_AMBIENT && entityType.getSpawnGroup() != SpawnGroup.WATER_CREATURE && entityType.getSpawnGroup() != SpawnGroup.UNDERGROUND_WATER_CREATURE && entityType.getSpawnGroup() != SpawnGroup.AXOLOTLS) break;
                            entities.add(entityType);
                            break;
                        }
                        case "monster": {
                            if (entityType.getSpawnGroup() != SpawnGroup.MONSTER) break;
                            entities.add(entityType);
                            break;
                        }
                        case "ambient": {
                            if (entityType.getSpawnGroup() != SpawnGroup.AMBIENT) break;
                            entities.add(entityType);
                            break;
                        }
                        case "misc": {
                            if (entityType.getSpawnGroup() != SpawnGroup.MISC) break;
                            entities.add(entityType);
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return entities;
    }

    @Override
    protected boolean isValueValid(Set<EntityType<?>> value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        if (this.suggestions == null) {
            this.suggestions = new ArrayList<String>(groups);
            for (EntityType entityType : Registries.ENTITY_TYPE) {
                if (this.filter != null && !this.filter.test(entityType)) continue;
                this.suggestions.add(Registries.ENTITY_TYPE.getId((Object)entityType).toString());
            }
        }
        return this.suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (EntityType entityType : (Set)this.get()) {
            valueTag.add((Object)NbtString.of((String)Registries.ENTITY_TYPE.getId((Object)entityType).toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public Set<EntityType<?>> load(NbtCompound tag) {
        ((Set)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            EntityType type = (EntityType)Registries.ENTITY_TYPE.get(Identifier.of((String)tagI.asString()));
            if (this.filter != null && !this.filter.test(type)) continue;
            ((Set)this.get()).add(type);
        }
        return (Set)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Set<EntityType<?>>, EntityTypeListSetting> {
        private Predicate<EntityType<?>> filter;

        public Builder() {
            super(new ObjectOpenHashSet(0));
        }

        @Override
        public Builder defaultValue(EntityType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? new ObjectOpenHashSet((Object[])defaults) : new ObjectOpenHashSet(0));
        }

        public Builder onlyAttackable() {
            this.filter = EntityUtils::isAttackable;
            return this;
        }

        public Builder filter(Predicate<EntityType<?>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public EntityTypeListSetting build() {
            return new EntityTypeListSetting(this.name, this.description, (Set)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}

