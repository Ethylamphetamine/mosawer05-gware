/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets.input;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareGuiTheme;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class WGbonleWareDropdown<T>
extends WDropdown<T>
implements GonbleWareWidget {
    public WGbonleWareDropdown(T[] values, T value) {
        super(values, value);
    }

    @Override
    protected WDropdown.WDropdownRoot createRootWidget() {
        return new WRoot();
    }

    @Override
    protected WDropdown.WDropdownValue createValueWidget() {
        return new WValue(this);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        GonbleWareGuiTheme theme = this.theme();
        double pad = this.pad();
        double s = theme.textHeight();
        this.renderBackground(renderer, this, this.pressed, this.mouseOver);
        String text = this.get().toString();
        double w = theme.textWidth(text);
        renderer.text(text, this.x + pad + this.maxValueWidth / 2.0 - w / 2.0, this.y + pad, theme.textColor.get(), false);
        renderer.rotatedQuad(this.x + pad + this.maxValueWidth + pad, this.y + pad, s, s, 0.0, GuiRenderer.TRIANGLE, theme.textColor.get());
    }

    private static class WRoot
    extends WDropdown.WDropdownRoot
    implements GonbleWareWidget {
        private WRoot() {
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            GonbleWareGuiTheme theme = this.theme();
            double s = theme.scale(2.0);
            SettingColor c = theme.outlineColor.get();
            renderer.quad(this.x, this.y + this.height - s, this.width, s, c);
            renderer.quad(this.x, this.y, s, this.height - s, c);
            renderer.quad(this.x + this.width - s, this.y, s, this.height - s, c);
        }
    }

    private class WValue
    extends WDropdown.WDropdownValue
    implements GonbleWareWidget {
        private WValue(WGbonleWareDropdown wGbonleWareDropdown) {
        }

        @Override
        protected void onCalculateSize() {
            double pad = this.pad();
            this.width = pad + this.theme.textWidth(this.value.toString()) + pad;
            this.height = pad + this.theme.textHeight() + pad;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            GonbleWareGuiTheme theme = this.theme();
            SettingColor color = theme.backgroundColor.get(this.pressed, this.mouseOver, true);
            int preA = color.a;
            color.a += color.a / 2;
            color.validate();
            renderer.quad(this, color);
            color.a = preA;
            String text = this.value.toString();
            renderer.text(text, this.x + this.width / 2.0 - theme.textWidth(text) / 2.0, this.y + this.pad(), theme.textColor.get(), false);
        }
    }
}

