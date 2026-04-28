/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WGonbleWareMultiLabel
extends WMultiLabel
implements GonbleWareWidget {
    public WGonbleWareMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = this.theme.textHeight(this.title);
        Color defaultColor = this.theme().textColor.get();
        for (int i = 0; i < this.lines.size(); ++i) {
            renderer.text((String)this.lines.get(i), this.x, this.y + h * (double)i, this.color != null ? this.color : defaultColor, false);
        }
    }
}

