/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.registry.Registry
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.reflect.AccessFlag;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class EnchantmentListSetting
extends Setting<Set<RegistryKey<Enchantment>>> {
    public EnchantmentListSetting(String name, String description, Set<RegistryKey<Enchantment>> defaultValue, Consumer<Set<RegistryKey<Enchantment>>> onChanged, Consumer<Setting<Set<RegistryKey<Enchantment>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection)this.defaultValue);
    }

    @Override
    protected Set<RegistryKey<Enchantment>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet enchs = new ObjectOpenHashSet(values.length);
        for (String value : values) {
            String name = value.trim();
            Identifier id = name.contains(":") ? Identifier.of((String)name) : Identifier.ofVanilla((String)name);
            enchs.add(RegistryKey.of((RegistryKey)RegistryKeys.ENCHANTMENT, (Identifier)id));
        }
        return enchs;
    }

    @Override
    protected boolean isValueValid(Set<RegistryKey<Enchantment>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Optional.ofNullable(MinecraftClient.getInstance().getNetworkHandler()).flatMap(networkHandler -> networkHandler.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT)).map(Registry::getIds).orElse(Set.of());
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (RegistryKey ench : (Set)this.get()) {
            valueTag.add((Object)NbtString.of((String)ench.getValue().toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public Set<RegistryKey<Enchantment>> load(NbtCompound tag) {
        ((Set)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            ((Set)this.get()).add(RegistryKey.of((RegistryKey)RegistryKeys.ENCHANTMENT, (Identifier)Identifier.of((String)tagI.asString())));
        }
        return (Set)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Set<RegistryKey<Enchantment>>, EnchantmentListSetting> {
        private static final Set<RegistryKey<Enchantment>> VANILLA_DEFAULTS = Arrays.stream(Enchantments.class.getDeclaredFields()).filter(field -> field.accessFlags().containsAll(List.of(AccessFlag.PUBLIC, AccessFlag.STATIC, AccessFlag.FINAL))).filter(field -> field.getType() == RegistryKey.class).map(field -> {
            try {
                return field.get(null);
            }
            catch (IllegalAccessException e) {
                return null;
            }
        }).filter(Objects::nonNull).map(RegistryKey.class::cast).filter(registryKey -> registryKey.getRegistryRef() == RegistryKeys.ENCHANTMENT).collect(Collectors.toSet());

        public Builder() {
            super(new ObjectOpenHashSet());
        }

        public Builder vanillaDefaults() {
            return (Builder)this.defaultValue(VANILLA_DEFAULTS);
        }

        @Override
        @SafeVarargs
        public final Builder defaultValue(RegistryKey<Enchantment> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? new ObjectOpenHashSet((Object[])defaults) : new ObjectOpenHashSet());
        }

        @Override
        public EnchantmentListSetting build() {
            return new EnchantmentListSetting(this.name, this.description, (Set)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

