/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.WWidget;

public abstract class WHorizontalSeparator
extends WWidget {
    protected String text;
    protected double textWidth;

    public WHorizontalSeparator(String text) {
        this.text = text;
    }

    @Override
    protected void onCalculateSize() {
        if (this.text != null) {
            this.textWidth = this.theme.textWidth(this.text);
        }
        this.width = 1.0;
        this.height = this.text != null ? this.theme.textHeight() : this.theme.scale(3.0);
    }
}

