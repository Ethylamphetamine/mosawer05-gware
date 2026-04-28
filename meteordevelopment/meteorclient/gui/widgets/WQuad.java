/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public abstract class WQuad
extends WWidget {
    public Color color;

    public WQuad(Color color) {
        this.color = color;
    }

    @Override
    protected void onCalculateSize() {
        double s;
        this.width = s = this.theme.scale(32.0);
        this.height = s;
    }
}

