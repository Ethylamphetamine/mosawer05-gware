/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.gui.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.item.Items;

public class ModulesScreen
extends TabScreen {
    private WCategoryController controller;
    private final Map<Module, WWidget> moduleWidges = new HashMap<Module, WWidget>();
    private final Map<Category, Integer> searchCategoryBuckets = new HashMap<Category, Integer>();

    public ModulesScreen(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
    }

    @Override
    public void initWidgets() {
        this.controller = this.add(new WCategoryController()).widget();
        WVerticalList help = this.add(this.theme.verticalList()).pad(4.0).bottom().widget();
        help.add(this.theme.label("Left click - Toggle module"));
        help.add(this.theme.label("Right click - Open module settings"));
    }

    @Override
    protected void init() {
        super.init();
        this.controller.refresh();
    }

    protected WWindow createCategory(WContainer c, Category category) {
        WWindow w = this.theme.window(category.name);
        w.id = category.name;
        w.padding = 0.0;
        w.spacing = 0.0;
        if (this.theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> wContainer.add(this.theme.item(category.icon)).pad(2.0);
        }
        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0.0;
        for (Module module : Modules.get().getGroup(category)) {
            if (module.hidden) continue;
            WWidget wid = this.theme.module(module);
            w.add(wid).expandX();
            this.moduleWidges.put(module, wid);
        }
        return w;
    }

    public void searchSetHighlight(String text, Map<Module, Integer> modules, Module module, WWidget widget) {
        if (text.isEmpty()) {
            widget.highlight = false;
            widget.deactivate = false;
            return;
        }
        if (modules.containsKey(module)) {
            int score = modules.get(module);
            if (score < 10) {
                widget.highlight = true;
                widget.deactivate = false;
            } else {
                widget.highlight = false;
                widget.deactivate = true;
            }
        } else {
            widget.highlight = false;
            widget.deactivate = true;
        }
    }

    protected void runSearchW(String text) {
        this.searchCategoryBuckets.clear();
        Map<Module, Integer> modules = Modules.get().searchTitles(text);
        if (modules.isEmpty()) {
            return;
        }
        for (Map.Entry<Module, WWidget> moduleWidget : this.moduleWidges.entrySet()) {
            if (modules.isEmpty()) continue;
            this.searchSetHighlight(text, modules, moduleWidget.getKey(), moduleWidget.getValue());
        }
    }

    protected WWindow createSearch(WContainer c) {
        WWindow w = this.theme.window("Search");
        w.id = "search";
        if (this.theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> wContainer.add(this.theme.item(Items.COMPASS.getDefaultStack())).pad(2.0);
        }
        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.maxHeight -= 20.0;
        WTextBox text = w.add(this.theme.textBox("")).minWidth(140.0).expandX().widget();
        text.setFocused(true);
        text.action = () -> this.runSearchW(text.get());
        return w;
    }

    protected Cell<WWindow> createFavorites(WContainer c) {
        boolean hasFavorites = Modules.get().getAll().stream().anyMatch(module -> module.favorite);
        if (!hasFavorites) {
            return null;
        }
        WWindow w = this.theme.window("Favorites");
        w.id = "favorites";
        w.padding = 0.0;
        w.spacing = 0.0;
        if (this.theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> wContainer.add(this.theme.item(Items.NETHER_STAR.getDefaultStack())).pad(2.0);
        }
        Cell<WWindow> cell = c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0.0;
        this.createFavoritesW(w);
        return cell;
    }

    protected boolean createFavoritesW(WWindow w) {
        ArrayList<Module> modules = new ArrayList<Module>();
        for (Module module : Modules.get().getAll()) {
            if (!module.favorite || module.hidden) continue;
            modules.add(module);
        }
        modules.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name));
        for (Module module : modules) {
            w.add(this.theme.module(module)).expandX();
        }
        return !modules.isEmpty();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Modules.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Modules.get());
    }

    @Override
    public void reload() {
    }

    protected class WCategoryController
    extends WContainer {
        public final List<WWindow> windows = new ArrayList<WWindow>();
        private Cell<WWindow> favorites;

        protected WCategoryController() {
        }

        @Override
        public void init() {
            for (Category category : Modules.loopCategories()) {
                this.windows.add(ModulesScreen.this.createCategory(this, category));
            }
            this.windows.add(ModulesScreen.this.createSearch(this));
            this.refresh();
        }

        protected void refresh() {
            if (this.favorites == null) {
                this.favorites = ModulesScreen.this.createFavorites(this);
                if (this.favorites != null) {
                    this.windows.add(this.favorites.widget());
                }
            } else {
                this.favorites.widget().clear();
                if (!ModulesScreen.this.createFavoritesW(this.favorites.widget())) {
                    this.remove(this.favorites);
                    this.windows.remove(this.favorites.widget());
                    this.favorites = null;
                }
            }
        }

        @Override
        protected void onCalculateWidgetPositions() {
            double pad = this.theme.scale(4.0);
            double h = this.theme.scale(40.0);
            double x = this.x + pad;
            double y = this.y;
            for (Cell cell : this.cells) {
                double windowWidth = Utils.getWindowWidth();
                double windowHeight = Utils.getWindowHeight();
                if (x + cell.width > windowWidth) {
                    x += pad;
                    y += h;
                }
                if (x > windowWidth && (x = windowWidth / 2.0 - cell.width / 2.0) < 0.0) {
                    x = 0.0;
                }
                if (y > windowHeight && (y = windowHeight / 2.0 - cell.height / 2.0) < 0.0) {
                    y = 0.0;
                }
                cell.x = x;
                cell.y = y;
                cell.width = ((WWidget)cell.widget()).width;
                cell.height = ((WWidget)cell.widget()).height;
                cell.alignWidget();
                x += cell.width + pad;
            }
        }
    }
}

