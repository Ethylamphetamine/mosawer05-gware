/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class ColorListSetting
extends Setting<List<SettingColor>> {
    public ColorListSetting(String name, String description, List<SettingColor> defaultValue, Consumer<List<SettingColor>> onChanged, Consumer<Setting<List<SettingColor>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected List<SettingColor> parseImpl(String str) {
        ArrayList<SettingColor> colors = new ArrayList<SettingColor>();
        try {
            String[] colorsStr;
            for (String colorStr : colorsStr = str.replaceAll("\\s+", "").split(";")) {
                String[] strs = colorStr.split(",");
                colors.add(new SettingColor(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]), Integer.parseInt(strs[2]), Integer.parseInt(strs[3])));
            }
        }
        catch (IndexOutOfBoundsException | NumberFormatException runtimeException) {
            // empty catch block
        }
        return colors;
    }

    @Override
    protected boolean isValueValid(List<SettingColor> value) {
        return true;
    }

    @Override
    protected void resetImpl() {
        this.value = new ArrayList(((List)this.defaultValue).size());
        for (SettingColor settingColor : (List)this.defaultValue) {
            ((List)this.value).add(new SettingColor(settingColor));
        }
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.put("value", (NbtElement)NbtUtils.listToTag((Iterable)this.get()));
        return tag;
    }

    @Override
    protected List<SettingColor> load(NbtCompound tag) {
        ((List)this.get()).clear();
        for (NbtElement e : tag.getList("value", 10)) {
            ((List)this.get()).add(new SettingColor().fromTag((NbtCompound)e));
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<SettingColor>, ColorListSetting> {
        public Builder() {
            super(new ArrayList());
        }

        @Override
        public ColorListSetting build() {
            return new ColorListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

