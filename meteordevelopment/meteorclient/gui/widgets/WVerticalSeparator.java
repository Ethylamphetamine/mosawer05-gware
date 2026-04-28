/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.WWidget;

public class WVerticalSeparator
extends WWidget {
    @Override
    protected void onCalculateSize() {
        this.width = this.theme.scale(3.0);
        this.height = 1.0;
    }
}

