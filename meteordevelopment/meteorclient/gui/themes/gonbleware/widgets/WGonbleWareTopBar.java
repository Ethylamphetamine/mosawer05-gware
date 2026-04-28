/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets;

import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WGonbleWareTopBar
extends WTopBar
implements GonbleWareWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return this.theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return this.theme().textColor.get();
    }
}

