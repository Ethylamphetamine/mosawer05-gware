/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReceiver
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.ChatHud
 *  net.minecraft.client.gui.hud.ChatHudLine
 *  net.minecraft.client.gui.hud.ChatHudLine$Visible
 *  net.minecraft.client.gui.hud.MessageIndicator
 *  net.minecraft.client.gui.hud.MessageIndicator$Icon
 *  net.minecraft.network.message.MessageSignatureData
 *  net.minecraft.text.OrderedText
 *  net.minecraft.text.Text
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.LocalCapture
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import meteordevelopment.meteorclient.mixininterface.IMessageHandler;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value={ChatHud.class})
public abstract class ChatHudMixin
implements IChatHud {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;
    @Unique
    private BetterChat betterChat;
    @Unique
    private int nextId;
    @Unique
    private boolean skipOnAddMessage;

    @Shadow
    public abstract void addMessage(Text var1, @Nullable MessageSignatureData var2, @Nullable MessageIndicator var3);

    @Shadow
    public abstract void addMessage(Text var1);

    @Override
    public void meteor$add(Text message, int id) {
        this.nextId = id;
        this.addMessage(message);
        this.nextId = 0;
    }

    @Inject(method={"addVisibleMessage"}, at={@At(value="INVOKE", target="Ljava/util/List;add(ILjava/lang/Object;)V", shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLineVisible(ChatHudLine message, CallbackInfo ci) {
        ((IChatHudLine)this.visibleMessages.getFirst()).meteor$setId(this.nextId);
    }

    @Inject(method={"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at={@At(value="INVOKE", target="Ljava/util/List;add(ILjava/lang/Object;)V", shift=At.Shift.AFTER)})
    private void onAddMessageAfterNewChatHudLine(ChatHudLine message, CallbackInfo ci) {
        ((IChatHudLine)this.messages.getFirst()).meteor$setId(this.nextId);
    }

    @ModifyExpressionValue(method={"addVisibleMessage"}, at={@At(value="NEW", target="(ILnet/minecraft/text/OrderedText;Lnet/minecraft/client/gui/hud/MessageIndicator;Z)Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;")})
    private ChatHudLine.Visible onAddMessage_modifyChatHudLineVisible(ChatHudLine.Visible line, @Local(ordinal=1) int j) {
        IMessageHandler handler = (IMessageHandler)this.client.getMessageHandler();
        if (handler == null) {
            return line;
        }
        IChatHudLineVisible meteorLine = (IChatHudLineVisible)line;
        meteorLine.meteor$setSender(handler.meteor$getSender());
        meteorLine.meteor$setStartOfEntry(j == 0);
        return line;
    }

    @ModifyExpressionValue(method={"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"}, at={@At(value="NEW", target="(ILnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)Lnet/minecraft/client/gui/hud/ChatHudLine;")})
    private ChatHudLine onAddMessage_modifyChatHudLine(ChatHudLine line) {
        IMessageHandler handler = (IMessageHandler)this.client.getMessageHandler();
        if (handler == null) {
            return line;
        }
        ((IChatHudLine)line).meteor$setSender(handler.meteor$getSender());
        return line;
    }

    @Inject(at={@At(value="HEAD")}, method={"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"}, cancellable=true)
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (this.skipOnAddMessage) {
            return;
        }
        ReceiveMessageEvent event = MeteorClient.EVENT_BUS.post(ReceiveMessageEvent.get(message, indicator, this.nextId));
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            this.visibleMessages.removeIf(msg -> ((IChatHudLine)msg).meteor$getId() == this.nextId && this.nextId != 0);
            for (int i = this.messages.size() - 1; i > -1; --i) {
                if (((IChatHudLine)this.messages.get(i)).meteor$getId() != this.nextId || this.nextId == 0) continue;
                this.messages.remove(i);
                this.getBetterChat().removeLine(i);
            }
            if (event.isModified()) {
                ci.cancel();
                this.skipOnAddMessage = true;
                this.addMessage(event.getMessage(), signatureData, event.getIndicator());
                this.skipOnAddMessage = false;
            }
        }
    }

    @ModifyExpressionValue(method={"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at={@At(value="CONSTANT", args={"intValue=100"})})
    private int maxLength(int size) {
        if (Modules.get() == null || !this.getBetterChat().isLongerChat()) {
            return size;
        }
        return size + this.betterChat.getExtraChatLines();
    }

    @ModifyExpressionValue(method={"addVisibleMessage"}, at={@At(value="CONSTANT", args={"intValue=100"})})
    private int maxLengthVisible(int size) {
        if (Modules.get() == null || !this.getBetterChat().isLongerChat()) {
            return size;
        }
        return size + this.betterChat.getExtraChatLines();
    }

    @ModifyExpressionValue(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;ceil(F)I")})
    private int onRender_modifyWidth(int width) {
        return this.getBetterChat().modifyChatWidth(width);
    }

    @ModifyReceiver(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I")})
    private DrawContext onRender_beforeDrawTextWithShadow(DrawContext context, TextRenderer textRenderer, OrderedText text, int x, int y, int color, @Local ChatHudLine.Visible line) {
        this.getBetterChat().drawPlayerHead(context, line, y, color);
        return context;
    }

    @ModifyExpressionValue(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;")})
    private MessageIndicator onRender_modifyIndicator(MessageIndicator indicator) {
        return Modules.get().get(NoRender.class).noMessageSignatureIndicator() ? null : indicator;
    }

    @Inject(method={"addVisibleMessage"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z")}, locals=LocalCapture.CAPTURE_FAILSOFT)
    private void onBreakChatMessageLines(ChatHudLine message, CallbackInfo ci, int i, MessageIndicator.Icon icon, List<OrderedText> list) {
        if (Modules.get() == null) {
            return;
        }
        this.getBetterChat().lines.addFirst((Object)list.size());
    }

    @Inject(method={"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at={@At(value="INVOKE", target="Ljava/util/List;remove(I)Ljava/lang/Object;")})
    private void onRemoveMessage(ChatHudLine message, CallbackInfo ci) {
        if (Modules.get() == null) {
            return;
        }
        int extra = this.getBetterChat().isLongerChat() ? this.getBetterChat().getExtraChatLines() : 0;
        for (int size = this.betterChat.lines.size(); size > 100 + extra; --size) {
            this.betterChat.lines.removeLast();
        }
    }

    @Inject(method={"clear"}, at={@At(value="HEAD")})
    private void onClear(boolean clearHistory, CallbackInfo ci) {
        this.getBetterChat().lines.clear();
    }

    @Inject(method={"refresh"}, at={@At(value="HEAD")})
    private void onRefresh(CallbackInfo ci) {
        this.getBetterChat().lines.clear();
    }

    @Unique
    private BetterChat getBetterChat() {
        if (this.betterChat == null) {
            this.betterChat = Modules.get().get(BetterChat.class);
        }
        return this.betterChat;
    }
}

