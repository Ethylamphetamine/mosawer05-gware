/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 */
package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class ColorSetting
extends Setting<SettingColor> {
    private static final List<String> SUGGESTIONS = List.of("0 0 0 255", "225 25 25 255", "25 225 25 255", "25 25 225 255", "255 255 255 255");

    public ColorSetting(String name, String description, SettingColor defaultValue, Consumer<SettingColor> onChanged, Consumer<Setting<SettingColor>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected SettingColor parseImpl(String str) {
        try {
            String[] strs = str.split(" ");
            return new SettingColor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3]));
        }
        catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void resetImpl() {
        if (this.value == null) {
            this.value = new SettingColor((SettingColor)this.defaultValue);
        } else {
            ((SettingColor)this.value).set((Color)this.defaultValue);
        }
    }

    @Override
    protected boolean isValueValid(SettingColor value) {
        value.validate();
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return SUGGESTIONS;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.put("value", (NbtElement)((SettingColor)this.get()).toTag());
        return tag;
    }

    @Override
    public SettingColor load(NbtCompound tag) {
        ((SettingColor)this.get()).fromTag(tag.getCompound("value"));
        return (SettingColor)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, SettingColor, ColorSetting> {
        public Builder() {
            super(new SettingColor());
        }

        @Override
        public ColorSetting build() {
            return new ColorSetting(this.name, this.description, (SettingColor)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }

        @Override
        public Builder defaultValue(SettingColor defaultValue) {
            ((SettingColor)this.defaultValue).set(defaultValue);
            return this;
        }

        @Override
        public Builder defaultValue(Color defaultValue) {
            ((SettingColor)this.defaultValue).set(defaultValue);
            return this;
        }
    }
}

