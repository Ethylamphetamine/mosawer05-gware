/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.gui.widgets.containers;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.math.MathHelper;

public abstract class WWindow
extends WVerticalList {
    public double padding = 8.0;
    public Consumer<WContainer> beforeHeaderInit;
    public String id;
    public final WWidget icon;
    protected final String title;
    protected WHeader header;
    public WView view;
    protected boolean dragging;
    protected boolean expanded = true;
    protected boolean dragged;
    protected double animProgress = 1.0;
    protected boolean moved = false;
    protected double movedX;
    protected double movedY;
    private boolean propagateEventsExpanded;

    public WWindow(WWidget icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    @Override
    public void init() {
        this.header = this.header(this.icon);
        this.header.theme = this.theme;
        super.add(this.header).expandWidgetX().widget();
        this.view = super.add(this.theme.view()).expandX().pad(this.padding).widget();
        if (this.id != null) {
            this.expanded = this.theme.getWindowConfig((String)this.id).expanded;
            this.animProgress = this.expanded ? 1.0 : 0.0;
        }
    }

    protected abstract WHeader header(WWidget var1);

    @Override
    public <T extends WWidget> Cell<T> add(T widget) {
        return this.view.add(widget);
    }

    @Override
    public void clear() {
        this.view.clear();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if (this.id != null) {
            WindowConfig config = this.theme.getWindowConfig(this.id);
            config.expanded = expanded;
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        if (this.id != null) {
            WindowConfig config = this.theme.getWindowConfig(this.id);
            if (config.x != -1.0) {
                this.x = config.x;
                if (this.x + this.width > (double)Utils.getWindowWidth()) {
                    this.x = (double)Utils.getWindowWidth() - this.width;
                }
            }
            if (config.y != -1.0) {
                this.y = config.y;
                if (this.y + this.height > (double)Utils.getWindowHeight()) {
                    this.y = (double)Utils.getWindowHeight() - this.height;
                }
            }
        }
        super.onCalculateWidgetPositions();
        if (this.moved) {
            this.move(this.movedX - this.x, this.movedY - this.y);
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean scissor;
        if (!this.visible) {
            return true;
        }
        boolean bl = scissor = this.animProgress != 0.0 && this.animProgress != 1.0 || this.expanded && this.animProgress != 1.0;
        if (scissor) {
            renderer.scissorStart(this.x, this.y, this.width, (this.height - this.header.height) * this.animProgress + this.header.height);
        }
        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);
        if (scissor) {
            renderer.scissorEnd();
        }
        return toReturn;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0 || widget instanceof WHeader) {
            widget.render(renderer, mouseX, mouseY, delta);
        }
        this.propagateEventsExpanded = this.expanded;
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return widget instanceof WHeader || this.propagateEventsExpanded;
    }

    protected abstract class WHeader
    extends WContainer {
        protected final WWidget icon;
        protected WTriangle triangle;
        protected WHorizontalList list;

        public WHeader(WWidget icon) {
            this.icon = icon;
        }

        @Override
        public void init() {
            if (this.icon != null) {
                this.createList();
                this.add(this.icon).centerY();
            }
            if (WWindow.this.beforeHeaderInit != null) {
                this.createList();
                WWindow.this.beforeHeaderInit.accept(this);
            }
            this.add(this.theme.label(WWindow.this.title, true)).expandCellX().center().pad(4.0);
            this.triangle = this.add(this.theme.triangle()).pad(4.0).right().centerY().widget();
            this.triangle.action = () -> WWindow.this.setExpanded(!WWindow.this.expanded);
        }

        protected void createList() {
            this.list = this.add(this.theme.horizontalList()).expandX().widget();
            this.list.spacing = 0.0;
        }

        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (this.list != null) {
                return this.list.add(widget);
            }
            return super.add(widget);
        }

        @Override
        protected void onCalculateSize() {
            this.width = 0.0;
            this.height = 0.0;
            for (Cell cell : this.cells) {
                double w = cell.padLeft() + ((WWidget)cell.widget()).width + cell.padRight();
                if (cell.widget() instanceof WTriangle) {
                    w *= 2.0;
                }
                this.width += w;
                this.height = Math.max(this.height, cell.padTop() + ((WWidget)cell.widget()).height + cell.padBottom());
            }
        }

        @Override
        public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
            if (this.mouseOver && !used) {
                if (button == 1) {
                    WWindow.this.setExpanded(!WWindow.this.expanded);
                } else {
                    WWindow.this.dragging = true;
                    WWindow.this.dragged = false;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onMouseReleased(double mouseX, double mouseY, int button) {
            if (WWindow.this.dragging) {
                WWindow.this.dragging = false;
                if (!WWindow.this.dragged) {
                    WWindow.this.setExpanded(!WWindow.this.expanded);
                }
            }
            return false;
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (WWindow.this.dragging) {
                WWindow.this.move(mouseX - lastMouseX, mouseY - lastMouseY);
                WWindow.this.moved = true;
                WWindow.this.movedX = this.x;
                WWindow.this.movedY = this.y;
                if (WWindow.this.id != null) {
                    WindowConfig config = this.theme.getWindowConfig(WWindow.this.id);
                    config.x = this.x;
                    config.y = this.y;
                }
                WWindow.this.dragged = true;
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            WWindow.this.animProgress = WWindow.this.animProgress + (double)(WWindow.this.expanded ? 1 : -1) * delta * 14.0;
            WWindow.this.animProgress = MathHelper.clamp((double)WWindow.this.animProgress, (double)0.0, (double)1.0);
            this.triangle.rotation = (1.0 - WWindow.this.animProgress) * -90.0;
            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}

