/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package meteordevelopment.meteorclient.renderer.text;

import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;

public interface TextRenderer {
    public static TextRenderer get() {
        return Config.get().customFont.get() != false ? Fonts.RENDERER : VanillaTextRenderer.INSTANCE;
    }

    public void setAlpha(double var1);

    public void begin(double var1, boolean var3, boolean var4);

    default public void begin(double scale) {
        this.begin(scale, false, false);
    }

    default public void begin() {
        this.begin(1.0, false, false);
    }

    default public void beginBig() {
        this.begin(1.0, false, true);
    }

    public double getWidth(String var1, int var2, boolean var3);

    default public double getWidth(String text, boolean shadow) {
        return this.getWidth(text, text.length(), shadow);
    }

    default public double getWidth(String text) {
        return this.getWidth(text, text.length(), false);
    }

    public double getHeight(boolean var1);

    default public double getHeight() {
        return this.getHeight(false);
    }

    public double render(String var1, double var2, double var4, Color var6, boolean var7);

    default public double render(String text, double x, double y, Color color) {
        return this.render(text, x, y, color, false);
    }

    public boolean isBuilding();

    default public void end() {
        this.end(null);
    }

    public void end(MatrixStack var1);
}

