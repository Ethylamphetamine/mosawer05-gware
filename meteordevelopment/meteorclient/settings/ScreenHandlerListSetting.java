/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.registry.Registries
 *  net.minecraft.screen.ScreenHandlerType
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ScreenHandlerListSetting
extends Setting<List<ScreenHandlerType<?>>> {
    public ScreenHandlerListSetting(String name, String description, List<ScreenHandlerType<?>> defaultValue, Consumer<List<ScreenHandlerType<?>>> onChanged, Consumer<Setting<List<ScreenHandlerType<?>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<ScreenHandlerType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList handlers = new ArrayList(values.length);
        try {
            for (String value : values) {
                ScreenHandlerType handler = (ScreenHandlerType)ScreenHandlerListSetting.parseId(Registries.SCREEN_HANDLER, value);
                if (handler == null) continue;
                handlers.add(handler);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return handlers;
    }

    @Override
    protected boolean isValueValid(List<ScreenHandlerType<?>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.SCREEN_HANDLER.getIds();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (ScreenHandlerType type : (List)this.get()) {
            Identifier id = Registries.SCREEN_HANDLER.getId((Object)type);
            if (id == null) continue;
            valueTag.add((Object)NbtString.of((String)id.toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public List<ScreenHandlerType<?>> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            ScreenHandlerType type = (ScreenHandlerType)Registries.SCREEN_HANDLER.get(Identifier.of((String)tagI.asString()));
            if (type == null) continue;
            ((List)this.get()).add(type);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<ScreenHandlerType<?>>, ScreenHandlerListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(ScreenHandlerType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public ScreenHandlerListSetting build() {
            return new ScreenHandlerListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

