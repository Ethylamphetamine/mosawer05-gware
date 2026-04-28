/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  org.lwjgl.BufferUtils
 */
package meteordevelopment.meteorclient.renderer.text;

import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.renderer.DrawMode;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.ShaderMesh;
import meteordevelopment.meteorclient.renderer.Shaders;
import meteordevelopment.meteorclient.renderer.text.Font;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.BufferUtils;

public class CustomTextRenderer
implements TextRenderer {
    public static final Color SHADOW_COLOR = new Color(60, 60, 60, 180);
    private final Mesh mesh = new ShaderMesh(Shaders.TEXT, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Vec2, Mesh.Attrib.Color);
    public final FontFace fontFace;
    private final Font[] fonts;
    private Font font;
    private boolean building;
    private boolean scaleOnly;
    private double fontScale = 1.0;
    private double scale = 1.0;

    public CustomTextRenderer(FontFace fontFace) {
        this.fontFace = fontFace;
        byte[] bytes = Utils.readBytes(fontFace.toStream());
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)bytes.length).put(bytes).flip();
        this.fonts = new Font[5];
        for (int i = 0; i < this.fonts.length; ++i) {
            this.fonts[i] = new Font(buffer, (int)Math.round(27.0 * ((double)i * 0.5 + 1.0)));
        }
    }

    @Override
    public void setAlpha(double a) {
        this.mesh.alpha = a;
    }

    @Override
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (this.building) {
            throw new RuntimeException("CustomTextRenderer.begin() called twice");
        }
        if (!scaleOnly) {
            this.mesh.begin();
        }
        if (big) {
            this.font = this.fonts[this.fonts.length - 1];
        } else {
            double scaleA = Math.floor(scale * 10.0) / 10.0;
            int scaleI = scaleA >= 3.0 ? 5 : (scaleA >= 2.5 ? 4 : (scaleA >= 2.0 ? 3 : (scaleA >= 1.5 ? 2 : 1)));
            this.font = this.fonts[scaleI - 1];
        }
        this.building = true;
        this.scaleOnly = scaleOnly;
        this.fontScale = (double)this.font.getHeight() / 27.0;
        this.scale = 1.0 + (scale - this.fontScale) / this.fontScale;
    }

    @Override
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0;
        }
        Font font = this.building ? this.font : this.fonts[0];
        return (font.getWidth(text, length) + (double)(shadow ? 1 : 0)) * this.scale / 1.5;
    }

    @Override
    public double getHeight(boolean shadow) {
        Font font = this.building ? this.font : this.fonts[0];
        return (double)(font.getHeight() + 1 + (shadow ? 1 : 0)) * this.scale / 1.5;
    }

    @Override
    public double render(String text, double x, double y, Color color, boolean shadow) {
        double width;
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            this.begin();
        }
        if (shadow) {
            int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
            CustomTextRenderer.SHADOW_COLOR.a = (int)((double)color.a / 255.0 * (double)preShadowA);
            width = this.font.render(this.mesh, text, x + this.fontScale * this.scale / 1.5, y + this.fontScale * this.scale / 1.5, SHADOW_COLOR, this.scale / 1.5);
            this.font.render(this.mesh, text, x, y, color, this.scale / 1.5);
            CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
        } else {
            width = this.font.render(this.mesh, text, x, y, color, this.scale / 1.5);
        }
        if (!wasBuilding) {
            this.end();
        }
        return width;
    }

    @Override
    public boolean isBuilding() {
        return this.building;
    }

    @Override
    public void end(MatrixStack matrices) {
        if (!this.building) {
            throw new RuntimeException("CustomTextRenderer.end() called without calling begin()");
        }
        if (!this.scaleOnly) {
            this.mesh.end();
            GL.bindTexture(this.font.texture.getGlId());
            this.mesh.render(matrices);
        }
        this.building = false;
        this.scale = 1.0;
    }

    public void destroy() {
        this.mesh.destroy();
    }
}

