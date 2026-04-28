/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class GenericSetting<T extends ICopyable<T> & ISerializable<T>>
extends Setting<T> {
    public GenericSetting(String name, String description, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        if (this.value == null) {
            this.value = ((ICopyable)this.defaultValue).copy();
        }
        ((ICopyable)this.value).set((ICopyable)this.defaultValue);
    }

    @Override
    protected T parseImpl(String str) {
        return ((ICopyable)this.defaultValue).copy();
    }

    @Override
    protected boolean isValueValid(T value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.put("value", (NbtElement)((ISerializable)((Object)((ICopyable)this.get()))).toTag());
        return tag;
    }

    @Override
    public T load(NbtCompound tag) {
        ((ISerializable)((Object)((ICopyable)this.get()))).fromTag(tag.getCompound("value"));
        return (T)((ICopyable)this.get());
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T>>
    extends Setting.SettingBuilder<Builder<T>, T, GenericSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public GenericSetting<T> build() {
            return new GenericSetting<ICopyable>(this.name, this.description, (ICopyable)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

