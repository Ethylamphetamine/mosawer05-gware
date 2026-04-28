/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.Style
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.misc.text.RunnableClickEvent;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Screen.class}, priority=500)
public abstract class ScreenMixin {
    @Inject(method={"renderInGameBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderInGameBackground(CallbackInfo info) {
        if (Utils.canUpdate() && Modules.get().get(NoRender.class).noGuiBackground()) {
            info.cancel();
        }
    }

    @Inject(method={"handleTextClick"}, at={@At(value="HEAD")}, cancellable=true)
    private void onInvalidClickEvent(@Nullable Style style, CallbackInfoReturnable<Boolean> cir) {
        ClickEvent clickEvent;
        if (style == null || !((clickEvent = style.getClickEvent()) instanceof RunnableClickEvent)) {
            return;
        }
        RunnableClickEvent runnableClickEvent = (RunnableClickEvent)clickEvent;
        runnableClickEvent.runnable.run();
        cir.setReturnValue((Object)true);
    }

    @Inject(method={"handleTextClick"}, at={@At(value="INVOKE", target="Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", ordinal=1, remap=false)}, cancellable=true)
    private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        MeteorClickEvent clickEvent;
        ClickEvent clickEvent2 = style.getClickEvent();
        if (clickEvent2 instanceof MeteorClickEvent && (clickEvent = (MeteorClickEvent)clickEvent2).getValue().startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(style.getClickEvent().getValue().substring(Config.get().prefix.get().length()));
                cir.setReturnValue((Object)true);
            }
            catch (CommandSyntaxException e) {
                MeteorClient.LOG.error("Failed to run command", (Throwable)e);
            }
        }
    }

    @Inject(method={"keyPressed"}, at={@At(value="HEAD")}, cancellable=true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
        if (this instanceof ChatScreen) {
            return;
        }
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        List<Integer> arrows = List.of(Integer.valueOf(262), Integer.valueOf(263), Integer.valueOf(264), Integer.valueOf(265));
        if (guiMove.disableArrows() && arrows.contains(keyCode) || guiMove.disableSpace() && keyCode == 32) {
            info.cancel();
        }
    }
}

