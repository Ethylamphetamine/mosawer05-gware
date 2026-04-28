/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.registry.Registry
 *  net.minecraft.util.Pair
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public abstract class RegistryListSettingScreen<T>
extends WindowScreen {
    protected final Setting<?> setting;
    protected final Collection<T> collection;
    private final Registry<T> registry;
    private WTextBox filter;
    private String filterText = "";
    private WTable table;

    public RegistryListSettingScreen(GuiTheme theme, String title, Setting<?> setting, Collection<T> collection, Registry<T> registry) {
        super(theme, title);
        this.registry = registry;
        this.setting = setting;
        this.collection = collection;
    }

    @Override
    public void initWidgets() {
        this.filter = this.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
        this.filter.setFocused(true);
        this.filter.action = () -> {
            this.filterText = this.filter.get().trim();
            this.table.clear();
            this.initWidgets(this.registry);
        };
        this.table = this.add(this.theme.table()).expandX().widget();
        this.initWidgets(this.registry);
    }

    private void initWidgets(Registry<T> registry) {
        WTable left = this.abc(pairs -> registry.forEach(t -> {
            if (this.skipValue(t) || this.collection.contains(t)) {
                return;
            }
            int words = Utils.searchInWords(this.getValueName(t), this.filterText);
            int diff = Utils.searchLevenshteinDefault(this.getValueName(t), this.filterText, false);
            if (words > 0 || diff <= this.getValueName(t).length() / 2) {
                pairs.add(new Pair(t, (Object)(-diff)));
            }
        }), true, t -> {
            this.addValue(registry, t);
            Object v = this.getAdditionalValue(t);
            if (v != null) {
                this.addValue(registry, v);
            }
        });
        if (!left.cells.isEmpty()) {
            this.table.add(this.theme.verticalSeparator()).expandWidgetY();
        }
        this.abc(pairs -> {
            for (T value : this.collection) {
                if (this.skipValue(value)) continue;
                int words = Utils.searchInWords(this.getValueName(value), this.filterText);
                int diff = Utils.searchLevenshteinDefault(this.getValueName(value), this.filterText, false);
                if (words <= 0 && diff > this.getValueName(value).length() / 2) continue;
                pairs.add(new Pair(value, (Object)(-diff)));
            }
        }, false, t -> {
            this.removeValue(registry, t);
            Object v = this.getAdditionalValue(t);
            if (v != null) {
                this.removeValue(registry, v);
            }
        });
    }

    private void addValue(Registry<T> registry, T value) {
        if (!this.collection.contains(value)) {
            this.collection.add(value);
            this.setting.onChanged();
            this.table.clear();
            this.initWidgets(registry);
        }
    }

    private void removeValue(Registry<T> registry, T value) {
        if (this.collection.remove(value)) {
            this.setting.onChanged();
            this.table.clear();
            this.initWidgets(registry);
        }
    }

    private WTable abc(Consumer<List<Pair<T, Integer>>> addValues, boolean isLeft, Consumer<T> buttonAction) {
        Cell<WTable> cell = this.table.add(this.theme.table()).top();
        WTable table = cell.widget();
        Consumer<Object> forEach = t -> {
            if (!this.includeValue(t)) {
                return;
            }
            table.add(this.getValueWidget(t));
            WPressable button = table.add(isLeft ? this.theme.plus() : this.theme.minus()).expandCellX().right().widget();
            button.action = () -> buttonAction.accept(t);
            table.row();
        };
        ArrayList<Pair> values = new ArrayList<Pair>();
        addValues.accept(values);
        if (!this.filterText.isEmpty()) {
            values.sort(Comparator.comparingInt(value -> -((Integer)value.getRight()).intValue()));
        }
        for (Pair pair : values) {
            forEach.accept(pair.getLeft());
        }
        if (!table.cells.isEmpty()) {
            cell.expandX();
        }
        return table;
    }

    protected boolean includeValue(T value) {
        return true;
    }

    protected abstract WWidget getValueWidget(T var1);

    protected abstract String getValueName(T var1);

    protected boolean skipValue(T value) {
        return false;
    }

    protected T getAdditionalValue(T value) {
        return null;
    }
}

