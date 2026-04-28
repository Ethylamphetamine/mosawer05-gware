/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

public class EnumSetting<T extends Enum<?>>
extends Setting<T> {
    private final T[] values;
    private final List<String> suggestions;

    public EnumSetting(String name, String description, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.values = (Enum[])((Enum)defaultValue).getDeclaringClass().getEnumConstants();
        this.suggestions = new ArrayList<String>(this.values.length);
        for (T value : this.values) {
            this.suggestions.add(((Enum)value).toString());
        }
    }

    @Override
    protected T parseImpl(String str) {
        for (T possibleValue : this.values) {
            if (!str.equalsIgnoreCase(((Enum)possibleValue).toString())) continue;
            return possibleValue;
        }
        return null;
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return this.suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", ((Enum)this.get()).toString());
        return tag;
    }

    @Override
    public T load(NbtCompound tag) {
        this.parse(tag.getString("value"));
        return (T)((Enum)this.get());
    }

    public static class Builder<T extends Enum<?>>
    extends Setting.SettingBuilder<Builder<T>, T, EnumSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public EnumSetting<T> build() {
            return new EnumSetting<Enum>(this.name, this.description, (Enum)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

