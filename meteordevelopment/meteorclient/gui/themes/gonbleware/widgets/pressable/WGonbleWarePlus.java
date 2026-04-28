/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareGuiTheme;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;

public class WGonbleWarePlus
extends WPlus
implements GonbleWareWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        GonbleWareGuiTheme theme = this.theme();
        double pad = this.pad();
        double s = theme.scale(3.0);
        this.renderBackground(renderer, this, this.pressed, this.mouseOver);
        renderer.quad(this.x + pad, this.y + this.height / 2.0 - s / 2.0, this.width - pad * 2.0, s, theme.plusColor.get());
        renderer.quad(this.x + this.width / 2.0 - s / 2.0, this.y + pad, s, this.height - pad * 2.0, theme.plusColor.get());
    }
}

