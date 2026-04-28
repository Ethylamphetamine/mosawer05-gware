/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

public class WGonbleWareSection
extends WSection {
    public WGonbleWareSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WSection.WHeader createHeader() {
        return new WMeteorHeader(this.title);
    }

    protected class WMeteorHeader
    extends WSection.WHeader {
        private WTriangle triangle;

        public WMeteorHeader(String title) {
            super(WGonbleWareSection.this, title);
        }

        @Override
        public void init() {
            this.add(this.theme.horizontalSeparator(this.title)).expandX();
            if (WGonbleWareSection.this.headerWidget != null) {
                this.add(WGonbleWareSection.this.headerWidget);
            }
            this.triangle = new WHeaderTriangle();
            this.triangle.theme = this.theme;
            this.triangle.action = () -> this.onClick();
            this.add(this.triangle);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            this.triangle.rotation = (1.0 - WGonbleWareSection.this.animProgress) * -90.0;
        }
    }

    protected static class WHeaderTriangle
    extends WTriangle
    implements GonbleWareWidget {
        protected WHeaderTriangle() {
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(this.x, this.y, this.width, this.height, this.rotation, GuiRenderer.TRIANGLE, this.theme().textColor.get());
        }
    }
}

