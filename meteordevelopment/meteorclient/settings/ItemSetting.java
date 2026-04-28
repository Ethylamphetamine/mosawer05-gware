/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemSetting
extends Setting<Item> {
    public final Predicate<Item> filter;

    public ItemSetting(String name, String description, Item defaultValue, Consumer<Item> onChanged, Consumer<Setting<Item>> onModuleActivated, IVisible visible, Predicate<Item> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    protected Item parseImpl(String str) {
        return (Item)ItemSetting.parseId(Registries.ITEM, str);
    }

    @Override
    protected boolean isValueValid(Item value) {
        return this.filter == null || this.filter.test(value);
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.ITEM.getIds();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", Registries.ITEM.getId((Object)((Item)this.get())).toString());
        return tag;
    }

    @Override
    public Item load(NbtCompound tag) {
        this.value = Registries.ITEM.get(Identifier.of((String)tag.getString("value")));
        if (this.filter != null && !this.filter.test((Item)this.value)) {
            for (Item item : Registries.ITEM) {
                if (!this.filter.test(item)) continue;
                this.value = item;
                break;
            }
        }
        return (Item)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Item, ItemSetting> {
        private Predicate<Item> filter;

        public Builder() {
            super(null);
        }

        public Builder filter(Predicate<Item> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ItemSetting build() {
            return new ItemSetting(this.name, this.description, (Item)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}

