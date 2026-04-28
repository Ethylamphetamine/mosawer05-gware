/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets.pressable;

import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;

public abstract class WMinus
extends WPressable {
    @Override
    protected void onCalculateSize() {
        double pad = this.pad();
        double s = this.theme.textHeight();
        this.width = pad + s + pad;
        this.height = pad + s + pad;
    }
}

