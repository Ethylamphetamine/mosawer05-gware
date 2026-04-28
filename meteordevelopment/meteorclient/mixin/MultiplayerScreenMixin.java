/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
 *  net.minecraft.client.gui.widget.ButtonWidget$Builder
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MultiplayerScreen.class})
public abstract class MultiplayerScreenMixin
extends Screen {
    @Unique
    private int textColor1;
    @Unique
    private int textColor2;
    @Unique
    private String loggedInAs;
    @Unique
    private int loggedInAsLength;

    public MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        this.textColor1 = Color.fromRGBA(255, 255, 255, 255);
        this.textColor2 = Color.fromRGBA(175, 175, 175, 255);
        this.loggedInAs = "Logged in as ";
        this.loggedInAsLength = this.textRenderer.getWidth(this.loggedInAs);
        this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Accounts"), button -> this.client.setScreen((Screen)GuiThemes.get().accountsScreen())).position(this.width - 75 - 3, 3).size(75, 20).build());
        this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Proxies"), button -> this.client.setScreen((Screen)GuiThemes.get().proxiesScreen())).position(this.width - 75 - 3 - 75 - 2, 3).size(75, 20).build());
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String left;
        int x = 3;
        int y = 3;
        context.drawTextWithShadow(MeteorClient.mc.textRenderer, this.loggedInAs, x, y, this.textColor1);
        context.drawTextWithShadow(MeteorClient.mc.textRenderer, Modules.get().get(NameProtect.class).getName(this.client.getSession().getUsername()), x + this.loggedInAsLength, y, this.textColor2);
        Objects.requireNonNull(this.textRenderer);
        Proxy proxy = Proxies.get().getEnabled();
        String string = left = proxy != null ? "Using proxy " : "Not using a proxy";
        String right = proxy != null ? (String)(proxy.name.get() != null && !proxy.name.get().isEmpty() ? "(" + proxy.name.get() + ") " : "") + proxy.address.get() + ":" + String.valueOf(proxy.port.get()) : null;
        context.drawTextWithShadow(MeteorClient.mc.textRenderer, left, x, y += 9 + 2, this.textColor1);
        if (right != null) {
            context.drawTextWithShadow(MeteorClient.mc.textRenderer, right, x + this.textRenderer.getWidth(left), y, this.textColor2);
        }
    }
}

