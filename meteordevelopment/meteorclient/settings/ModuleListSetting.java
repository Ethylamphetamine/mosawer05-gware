/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class ModuleListSetting
extends Setting<List<Module>> {
    private static List<String> suggestions;

    public ModuleListSetting(String name, String description, List<Module> defaultValue, Consumer<List<Module>> onChanged, Consumer<Setting<List<Module>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<Module> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<Module> modules = new ArrayList<Module>(values.length);
        try {
            for (String value : values) {
                Module module = Modules.get().get(value.trim());
                if (module == null) continue;
                modules.add(module);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return modules;
    }

    @Override
    protected boolean isValueValid(List<Module> value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList<String>(Modules.get().getAll().size());
            for (Module module : Modules.get().getAll()) {
                suggestions.add(module.name);
            }
        }
        return suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList modulesTag = new NbtList();
        for (Module module : (List)this.get()) {
            modulesTag.add((Object)NbtString.of((String)module.name));
        }
        tag.put("modules", (NbtElement)modulesTag);
        return tag;
    }

    @Override
    public List<Module> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("modules", 8);
        for (NbtElement tagI : valueTag) {
            Module module = Modules.get().get(tagI.asString());
            if (module == null) continue;
            ((List)this.get()).add(module);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<Module>, ModuleListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        @SafeVarargs
        public final Builder defaultValue(Class<? extends Module> ... defaults) {
            ArrayList<Module> modules = new ArrayList<Module>();
            for (Class<? extends Module> klass : defaults) {
                if (Modules.get().get(klass) == null) continue;
                modules.add(Modules.get().get(klass));
            }
            return (Builder)this.defaultValue(modules);
        }

        @Override
        public ModuleListSetting build() {
            return new ModuleListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

