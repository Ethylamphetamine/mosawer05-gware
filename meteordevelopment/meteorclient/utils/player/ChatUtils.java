/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.text.TextColor
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Pair
 *  net.minecraft.util.math.Vec3d
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.utils.player;

import com.mojang.brigadier.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ChatUtils {
    private static final List<Pair<String, Supplier<Text>>> customPrefixes = new ArrayList<Pair<String, Supplier<Text>>>();
    private static String forcedPrefixClassName;
    private static Text PREFIX;

    private ChatUtils() {
    }

    @PostInit
    public static void init() {
        PREFIX = Text.empty().setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)).append("[").append((Text)Text.literal((String)"GWare").setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)MeteorClient.ADDON.color.getPacked())))).append("] ");
    }

    public static Text getMeteorPrefix() {
        return PREFIX;
    }

    public static void registerCustomPrefix(String packageName, Supplier<Text> supplier) {
        for (Pair<String, Supplier<Text>> pair : customPrefixes) {
            if (!((String)pair.getLeft()).equals(packageName)) continue;
            pair.setRight(supplier);
            return;
        }
        customPrefixes.add((Pair<String, Supplier<Text>>)new Pair((Object)packageName, supplier));
    }

    public static void unregisterCustomPrefix(String packageName) {
        customPrefixes.removeIf(pair -> ((String)pair.getLeft()).equals(packageName));
    }

    public static void forceNextPrefixClass(Class<?> klass) {
        forcedPrefixClassName = klass.getName();
    }

    public static void sendPlayerMsg(String message) {
        MeteorClient.mc.inGameHud.getChatHud().addToMessageHistory(message);
        if (message.startsWith("/")) {
            MeteorClient.mc.player.networkHandler.sendChatCommand(message.substring(1));
        } else {
            MeteorClient.mc.player.networkHandler.sendChatMessage(message);
        }
    }

    public static void info(String message, Object ... args) {
        ChatUtils.sendMsg(Formatting.GRAY, message, args);
    }

    public static void infoPrefix(String prefix, String message, Object ... args) {
        ChatUtils.sendMsg(0, prefix, Formatting.LIGHT_PURPLE, Formatting.GRAY, message, args);
    }

    public static void warning(String message, Object ... args) {
        ChatUtils.sendMsg(Formatting.YELLOW, message, args);
    }

    public static void warningPrefix(String prefix, String message, Object ... args) {
        ChatUtils.sendMsg(0, prefix, Formatting.LIGHT_PURPLE, Formatting.YELLOW, message, args);
    }

    public static void error(String message, Object ... args) {
        ChatUtils.sendMsg(Formatting.RED, message, args);
    }

    public static void errorPrefix(String prefix, String message, Object ... args) {
        ChatUtils.sendMsg(0, prefix, Formatting.LIGHT_PURPLE, Formatting.RED, message, args);
    }

    public static void sendMsg(Text message) {
        ChatUtils.sendMsg(null, message);
    }

    public static void sendMsg(String prefix, Text message) {
        ChatUtils.sendMsg(0, prefix, Formatting.LIGHT_PURPLE, message);
    }

    public static void sendMsg(Formatting color, String message, Object ... args) {
        ChatUtils.sendMsg(0, null, null, color, message, args);
    }

    public static void sendMsg(int id, Formatting color, String message, Object ... args) {
        ChatUtils.sendMsg(id, null, null, color, message, args);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable Formatting prefixColor, Formatting messageColor, String messageContent, Object ... args) {
        MutableText message = ChatUtils.formatMsg(String.format(messageContent, args), messageColor);
        ChatUtils.sendMsg(id, prefixTitle, prefixColor, (Text)message);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable Formatting prefixColor, String messageContent, Formatting messageColor) {
        MutableText message = ChatUtils.formatMsg(messageContent, messageColor);
        ChatUtils.sendMsg(id, prefixTitle, prefixColor, (Text)message);
    }

    public static void sendMsg(int id, @Nullable String prefixTitle, @Nullable Formatting prefixColor, Text msg) {
        if (MeteorClient.mc.world == null) {
            return;
        }
        MutableText message = Text.empty();
        message.append(ChatUtils.getPrefix());
        if (prefixTitle != null) {
            message.append((Text)ChatUtils.getCustomPrefix(prefixTitle, prefixColor));
        }
        message.append(msg);
        if (!Config.get().deleteChatFeedback.get().booleanValue()) {
            id = 0;
        }
        ((IChatHud)MeteorClient.mc.inGameHud.getChatHud()).meteor$add((Text)message, id);
    }

    private static MutableText getCustomPrefix(String prefixTitle, Formatting prefixColor) {
        MutableText prefix = Text.empty();
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        prefix.append("[");
        MutableText moduleTitle = Text.literal((String)prefixTitle);
        moduleTitle.setStyle(moduleTitle.getStyle().withFormatting(prefixColor));
        prefix.append((Text)moduleTitle);
        prefix.append("] ");
        return prefix;
    }

    private static Text getPrefix() {
        if (customPrefixes.isEmpty()) {
            forcedPrefixClassName = null;
            return PREFIX;
        }
        boolean foundChatUtils = false;
        String className = null;
        if (forcedPrefixClassName != null) {
            className = forcedPrefixClassName;
            forcedPrefixClassName = null;
        } else {
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (foundChatUtils) {
                    if (element.getClassName().equals(ChatUtils.class.getName())) continue;
                    className = element.getClassName();
                    break;
                }
                if (!element.getClassName().equals(ChatUtils.class.getName())) continue;
                foundChatUtils = true;
            }
        }
        if (className == null) {
            return PREFIX;
        }
        for (Pair pair : customPrefixes) {
            if (!className.startsWith((String)pair.getLeft())) continue;
            Text prefix = (Text)((Supplier)pair.getRight()).get();
            return prefix != null ? prefix : PREFIX;
        }
        return PREFIX;
    }

    private static MutableText formatMsg(String message, Formatting defaultColor) {
        StringReader reader = new StringReader(message);
        MutableText text = Text.empty();
        Style style = Style.EMPTY.withFormatting(defaultColor);
        StringBuilder result = new StringBuilder();
        boolean formatting = false;
        while (reader.canRead()) {
            char c = reader.read();
            if (c == '(') {
                text.append((Text)Text.literal((String)result.toString()).setStyle(style));
                result.setLength(0);
                result.append(c);
                formatting = true;
                continue;
            }
            result.append(c);
            if (!formatting || c != ')') continue;
            switch (result.toString()) {
                case "(default)": {
                    style = style.withFormatting(defaultColor);
                    result.setLength(0);
                    break;
                }
                case "(highlight)": {
                    style = style.withFormatting(Formatting.WHITE);
                    result.setLength(0);
                    break;
                }
                case "(underline)": {
                    style = style.withFormatting(Formatting.UNDERLINE);
                    result.setLength(0);
                    break;
                }
                case "(bold)": {
                    style = style.withFormatting(Formatting.BOLD);
                    result.setLength(0);
                }
            }
            formatting = false;
        }
        if (!result.isEmpty()) {
            text.append((Text)Text.literal((String)result.toString()).setStyle(style));
        }
        return text;
    }

    public static MutableText formatCoords(Vec3d pos) {
        String coordsString = String.format("(highlight)(underline)%.0f, %.0f, %.0f(default)", pos.x, pos.y, pos.z);
        MutableText coordsText = ChatUtils.formatMsg(coordsString, Formatting.GRAY);
        if (BaritoneUtils.IS_AVAILABLE) {
            Style style = coordsText.getStyle().withFormatting(Formatting.BOLD).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Set as Baritone goal"))).withClickEvent((ClickEvent)new MeteorClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("%sgoto %d %d %d", BaritoneUtils.getPrefix(), (int)pos.x, (int)pos.y, (int)pos.z)));
            coordsText.setStyle(style);
        }
        return coordsText;
    }
}

