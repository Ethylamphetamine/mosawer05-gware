/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets.containers;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;

public class WVerticalList
extends WContainer {
    public double spacing = 3.0;
    protected double widthRemove;

    protected double spacing() {
        return this.theme.scale(this.spacing);
    }

    @Override
    protected void onCalculateSize() {
        this.width = 0.0;
        this.height = 0.0;
        for (int i = 0; i < this.cells.size(); ++i) {
            Cell cell = (Cell)this.cells.get(i);
            if (i > 0) {
                this.height += this.spacing();
            }
            this.width = Math.max(this.width, cell.padLeft() + ((WWidget)cell.widget()).width + cell.padRight());
            this.height += cell.padTop() + ((WWidget)cell.widget()).height + cell.padBottom();
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        double y = this.y;
        for (int i = 0; i < this.cells.size(); ++i) {
            Cell cell = (Cell)this.cells.get(i);
            if (i > 0) {
                y += this.spacing();
            }
            cell.x = this.x + cell.padLeft();
            cell.y = y += cell.padTop();
            cell.width = this.width - this.widthRemove - cell.padLeft() - cell.padRight();
            cell.height = ((WWidget)cell.widget()).height;
            cell.alignWidget();
            y += cell.height + cell.padBottom();
        }
    }
}

