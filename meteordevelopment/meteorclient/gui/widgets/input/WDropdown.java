/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.gui.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WRoot;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import net.minecraft.util.math.MathHelper;

public abstract class WDropdown<T>
extends WPressable {
    public Runnable action;
    protected T[] values;
    protected T value;
    protected double maxValueWidth;
    protected WDropdownRoot root;
    protected boolean expanded;
    protected double animProgress;

    public WDropdown(T[] values, T value) {
        this.values = values;
        this.set(value);
    }

    @Override
    public void init() {
        this.root = this.createRootWidget();
        this.root.theme = this.theme;
        this.root.spacing = 0.0;
        for (int i = 0; i < this.values.length; ++i) {
            WDropdownValue widget = this.createValueWidget();
            widget.theme = this.theme;
            widget.value = this.values[i];
            Cell<WDropdownValue> cell = this.root.add(widget).padHorizontal(2.0).expandWidgetX();
            if (i < this.values.length - 1) continue;
            cell.padBottom(2.0);
        }
    }

    protected abstract WDropdownRoot createRootWidget();

    protected abstract WDropdownValue createValueWidget();

    @Override
    protected void onCalculateSize() {
        double pad = this.pad();
        this.maxValueWidth = 0.0;
        for (T value : this.values) {
            double valueWidth = this.theme.textWidth(value.toString());
            this.maxValueWidth = Math.max(this.maxValueWidth, valueWidth);
        }
        this.root.calculateSize();
        this.width = pad + this.maxValueWidth + pad + this.theme.textHeight() + pad;
        this.height = pad + this.theme.textHeight() + pad;
        this.root.width = this.width;
    }

    @Override
    protected void onCalculateWidgetPositions() {
        super.onCalculateWidgetPositions();
        this.root.x = this.x;
        this.root.y = this.y + this.height;
        this.root.calculateWidgetPositions();
    }

    @Override
    protected void onPressed(int button) {
        this.expanded = !this.expanded;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);
        this.root.move(deltaX, deltaY);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean render = super.render(renderer, mouseX, mouseY, delta);
        this.animProgress += (double)(this.expanded ? 1 : -1) * delta * 14.0;
        this.animProgress = MathHelper.clamp((double)this.animProgress, (double)0.0, (double)1.0);
        if (!render && this.animProgress > 0.0) {
            renderer.absolutePost(() -> {
                renderer.scissorStart(this.x, this.y + this.height, this.width, this.root.height * this.animProgress);
                this.root.render(renderer, mouseX, mouseY, delta);
                renderer.scissorEnd();
            });
        }
        if (this.expanded && this.root.mouseOver) {
            this.theme.disableHoverColor = true;
        }
        return render;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (!this.mouseOver && !this.root.mouseOver) {
            this.expanded = false;
        }
        if (super.onMouseClicked(mouseX, mouseY, button, used)) {
            used = true;
        }
        if (this.expanded && this.root.mouseClicked(mouseX, mouseY, button, used)) {
            used = true;
        }
        return used;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (super.onMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return this.expanded && this.root.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        if (this.expanded) {
            this.root.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
        }
    }

    @Override
    public boolean onMouseScrolled(double amount) {
        if (super.onMouseScrolled(amount)) {
            return true;
        }
        if (this.expanded) {
            return this.root.mouseScrolled(amount);
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(int key, int mods) {
        if (super.onKeyPressed(key, mods)) {
            return true;
        }
        return this.expanded && this.root.keyPressed(key, mods);
    }

    @Override
    public boolean onKeyRepeated(int key, int mods) {
        if (super.onKeyRepeated(key, mods)) {
            return true;
        }
        return this.expanded && this.root.keyRepeated(key, mods);
    }

    @Override
    public boolean onCharTyped(char c) {
        if (super.onCharTyped(c)) {
            return true;
        }
        return this.expanded && this.root.charTyped(c);
    }

    protected static abstract class WDropdownRoot
    extends WVerticalList
    implements WRoot {
        protected WDropdownRoot() {
        }

        @Override
        public void invalidate() {
        }
    }

    protected abstract class WDropdownValue
    extends WPressable {
        protected T value;

        protected WDropdownValue() {
        }

        @Override
        protected void onPressed(int button) {
            boolean isNew = !WDropdown.this.value.equals(this.value);
            WDropdown.this.value = this.value;
            WDropdown.this.expanded = false;
            if (isNew && WDropdown.this.action != null) {
                WDropdown.this.action.run();
            }
        }
    }
}

