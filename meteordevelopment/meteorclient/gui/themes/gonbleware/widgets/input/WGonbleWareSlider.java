/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareGuiTheme;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WGonbleWareSlider
extends WSlider
implements GonbleWareWidget {
    public WGonbleWareSlider(double value, double min, double max) {
        super(value, min, max);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double valueWidth = this.valueWidth();
        this.renderBar(renderer, valueWidth);
        this.renderHandle(renderer, valueWidth);
    }

    private void renderBar(GuiRenderer renderer, double valueWidth) {
        GonbleWareGuiTheme theme = this.theme();
        double s = theme.scale(3.0);
        double handleSize = this.handleSize();
        double x = this.x + handleSize / 2.0;
        double y = this.y + this.height / 2.0 - s / 2.0;
        renderer.quad(x, y, valueWidth, s, theme.sliderLeft.get());
        renderer.quad(x + valueWidth, y, this.width - valueWidth - handleSize, s, theme.sliderRight.get());
    }

    private void renderHandle(GuiRenderer renderer, double valueWidth) {
        GonbleWareGuiTheme theme = this.theme();
        double s = this.handleSize();
        renderer.quad(this.x + valueWidth, this.y, s, s, GuiRenderer.CIRCLE, (Color)theme.sliderHandle.get(this.dragging, this.handleMouseOver));
    }
}

