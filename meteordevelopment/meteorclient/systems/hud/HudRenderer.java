/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.Identifier
 *  org.lwjgl.BufferUtils
 */
package meteordevelopment.meteorclient.systems.hud;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.CustomFontChangedEvent;
import meteordevelopment.meteorclient.renderer.DrawMode;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShaderMesh;
import meteordevelopment.meteorclient.renderer.Shaders;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.renderer.text.Font;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;

public class HudRenderer {
    public static final HudRenderer INSTANCE = new HudRenderer();
    private static final double SCALE_TO_HEIGHT = 0.05555555555555555;
    private final Hud hud = Hud.get();
    private final List<Runnable> postTasks = new ArrayList<Runnable>();
    private final Int2ObjectMap<FontHolder> fontsInUse = new Int2ObjectOpenHashMap();
    private final LoadingCache<Integer, FontHolder> fontCache = CacheBuilder.newBuilder().maximumSize(4L).expireAfterAccess(Duration.ofMinutes(10L)).removalListener(notification -> {
        if (notification.wasEvicted()) {
            ((FontHolder)notification.getValue()).destroy();
        }
    }).build(CacheLoader.from(HudRenderer::loadFont));
    public DrawContext drawContext;
    public double delta;

    private HudRenderer() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void begin(DrawContext drawContext) {
        Renderer2D.COLOR.begin();
        this.drawContext = drawContext;
        this.delta = Utils.frameTime;
        if (!this.hud.hasCustomFont()) {
            VanillaTextRenderer.INSTANCE.scaleIndividually = true;
            VanillaTextRenderer.INSTANCE.begin();
        }
    }

    public void end() {
        Renderer2D.COLOR.render(new MatrixStack());
        if (this.hud.hasCustomFont()) {
            ObjectIterator it = this.fontsInUse.values().iterator();
            while (it.hasNext()) {
                FontHolder fontHolder = (FontHolder)it.next();
                if (fontHolder.visited) {
                    GL.bindTexture(fontHolder.font.texture.getGlId());
                    fontHolder.getMesh().render(null);
                } else {
                    it.remove();
                    this.fontCache.put((Object)fontHolder.font.getHeight(), (Object)fontHolder);
                }
                fontHolder.visited = false;
            }
        } else {
            VanillaTextRenderer.INSTANCE.end();
            VanillaTextRenderer.INSTANCE.scaleIndividually = false;
        }
        for (Runnable task : this.postTasks) {
            task.run();
        }
        this.postTasks.clear();
        this.drawContext = null;
    }

    public void line(double x1, double y1, double x2, double y2, Color color) {
        Renderer2D.COLOR.line(x1, y1, x2, y2, color);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        Renderer2D.COLOR.quad(x, y, width, height, color);
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        Renderer2D.COLOR.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        Renderer2D.COLOR.triangle(x1, y1, x2, y2, x3, y3, color);
    }

