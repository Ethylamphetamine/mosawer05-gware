/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.llamalad7.mixinextras.sugar.ref.LocalIntRef
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.PlayerListHud
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.MathHelper
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerListHud.class})
public abstract class PlayerListHudMixin {
    @Shadow
    protected abstract List<PlayerListEntry> collectPlayerEntries();

    @Inject(method={"collectPlayerEntries"}, at={@At(value="RETURN")}, cancellable=true)
    private void modifyPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        List originalList = (List)cir.getReturnValue();
        BetterTab betterTab = Modules.get().get(BetterTab.class);
        List<PlayerListEntry> modifiedList = originalList.stream().filter(betterTab::shouldShowPlayer).toList();
        cir.setReturnValue(modifiedList);
    }

    @ModifyConstant(constant={@Constant(longValue=80L)}, method={"collectPlayerEntries"})
    private long modifyCount(long count) {
        BetterTab module = Modules.get().get(BetterTab.class);
        return module.isActive() ? (long)module.tabSize.get().intValue() : count;
    }

    @Inject(method={"getPlayerName"}, at={@At(value="HEAD")}, cancellable=true)
    public void getPlayerName(PlayerListEntry playerListEntry, CallbackInfoReturnable<Text> info) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);
        if (betterTab.isActive()) {
            info.setReturnValue((Object)betterTab.getPlayerName(playerListEntry));
        }
    }

    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Ljava/lang/Math;min(II)I"), index=0)
    private int modifyWidth(int width) {
        BetterTab module = Modules.get().get(BetterTab.class);
        return module.isActive() && module.accurateLatency.get() != false ? width + 30 : width;
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Ljava/lang/Math;min(II)I", shift=At.Shift.BEFORE)})
    private void modifyHeight(CallbackInfo ci, @Local(ordinal=5) LocalIntRef o, @Local(ordinal=6) LocalIntRef p) {
        int newO;
        BetterTab module = Modules.get().get(BetterTab.class);
        if (!module.isActive()) {
            return;
        }
        int newP = 1;
        int totalPlayers = newO = this.collectPlayerEntries().size();
        while (newO > module.tabHeight.get()) {
            newO = (totalPlayers + ++newP - 1) / newP;
        }
        o.set(newO);
        p.set(newP);
    }

    @Inject(method={"renderLatencyIcon"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);
        if (betterTab.isActive() && betterTab.accurateLatency.get().booleanValue()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            TextRenderer textRenderer = mc.textRenderer;
            int latency = MathHelper.clamp((int)entry.getLatency(), (int)0, (int)9999);
            int color = latency < 150 ? 59760 : (latency < 300 ? 15192096 : 14107192);
            String text = latency + "ms";
            context.drawTextWithShadow(textRenderer, text, x + width - textRenderer.getWidth(text), y, color);
            ci.cancel();
        }
    }
}

