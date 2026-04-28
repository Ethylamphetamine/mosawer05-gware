/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets.pressable;

import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;

public abstract class WTriangle
extends WPressable {
    public double rotation;

    @Override
    protected void onCalculateSize() {
        double s;
        this.width = s = this.theme.textHeight();
        this.height = s;
    }
}

