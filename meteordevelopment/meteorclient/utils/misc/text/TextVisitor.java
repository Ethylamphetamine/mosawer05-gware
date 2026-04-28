/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.PlainTextContent
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.text.TextContent
 */
package meteordevelopment.meteorclient.utils.misc.text;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

@FunctionalInterface
public interface TextVisitor<T> {
    public Optional<T> accept(Text var1, Style var2, String var3);

    public static <T> Optional<T> visit(Text text, TextVisitor<T> visitor, Style baseStyle) {
        ArrayDeque<Text> queue = TextVisitor.collectSiblings(text);
        return text.visit((style, string) -> visitor.accept((Text)queue.remove(), style, string), baseStyle);
    }

    public static ArrayDeque<Text> collectSiblings(Text text) {
        ArrayDeque<Text> queue = new ArrayDeque<Text>();
        TextVisitor.collectSiblings(text, queue);
        return queue;
    }

    private static void collectSiblings(Text text, Queue<Text> queue) {
        PlainTextContent ptc;
        TextContent textContent = text.getContent();
        if (!(textContent instanceof PlainTextContent) || !(ptc = (PlainTextContent)textContent).comp_737().isEmpty()) {
            queue.add(text);
        }
        for (Text sibling : text.getSiblings()) {
            TextVisitor.collectSiblings(sibling, queue);
        }
    }
}

