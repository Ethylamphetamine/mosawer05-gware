/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntDoubleImmutablePair
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 */
package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.ints.IntDoubleImmutablePair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.renderer.Framebuffer;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.PostProcessRenderer;
import meteordevelopment.meteorclient.renderer.Shader;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class Blur
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScreens;
    private final IntDoubleImmutablePair[] strengths;
    private final Setting<Integer> strength;
    private final Setting<Integer> fadeTime;
    private final Setting<Boolean> meteor;
    private final Setting<Boolean> inventories;
    private final Setting<Boolean> chat;
    private final Setting<Boolean> other;
    private Shader shaderDown;
    private Shader shaderUp;
    private Shader shaderPassthrough;
    private final Framebuffer[] fbos;
    private boolean enabled;
    private long fadeEndAt;

    public Blur() {
        super(Categories.Render, "blur", "Blurs background when in GUI screens.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScreens = this.settings.createGroup("Screens");
        this.strengths = new IntDoubleImmutablePair[]{IntDoubleImmutablePair.of((int)1, (double)1.25), IntDoubleImmutablePair.of((int)1, (double)2.25), IntDoubleImmutablePair.of((int)2, (double)2.0), IntDoubleImmutablePair.of((int)2, (double)3.0), IntDoubleImmutablePair.of((int)2, (double)4.25), IntDoubleImmutablePair.of((int)3, (double)2.5), IntDoubleImmutablePair.of((int)3, (double)3.25), IntDoubleImmutablePair.of((int)3, (double)4.25), IntDoubleImmutablePair.of((int)3, (double)5.5), IntDoubleImmutablePair.of((int)4, (double)3.25), IntDoubleImmutablePair.of((int)4, (double)4.0), IntDoubleImmutablePair.of((int)4, (double)5.0), IntDoubleImmutablePair.of((int)4, (double)6.0), IntDoubleImmutablePair.of((int)4, (double)7.25), IntDoubleImmutablePair.of((int)4, (double)8.25), IntDoubleImmutablePair.of((int)5, (double)4.5), IntDoubleImmutablePair.of((int)5, (double)5.25), IntDoubleImmutablePair.of((int)5, (double)6.25), IntDoubleImmutablePair.of((int)5, (double)7.25), IntDoubleImmutablePair.of((int)5, (double)8.5)};
        this.strength = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("strength")).description("How strong the blur should be.")).defaultValue(5)).min(1).max(20).sliderRange(1, 20).build());
        this.fadeTime = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("fade-time")).description("How long the fade will last in milliseconds.")).defaultValue(100)).min(0).sliderMax(500).build());
        this.meteor = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("meteor")).description("Applies blur to Meteor screens.")).defaultValue(true)).build());
        this.inventories = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("inventories")).description("Applies blur to inventory screens.")).defaultValue(true)).build());
        this.chat = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat")).description("Applies blur when in chat.")).defaultValue(false)).build());
        this.other = this.sgScreens.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("other")).description("Applies blur to all other screen types.")).defaultValue(true)).build());
        this.fbos = new Framebuffer[6];
        MeteorClient.EVENT_BUS.subscribe(new ConsumerListener<ResolutionChangedEvent>(ResolutionChangedEvent.class, event -> {
            for (int i = 0; i < this.fbos.length; ++i) {
                if (this.fbos[i] != null) {
                    this.fbos[i].resize();
                    continue;
                }
                this.fbos[i] = new Framebuffer(1.0 / Math.pow(2.0, i));
            }
        }));
        MeteorClient.EVENT_BUS.subscribe(new ConsumerListener<RenderAfterWorldEvent>(RenderAfterWorldEvent.class, event -> this.onRenderAfterWorld()));
    }

    private void onRenderAfterWorld() {
        int i;
        boolean shouldRender = this.shouldRender();
        long time = System.currentTimeMillis();
        if (this.enabled) {
            if (!shouldRender) {
                if (this.fadeEndAt == -1L) {
                    this.fadeEndAt = System.currentTimeMillis() + (long)this.fadeTime.get().intValue();
                }
                if (time >= this.fadeEndAt) {
                    this.enabled = false;
                    this.fadeEndAt = -1L;
                }
            }
        } else if (shouldRender) {
            this.enabled = true;
            this.fadeEndAt = System.currentTimeMillis() + (long)this.fadeTime.get().intValue();
        }
        if (!this.enabled) {
            return;
        }
        if (this.shaderDown == null) {
            this.shaderDown = new Shader("blur.vert", "blur_down.frag");
            this.shaderUp = new Shader("blur.vert", "blur_up.frag");
            this.shaderPassthrough = new Shader("passthrough.vert", "passthrough.frag");
            for (int i2 = 0; i2 < this.fbos.length; ++i2) {
                if (this.fbos[i2] != null) continue;
                this.fbos[i2] = new Framebuffer(1.0 / Math.pow(2.0, i2));
            }
        }
        double progress = 1.0;
        if (time < this.fadeEndAt) {
            progress = shouldRender ? 1.0 - (double)(this.fadeEndAt - time) / this.fadeTime.get().doubleValue() : (double)(this.fadeEndAt - time) / this.fadeTime.get().doubleValue();
        } else {
            this.fadeEndAt = -1L;
        }
        IntDoubleImmutablePair strength = this.strengths[(int)((double)(this.strength.get() - 1) * progress)];
        int iterations = strength.leftInt();
        double offset = strength.rightDouble();
        PostProcessRenderer.beginRender();
        this.renderToFbo(this.fbos[0], MinecraftClient.getInstance().getFramebuffer().getColorAttachment(), this.shaderDown, offset);
        for (i = 0; i < iterations; ++i) {
            this.renderToFbo(this.fbos[i + 1], this.fbos[i].texture, this.shaderDown, offset);
        }
        for (i = iterations; i >= 1; --i) {
            this.renderToFbo(this.fbos[i - 1], this.fbos[i].texture, this.shaderUp, offset);
        }
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        this.shaderPassthrough.bind();
        GL.bindTexture(this.fbos[0].texture);
        this.shaderPassthrough.set("uTexture", 0);
        PostProcessRenderer.render();
        PostProcessRenderer.endRender();
    }

    private void renderToFbo(Framebuffer targetFbo, int sourceText, Shader shader, double offset) {
        targetFbo.bind();
        targetFbo.setViewport();
        shader.bind();
        GL.bindTexture(sourceText);
        shader.set("uTexture", 0);
        shader.set("uHalfTexelSize", 0.5 / (double)targetFbo.width, 0.5 / (double)targetFbo.height);
        shader.set("uOffset", offset);
        PostProcessRenderer.render();
    }

    private boolean shouldRender() {
        if (!this.isActive()) {
            return false;
        }
        Screen screen = this.mc.currentScreen;
        if (screen instanceof WidgetScreen) {
            return this.meteor.get();
        }
        if (screen instanceof HandledScreen) {
            return this.inventories.get();
        }
        if (screen instanceof ChatScreen) {
            return this.chat.get();
        }
        if (screen != null) {
            return this.other.get();
        }
        return false;
    }
}

