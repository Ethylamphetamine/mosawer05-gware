/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.registry.Registry
 *  net.minecraft.util.Identifier
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class Setting<T>
implements IGetter<T>,
ISerializable<T> {
    private static final List<String> NO_SUGGESTIONS = new ArrayList<String>(0);
    public final String name;
    public final String title;
    public final String description;
    private final IVisible visible;
    protected final T defaultValue;
    protected T value;
    public final Consumer<Setting<T>> onModuleActivated;
    private final Consumer<T> onChanged;
    public Module module;
    public boolean lastWasVisible;

    public Setting(String name, String description, T defaultValue, Consumer<T> onChanged, Consumer<Setting<T>> onModuleActivated, IVisible visible) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.defaultValue = defaultValue;
        this.onChanged = onChanged;
        this.onModuleActivated = onModuleActivated;
        this.visible = visible;
        this.resetImpl();
    }

    @Override
    public T get() {
        return this.value;
    }

    public boolean set(T value) {
        if (!this.isValueValid(value)) {
            return false;
        }
        this.value = value;
        this.onChanged();
        return true;
    }

    protected void resetImpl() {
        this.value = this.defaultValue;
    }

    public void reset() {
        this.resetImpl();
        this.onChanged();
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public boolean parse(String str) {
        T newValue = this.parseImpl(str);
        if (newValue != null && this.isValueValid(newValue)) {
            this.value = newValue;
            this.onChanged();
        }
        return newValue != null;
    }

    public boolean wasChanged() {
        return !Objects.equals(this.value, this.defaultValue);
    }

    public void onChanged() {
        if (this.onChanged != null) {
            this.onChanged.accept(this.value);
        }
    }

    public void onActivated() {
        if (this.onModuleActivated != null) {
            this.onModuleActivated.accept(this);
        }
    }

    public boolean isVisible() {
        return this.visible == null || this.visible.isVisible();
    }

    protected abstract T parseImpl(String var1);

    protected abstract boolean isValueValid(T var1);

    public Iterable<Identifier> getIdentifierSuggestions() {
        return null;
    }

    public List<String> getSuggestions() {
        return NO_SUGGESTIONS;
    }

    protected abstract NbtCompound save(NbtCompound var1);

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", this.name);
        this.save(tag);
        return tag;
    }

    protected abstract T load(NbtCompound var1);

    @Override
    public T fromTag(NbtCompound tag) {
        T value = this.load(tag);
        this.onChanged();
        return value;
    }

    public String toString() {
        return this.value.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Setting setting = (Setting)o;
        return Objects.equals(this.name, setting.name);
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Nullable
    public static <T> T parseId(Registry<T> registry, String name) {
        Identifier id = (name = name.trim()).contains(":") ? Identifier.of((String)name) : Identifier.of((String)"minecraft", (String)name);
        if (registry.containsId(id)) {
            return (T)registry.get(id);
        }
        return null;
    }

    public static abstract class SettingBuilder<B, V, S> {
        protected String name = "undefined";
        protected String description = "";
        protected V defaultValue;
        protected IVisible visible;
        protected Consumer<V> onChanged;
        protected Consumer<Setting<V>> onModuleActivated;

        protected SettingBuilder(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        public B name(String name) {
            this.name = name;
            return (B)this;
        }

        public B description(String description) {
            this.description = description;
            return (B)this;
        }

        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return (B)this;
        }

        public B visible(IVisible visible) {
            this.visible = visible;
            return (B)this;
        }

        public B onChanged(Consumer<V> onChanged) {
            this.onChanged = onChanged;
            return (B)this;
        }

        public B onModuleActivated(Consumer<Setting<V>> onModuleActivated) {
            this.onModuleActivated = onModuleActivated;
            return (B)this;
        }

        public abstract S build();
    }
}

