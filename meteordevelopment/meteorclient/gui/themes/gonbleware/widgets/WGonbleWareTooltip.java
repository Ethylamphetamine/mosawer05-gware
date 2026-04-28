/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.WTooltip;

public class WGonbleWareTooltip
extends WTooltip
implements GonbleWareWidget {
    public WGonbleWareTooltip(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderer.quad(this, this.theme().backgroundColor.get());
    }
}

