/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareGuiTheme;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.util.math.MathHelper;

public class WGonbleWareCheckbox
extends WCheckbox
implements GonbleWareWidget {
    private double animProgress;

    public WGonbleWareCheckbox(boolean checked) {
        super(checked);
        this.animProgress = checked ? 1.0 : 0.0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        GonbleWareGuiTheme theme = this.theme();
        this.animProgress += (double)(this.checked ? 1 : -1) * delta * 14.0;
        this.animProgress = MathHelper.clamp((double)this.animProgress, (double)0.0, (double)1.0);
        this.renderBackground(renderer, this, this.pressed, this.mouseOver);
        if (this.animProgress > 0.0) {
            double cs = (this.width - theme.scale(2.0)) / 1.75 * this.animProgress;
            renderer.quad(this.x + (this.width - cs) / 2.0, this.y + (this.height - cs) / 2.0, cs, cs, theme.checkboxColor.get());
        }
    }
}

