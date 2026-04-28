/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;

public abstract class WLabel
extends WPressable {
    public Color color;
    protected String text;
    protected boolean title;

    public WLabel(String text, boolean title) {
        this.text = text;
        this.title = title;
    }

    @Override
    protected void onCalculateSize() {
        this.width = this.theme.textWidth(this.text, this.text.length(), this.title);
        this.height = this.theme.textHeight(this.title);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (this.action != null) {
            return super.onMouseClicked(mouseX, mouseY, button, used);
        }
        return false;
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (this.action != null) {
            return super.onMouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    public void set(String text) {
        if ((double)Math.round(this.theme.textWidth(text, text.length(), this.title)) != this.width) {
            this.invalidate();
        }
        this.text = text;
    }

    public String get() {
        return this.text;
    }

    public WLabel color(Color color) {
        this.color = color;
        return this;
    }
}