    public void texture(Identifier id, double x, double y, double width, double height, Color color) {
        GL.bindTexture(id);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, width, height, color);
        Renderer2D.TEXTURE.render(null);
    }

    public double text(String text, double x, double y, Color color, boolean shadow, double scale) {
        double width;
        if (scale == -1.0) {
            scale = this.hud.getTextScale();
        }
        if (!this.hud.hasCustomFont()) {
            VanillaTextRenderer.INSTANCE.scale = scale * 2.0;
            return VanillaTextRenderer.INSTANCE.render(text, x, y, color, shadow);
        }
        FontHolder fontHolder = this.getFontHolder(scale, true);
        Font font = fontHolder.font;
        Mesh mesh = fontHolder.getMesh();
        if (shadow) {
            int preShadowA = CustomTextRenderer.SHADOW_COLOR.a;
            CustomTextRenderer.SHADOW_COLOR.a = (int)((double)color.a / 255.0 * (double)preShadowA);
            width = font.render(mesh, text, x + 1.0, y + 1.0, CustomTextRenderer.SHADOW_COLOR, scale);
            font.render(mesh, text, x, y, color, scale);
            CustomTextRenderer.SHADOW_COLOR.a = preShadowA;
        } else {
            width = font.render(mesh, text, x, y, color, scale);
        }
        return width;
    }

    public double text(String text, double x, double y, Color color, boolean shadow) {
        return this.text(text, x, y, color, shadow, -1.0);
    }

    public double textWidth(String text, boolean shadow, double scale) {
        if (text.isEmpty()) {
            return 0.0;
        }
        if (this.hud.hasCustomFont()) {
            double width = this.getFont(scale).getWidth(text, text.length());
            return (width + (double)(shadow ? 1 : 0)) * (scale == -1.0 ? this.hud.getTextScale() : scale) + (double)(shadow ? 1 : 0);
        }
        VanillaTextRenderer.INSTANCE.scale = (scale == -1.0 ? this.hud.getTextScale() : scale) * 2.0;
        return VanillaTextRenderer.INSTANCE.getWidth(text, shadow);
    }

    public double textWidth(String text, boolean shadow) {
        return this.textWidth(text, shadow, -1.0);
    }

    public double textWidth(String text, double scale) {
        return this.textWidth(text, false, scale);
    }

    public double textWidth(String text) {
        return this.textWidth(text, false, -1.0);
    }

    public double textHeight(boolean shadow, double scale) {
        if (this.hud.hasCustomFont()) {
            double height = this.getFont(scale).getHeight() + 1;
            return (height + (double)(shadow ? 1 : 0)) * (scale == -1.0 ? this.hud.getTextScale() : scale);
        }
        VanillaTextRenderer.INSTANCE.scale = (scale == -1.0 ? this.hud.getTextScale() : scale) * 2.0;
        return VanillaTextRenderer.INSTANCE.getHeight(shadow);
    }

    public double textHeight(boolean shadow) {
        return this.textHeight(shadow, -1.0);
    }

    public double textHeight() {
        return this.textHeight(false, -1.0);
    }

    public void post(Runnable task) {
        this.postTasks.add(task);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay, String countOverlay) {
        RenderUtils.drawItem(this.drawContext, itemStack, x, y, scale, overlay, countOverlay);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(this.drawContext, itemStack, x, y, scale, overlay);
    }

    private FontHolder getFontHolder(double scale, boolean render) {
        int height;
        FontHolder fontHolder;
        if (scale == -1.0) {
            scale = this.hud.getTextScale();
        }
        if ((fontHolder = (FontHolder)this.fontsInUse.get(height = (int)Math.round(scale / 0.05555555555555555))) != null) {
            if (render) {
                fontHolder.visited = true;
            }
            return fontHolder;
        }
        if (render) {
            fontHolder = (FontHolder)this.fontCache.getIfPresent((Object)height);
            if (fontHolder == null) {
                fontHolder = HudRenderer.loadFont(height);
            } else {
                this.fontCache.invalidate((Object)height);
            }
            this.fontsInUse.put(height, (Object)fontHolder);
            fontHolder.visited = true;
            return fontHolder;
        }
        return (FontHolder)this.fontCache.getUnchecked((Object)height);
    }

    private Font getFont(double scale) {
        return this.getFontHolder((double)scale, (boolean)false).font;
    }

    @EventHandler
    private void onCustomFontChanged(CustomFontChangedEvent event) {
        for (FontHolder fontHolder : this.fontsInUse.values()) {
            fontHolder.destroy();
        }
        for (FontHolder fontHolder : this.fontCache.asMap().values()) {
            fontHolder.destroy();
        }
        this.fontsInUse.clear();
        this.fontCache.invalidateAll();
    }

    private static FontHolder loadFont(int height) {
        byte[] data = Utils.readBytes(Fonts.RENDERER.fontFace.toStream());
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)data.length).put(data).flip();
        return new FontHolder(new Font(buffer, height));
    }

    private static class FontHolder {
        public final Font font;
        public boolean visited;
        private Mesh mesh;

        public FontHolder(Font font) {
            this.font = font;
        }

        public Mesh getMesh() {
            if (this.mesh == null) {
                this.mesh = new ShaderMesh(Shaders.TEXT, DrawMode.Triangles, Mesh.Attrib.Vec2, Mesh.Attrib.Vec2, Mesh.Attrib.Color);
            }
            if (!this.mesh.isBuilding()) {
                this.mesh.begin();
            }
            return this.mesh;
        }

        public void destroy() {
            this.font.texture.clearGlId();
            if (this.mesh != null) {
                this.mesh.destroy();
            }
        }
    }
}

