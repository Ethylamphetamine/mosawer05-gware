/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Mouse
 */
package meteordevelopment.meteorclient.gui.widgets.containers;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.Mouse;

public abstract class WContainer
extends WWidget {
    public final List<Cell<?>> cells = new ArrayList();

    public <T extends WWidget> Cell<T> add(T widget) {
        widget.parent = this;
        widget.theme = this.theme;
        Cell<T> cell = new Cell<T>(widget).centerY();
        this.cells.add(cell);
        widget.init();
        this.invalidate();
        return cell;
    }

    public void clear() {
        if (!this.cells.isEmpty()) {
            this.cells.clear();
            this.invalidate();
        }
    }

    public void remove(Cell<?> cell) {
        if (this.cells.remove(cell)) {
            this.invalidate();
        }
    }

    @Override
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);
        for (Cell<?> cell : this.cells) {
            cell.move(deltaX, deltaY);
        }
    }

    public void moveCells(double deltaX, double deltaY) {
        for (Cell<?> cell : this.cells) {
            cell.move(deltaX, deltaY);
            Mouse mouse = MeteorClient.mc.mouse;
            ((WWidget)cell.widget()).mouseMoved(mouse.getX(), mouse.getY(), mouse.getX(), mouse.getY());
        }
    }

    @Override
    public void calculateSize() {
        for (Cell<?> cell : this.cells) {
            ((WWidget)cell.widget()).calculateSize();
        }
        super.calculateSize();
    }

    @Override
    protected void onCalculateSize() {
        this.width = 0.0;
        this.height = 0.0;
        for (Cell<?> cell : this.cells) {
            this.width = Math.max(this.width, cell.padLeft() + ((WWidget)cell.widget()).width + cell.padRight());
            this.height = Math.max(this.height, cell.padTop() + ((WWidget)cell.widget()).height + cell.padBottom());
        }
    }

    @Override
    public void calculateWidgetPositions() {
        super.calculateWidgetPositions();
        for (Cell<?> cell : this.cells) {
            ((WWidget)cell.widget()).calculateWidgetPositions();
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        for (Cell<?> cell : this.cells) {
            cell.x = this.x + cell.padLeft();
            cell.y = this.y + cell.padTop();
            cell.width = this.width - cell.padLeft() - cell.padRight();
            cell.height = this.height - cell.padTop() - cell.padBottom();
            cell.alignWidget();
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (super.render(renderer, mouseX, mouseY, delta)) {
            return true;
        }
        for (Cell<?> cell : this.cells) {
            double y = ((WWidget)cell.widget()).y;
            if (y > (double)Utils.getWindowHeight()) break;
            if (!(y + ((WWidget)cell.widget()).height > 0.0)) continue;
            this.renderWidget((WWidget)cell.widget(), renderer, mouseX, mouseY, delta);
        }
        return false;
    }

    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        widget.render(renderer, mouseX, mouseY, delta);
    }

    protected boolean propagateEvents(WWidget widget) {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean used) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget()) || !((WWidget)cell.widget()).mouseClicked(mouseX, mouseY, button, used)) continue;
                used = true;
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        return super.mouseClicked(mouseX, mouseY, button, used) || used;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget()) || !((WWidget)cell.widget()).mouseReleased(mouseX, mouseY, button)) continue;
                return true;
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget())) continue;
                ((WWidget)cell.widget()).mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        super.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
    }

    @Override
    public boolean mouseScrolled(double amount) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget()) || !((WWidget)cell.widget()).mouseScrolled(amount)) continue;
                return true;
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        return super.mouseScrolled(amount);
    }

    @Override
    public boolean keyPressed(int key, int modifiers) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget()) || !((WWidget)cell.widget()).keyPressed(key, modifiers)) continue;
                return true;
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        return this.onKeyPressed(key, modifiers);
    }

    @Override
    public boolean keyRepeated(int key, int modifiers) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget()) || !((WWidget)cell.widget()).keyRepeated(key, modifiers)) continue;
                return true;
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        return this.onKeyRepeated(key, modifiers);
    }

    @Override
    public boolean charTyped(char c) {
        try {
            for (Cell<?> cell : this.cells) {
                if (!this.propagateEvents((WWidget)cell.widget()) || !((WWidget)cell.widget()).charTyped(c)) continue;
                return true;
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        return super.charTyped(c);
    }
}

