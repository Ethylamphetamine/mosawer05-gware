/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

public class SettingGroup
implements ISerializable<SettingGroup>,
Iterable<Setting<?>> {
    public final String name;
    public boolean sectionExpanded;
    final List<Setting<?>> settings = new ArrayList(1);

    SettingGroup(String name, boolean sectionExpanded) {
        this.name = name;
        this.sectionExpanded = sectionExpanded;
    }

    public Setting<?> get(String name) {
        for (Setting<?> setting : this) {
            if (!setting.name.equals(name)) continue;
            return setting;
        }
        return null;
    }

    public <T> Setting<T> add(Setting<T> setting) {
        this.settings.add(setting);
        return setting;
    }

    public Setting<?> getByIndex(int index) {
        return this.settings.get(index);
    }

    @Override
    @NotNull
    public Iterator<Setting<?>> iterator() {
        return this.settings.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", this.name);
        tag.putBoolean("sectionExpanded", this.sectionExpanded);
        NbtList settingsTag = new NbtList();
        for (Setting<?> setting : this) {
            if (!setting.wasChanged()) continue;
            settingsTag.add((Object)setting.toTag());
        }
        tag.put("settings", (NbtElement)settingsTag);
        return tag;
    }

    @Override
    public SettingGroup fromTag(NbtCompound tag) {
        this.sectionExpanded = tag.getBoolean("sectionExpanded");
        NbtList settingsTag = tag.getList("settings", 10);
        for (NbtElement t : settingsTag) {
            NbtCompound settingTag = (NbtCompound)t;
            Setting<?> setting = this.get(settingTag.getString("name"));
            if (setting == null) continue;
            setting.fromTag(settingTag);
        }
        return this;
    }
}

