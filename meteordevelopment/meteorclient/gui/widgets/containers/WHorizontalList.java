/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets.containers;

import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;

public class WHorizontalList
extends WContainer {
    public double spacing = 3.0;
    protected double calculatedWidth;
    protected int fillXCount;

    protected double spacing() {
        return this.theme.scale(this.spacing);
    }

    @Override
    protected void onCalculateSize() {
        this.width = 0.0;
        this.height = 0.0;
        this.fillXCount = 0;
        for (int i = 0; i < this.cells.size(); ++i) {
            Cell cell = (Cell)this.cells.get(i);
            if (i > 0) {
                this.width += this.spacing();
            }
            this.width += cell.padLeft() + ((WWidget)cell.widget()).width + cell.padRight();
            this.height = Math.max(this.height, cell.padTop() + ((WWidget)cell.widget()).height + cell.padBottom());
            if (!cell.expandCellX) continue;
            ++this.fillXCount;
        }
        this.calculatedWidth = this.width;
    }

    @Override
    protected void onCalculateWidgetPositions() {
        double x = this.x;
        double fillXWidth = (this.width - this.calculatedWidth) / (double)this.fillXCount;
        for (int i = 0; i < this.cells.size(); ++i) {
            Cell cell = (Cell)this.cells.get(i);
            if (i > 0) {
                x += this.spacing();
            }
            cell.x = x += cell.padLeft();
            cell.y = this.y + cell.padTop();
            cell.width = ((WWidget)cell.widget()).width;
            cell.height = this.height - cell.padTop() - cell.padTop();
            if (cell.expandCellX) {
                cell.width += fillXWidth;
            }
            cell.alignWidget();
            x += cell.width + cell.padRight();
        }
    }
}

