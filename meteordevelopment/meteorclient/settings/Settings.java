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
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.ColorListSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

public class Settings
implements ISerializable<Settings>,
Iterable<SettingGroup> {
    private SettingGroup defaultGroup;
    public final List<SettingGroup> groups = new ArrayList<SettingGroup>(1);

    public void onActivated() {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                setting.onActivated();
            }
        }
    }

    public Setting<?> get(String name) {
        for (SettingGroup sg : this) {
            for (Setting<?> setting : sg) {
                if (!name.equalsIgnoreCase(setting.name)) continue;
                return setting;
            }
        }
        return null;
    }

    public void reset() {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                setting.reset();
            }
        }
    }

    public SettingGroup getGroup(String name) {
        for (SettingGroup sg : this) {
            if (!sg.name.equals(name)) continue;
            return sg;
        }
        return null;
    }

    public int sizeGroups() {
        return this.groups.size();
    }

    public SettingGroup getDefaultGroup() {
        if (this.defaultGroup == null) {
            this.defaultGroup = this.createGroup("General");
        }
        return this.defaultGroup;
    }

    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        this.groups.add(group);
        return group;
    }

    public SettingGroup createGroup(String name) {
        return this.createGroup(name, true);
    }

    public void registerColorSettings(Module module) {
        for (SettingGroup group : this) {
            for (Setting<SettingColor> setting : group) {
                setting.module = module;
                if (setting instanceof ColorSetting) {
                    RainbowColors.addSetting(setting);
                    continue;
                }
                if (!(setting instanceof ColorListSetting)) continue;
                RainbowColors.addSettingList(setting);
            }
        }
    }

    public void unregisterColorSettings() {
        for (SettingGroup group : this) {
            for (Setting<SettingColor> setting : group) {
                if (setting instanceof ColorSetting) {
                    RainbowColors.removeSetting(setting);
                    continue;
                }
                if (!(setting instanceof ColorListSetting)) continue;
                RainbowColors.removeSettingList(setting);
            }
        }
    }

    public void tick(WContainer settings, GuiTheme theme) {
        for (SettingGroup group : this.groups) {
            for (Setting<?> setting : group) {
                boolean visible = setting.isVisible();
                if (visible != setting.lastWasVisible) {
                    settings.clear();
                    settings.add(theme.settings(this)).expandX();
                }
                setting.lastWasVisible = visible;
            }
        }
    }

    @Override
    @NotNull
    public Iterator<SettingGroup> iterator() {
        return this.groups.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("groups", (NbtElement)NbtUtils.listToTag(this.groups));
        return tag;
    }

    @Override
    public Settings fromTag(NbtCompound tag) {
        NbtList groupsTag = tag.getList("groups", 10);
        for (NbtElement t : groupsTag) {
            NbtCompound groupTag = (NbtCompound)t;
            SettingGroup sg = this.getGroup(groupTag.getString("name"));
            if (sg == null) continue;
            sg.fromTag(groupTag);
        }
        return this;
    }
}

