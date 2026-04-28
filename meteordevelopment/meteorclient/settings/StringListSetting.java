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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class StringListSetting
extends Setting<List<String>> {
    public final Class<? extends WTextBox.Renderer> renderer;
    public final CharFilter filter;

    public StringListSetting(String name, String description, List<String> defaultValue, Consumer<List<String>> onChanged, Consumer<Setting<List<String>>> onModuleActivated, IVisible visible, Class<? extends WTextBox.Renderer> renderer, CharFilter filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.renderer = renderer;
        this.filter = filter;
    }

    @Override
    protected List<String> parseImpl(String str) {
        return Arrays.asList(str.split(","));
    }

    @Override
    protected boolean isValueValid(List<String> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (int i = 0; i < ((List)this.value).size(); ++i) {
            valueTag.add(i, (NbtElement)NbtString.of((String)((String)((List)this.get()).get(i))));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public List<String> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            ((List)this.get()).add(tagI.asString());
        }
        return (List)this.get();
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    public static void fillTable(GuiTheme theme, WTable table, StringListSetting setting) {
        table.clear();
        ArrayList strings = new ArrayList((Collection)setting.get());
        CharFilter filter = setting.filter == null ? (text, c) -> true : setting.filter;
        for (int i = 0; i < ((List)setting.get()).size(); ++i) {
            int msgI = i;
            String message = (String)((List)setting.get()).get(i);
            WTextBox textBox = table.add(theme.textBox(message, filter, setting.renderer)).expandX().widget();
            textBox.action = () -> strings.set(msgI, textBox.get());
            textBox.actionOnUnfocused = () -> setting.set(strings);
            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                strings.remove(msgI);
                setting.set(strings);
                StringListSetting.fillTable(theme, table, setting);
            };
            table.row();
        }
        if (!((List)setting.get()).isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }
        WButton add = table.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            strings.add("");
            setting.set(strings);
            StringListSetting.fillTable(theme, table, setting);
        };
        WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();
            StringListSetting.fillTable(theme, table, setting);
        };
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<String>, StringListSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private CharFilter filter;

        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(String ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder filter(CharFilter filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public StringListSetting build() {
            return new StringListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.renderer, this.filter);
        }
    }
}

