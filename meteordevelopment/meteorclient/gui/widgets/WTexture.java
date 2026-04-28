/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.renderer.Texture;

public class WTexture
extends WWidget {
    private final double width;
    private final double height;
    private final double rotation;
    private final Texture texture;

    public WTexture(double width, double height, double rotation, Texture texture) {
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.texture = texture;
    }

    @Override
    protected void onCalculateSize() {
        ((WWidget)this).width = this.theme.scale(this.width);
        ((WWidget)this).height = this.theme.scale(this.height);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.texture.isValid()) {
            renderer.texture(this.x, this.y, ((WWidget)this).width, ((WWidget)this).height, this.rotation, this.texture);
        }
    }
}

