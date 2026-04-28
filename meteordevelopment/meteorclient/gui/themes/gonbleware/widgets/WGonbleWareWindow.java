/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

public class WGonbleWareWindow
extends WWindow
implements GonbleWareWidget {
    public WGonbleWareWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WWindow.WHeader header(WWidget icon) {
        return new WGonbleWareHeader(icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0) {
            renderer.quad(this.x, this.y + this.header.height, this.width, this.height - this.header.height, this.theme().backgroundColor.get());
        }
    }

    private class WGonbleWareHeader
    extends WWindow.WHeader {
        public WGonbleWareHeader(WWidget icon) {
            super(WGonbleWareWindow.this, icon);
        }

        @Override
        public void init() {
            if (this.icon != null) {
                super.createList();
                this.add(this.icon).centerY();
            }
            if (WGonbleWareWindow.this.beforeHeaderInit != null) {
                this.createList();
                WGonbleWareWindow.this.beforeHeaderInit.accept(this);
            }
            this.add(this.theme.label(WGonbleWareWindow.this.title, true)).expandCellX().centerY().pad(4.0);
            this.triangle = this.add(this.theme.triangle()).pad(4.0).right().centerY().widget();
            this.triangle.action = () -> WGonbleWareWindow.this.setExpanded(!WGonbleWareWindow.this.expanded);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quad(this, WGonbleWareWindow.this.theme().accentColor.get());
        }
    }
}

