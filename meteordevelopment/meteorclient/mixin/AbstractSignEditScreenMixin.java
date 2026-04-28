/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen
 *  net.minecraft.text.KeybindTextContent
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.PlainTextContent$Literal
 *  net.minecraft.text.Text
 *  net.minecraft.text.TextContent
 *  net.minecraft.text.TranslatableTextContent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={AbstractSignEditScreen.class})
public abstract class AbstractSignEditScreenMixin {
    @ModifyExpressionValue(method={"<init>(Lnet/minecraft/block/entity/SignBlockEntity;ZZLnet/minecraft/text/Text;)V"}, at={@At(value="INVOKE", target="Ljava/util/stream/IntStream;mapToObj(Ljava/util/function/IntFunction;)Ljava/util/stream/Stream;")})
    private Stream<Text> modifyTranslatableText(Stream<Text> original) {
        return original.map(this::modifyText);
    }

    @Unique
    private Text modifyText(Text message) {
        KeybindTextContent content;
        Object key;
        MutableText modified = MutableText.of((TextContent)message.getContent());
        TextContent textContent = message.getContent();
        if (textContent instanceof KeybindTextContent && ((String)(key = (content = (KeybindTextContent)textContent).getKey())).contains("meteor-client")) {
            modified = MutableText.of((TextContent)new PlainTextContent.Literal((String)key));
        }
        if ((key = message.getContent()) instanceof TranslatableTextContent && ((String)(key = (content = (TranslatableTextContent)key).getKey())).contains("meteor-client")) {
            modified = MutableText.of((TextContent)new PlainTextContent.Literal((String)key));
        }
        modified.setStyle(message.getStyle());
        for (Text sibling : message.getSiblings()) {
            modified.append(this.modifyText(sibling));
        }
        return modified;
    }
}

