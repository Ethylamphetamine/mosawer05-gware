/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.blaze3d.systems.RenderSystem
 *  it.unimi.dsi.fastutil.chars.Char2CharMap
 *  it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.ChatHudLine
 *  net.minecraft.client.gui.hud.ChatHudLine$Visible
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.mixin.ChatHudAccessor;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.misc.text.TextVisitor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BetterChat
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgFilter;
    private final SettingGroup sgLongerChat;
    private final SettingGroup sgPrefix;
    private final SettingGroup sgSuffix;
    private final Setting<Boolean> annoy;
    private final Setting<Boolean> fancy;
    private final Setting<Boolean> timestamps;
    private final Setting<Boolean> playerHeads;
    private final Setting<Boolean> coordsProtection;
    private final Setting<Boolean> keepHistory;
    private final Setting<Boolean> highlightNearby;
    private final Setting<Boolean> dingNearby;
    private final Setting<Boolean> antiSpam;
    private final Setting<Integer> antiSpamDepth;
    private final Setting<Boolean> antiClear;
    private final Setting<Boolean> filterRegex;
    private final Setting<List<String>> regexFilters;
    private final Setting<Boolean> infiniteChatBox;
    private final Setting<Boolean> longerChatHistory;
    private final Setting<Integer> longerChatLines;
    private final Setting<Boolean> prefix;
    private final Setting<Boolean> prefixRandom;
    private final Setting<String> prefixText;
    private final Setting<Boolean> prefixSmallCaps;
    private final Setting<Boolean> suffix;
    private final Setting<Boolean> suffixRandom;
    private final Setting<String> suffixText;
    private final Setting<Boolean> suffixSmallCaps;
    private static final Pattern antiSpamRegex = Pattern.compile(" \\(([0-9]+)\\)$");
    private static final Pattern antiClearRegex = Pattern.compile("\\n(\\n|\\s)+\\n");
    private static final Pattern timestampRegex = Pattern.compile("^(<[0-9]{2}:[0-9]{2}>\\s)");
    private static final Pattern usernameRegex = Pattern.compile("^(?:<[0-9]{2}:[0-9]{2}>\\s)?<(.*?)>.*");
    private final Char2CharMap SMALL_CAPS;
    private final SimpleDateFormat dateFormat;
    public final IntList lines;
    private static final List<CustomHeadEntry> CUSTOM_HEAD_ENTRIES = new ArrayList<CustomHeadEntry>();
    private static final Pattern TIMESTAMP_REGEX = Pattern.compile("^<\\d{1,2}:\\d{1,2}>");
    private final List<Pattern> filterRegexList;
    private static final Pattern coordRegex;

    public BetterChat() {
        super(Categories.Misc, "better-chat", "Improves your chat experience in various ways.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgFilter = this.settings.createGroup("Filter");
        this.sgLongerChat = this.settings.createGroup("Longer Chat");
        this.sgPrefix = this.settings.createGroup("Prefix");
        this.sgSuffix = this.settings.createGroup("Suffix");
        this.annoy = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("annoy")).description("Makes your messages aNnOyInG.")).defaultValue(false)).build());
        this.fancy = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fancy-chat")).description("Makes your messages \u0493\u1d00\u0274\u1d04\u028f!")).defaultValue(false)).build());
        this.timestamps = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("timestamps")).description("Adds client-side time stamps to the beginning of chat messages.")).defaultValue(false)).build());
        this.playerHeads = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("player-heads")).description("Displays player heads next to their messages.")).defaultValue(true)).build());
        this.coordsProtection = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("coords-protection")).description("Prevents you from sending messages in chat that may contain coordinates.")).defaultValue(true)).build());
        this.keepHistory = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("keep-history")).description("Prevents the chat history from being cleared when disconnecting.")).defaultValue(true)).build());
        this.highlightNearby = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-nearby")).description("Highlights a message when a player in visual range sends it.")).defaultValue(true)).build());
        this.dingNearby = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ding-nearby")).description("Plays a tone when when a player in visual range sends a message.")).defaultValue(true)).build());
        this.antiSpam = this.sgFilter.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-spam")).description("Blocks duplicate messages from filling your chat.")).defaultValue(true)).build());
        this.antiSpamDepth = this.sgFilter.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("depth")).description("How many messages to filter.")).defaultValue(20)).min(1).sliderMin(1).visible(this.antiSpam::get)).build());
        this.antiClear = this.sgFilter.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-clear")).description("Prevents servers from clearing chat.")).defaultValue(true)).build());
        this.filterRegex = this.sgFilter.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("filter-regex")).description("Filter out chat messages that match the regex filter.")).defaultValue(false)).build());
        this.regexFilters = this.sgFilter.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("regex-filter")).description("Regex filter used for filtering chat messages.")).visible(this.filterRegex::get)).onChanged(strings -> this.compileFilterRegexList())).build());
        this.infiniteChatBox = this.sgLongerChat.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("infinite-chat-box")).description("Lets you type infinitely long messages.")).defaultValue(true)).build());
        this.longerChatHistory = this.sgLongerChat.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("longer-chat-history")).description("Extends chat length.")).defaultValue(true)).build());
        this.longerChatLines = this.sgLongerChat.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("extra-lines")).description("The amount of extra chat lines.")).defaultValue(1000)).min(0).sliderRange(0, 1000).visible(this.longerChatHistory::get)).build());
        this.prefix = this.sgPrefix.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("prefix")).description("Adds a prefix to your chat messages.")).defaultValue(false)).build());
        this.prefixRandom = this.sgPrefix.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("random")).description("Uses a random number as your prefix.")).defaultValue(false)).build());
        this.prefixText = this.sgPrefix.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("text")).description("The text to add as your prefix.")).defaultValue("> ")).visible(() -> this.prefixRandom.get() == false)).build());
        this.prefixSmallCaps = this.sgPrefix.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("small-caps")).description("Uses small caps in the prefix.")).defaultValue(false)).visible(() -> this.prefixRandom.get() == false)).build());
        this.suffix = this.sgSuffix.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("suffix")).description("Adds a suffix to your chat messages.")).defaultValue(false)).build());
        this.suffixRandom = this.sgSuffix.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("random")).description("Uses a random number as your suffix.")).defaultValue(false)).build());
        this.suffixText = this.sgSuffix.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("text")).description("The text to add as your suffix.")).defaultValue(" | meteor on crack!")).visible(() -> this.suffixRandom.get() == false)).build());
        this.suffixSmallCaps = this.sgSuffix.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("small-caps")).description("Uses small caps in the suffix.")).defaultValue(true)).visible(() -> this.suffixRandom.get() == false)).build());
        this.SMALL_CAPS = new Char2CharOpenHashMap();
        this.dateFormat = new SimpleDateFormat("HH:mm");
        this.lines = new IntArrayList();
        this.filterRegexList = new ArrayList<Pattern>();
        String[] a = "abcdefghijklmnopqrstuvwxyz".split("");
        String[] b = "\u1d00\u0299\u1d04\u1d05\u1d07\ua730\u0262\u029c\u026a\u1d0a\u1d0b\u029f\u1d0d\u0274\u1d0f\u1d29q\u0280\ua731\u1d1b\u1d1c\u1d20\u1d21xy\u1d22".split("");
        for (int i = 0; i < a.length; ++i) {
            this.SMALL_CAPS.put(a[i].charAt(0), b[i].charAt(0));
        }
        this.compileFilterRegexList();
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        Matcher usernameMatcher;
        Text antiSpammed;
        String messageString;
        Text message = event.getMessage();
        if (this.filterRegex.get().booleanValue()) {
            messageString = message.getString();
            for (Pattern pattern : this.filterRegexList) {
                if (!pattern.matcher(messageString).find()) continue;
                event.cancel();
                return;
            }
        }
        if (this.antiClear.get().booleanValue() && antiClearRegex.matcher(messageString = message.getString()).find()) {
            MutableText newMessage = Text.empty();
            TextVisitor.visit(message, (text, style, string) -> {
                Matcher antiClearMatcher = antiClearRegex.matcher(string);
                if (antiClearMatcher.find()) {
                    newMessage.append((Text)Text.literal((String)antiClearMatcher.replaceAll("\n\n")).setStyle(style));
                } else {
                    newMessage.append((Text)text.copyContentOnly().setStyle(style));
                }
                return Optional.empty();
            }, Style.EMPTY);
            message = newMessage;
        }
        if (this.antiSpam.get().booleanValue() && (antiSpammed = this.appendAntiSpam(message)) != null) {
            message = antiSpammed;
        }
        if (this.timestamps.get().booleanValue()) {
            MutableText timestamp = Text.literal((String)("<" + this.dateFormat.format(new Date()) + "> ")).formatted(Formatting.GRAY);
            message = Text.empty().append((Text)timestamp).append(message);
        }
        if (this.highlightNearby.get().booleanValue() && (usernameMatcher = usernameRegex.matcher(message.getString())).matches()) {
            PlayerEntity sender;
            String username = usernameMatcher.group(1);
            PlayerListEntry entry = this.mc.getNetworkHandler().getPlayerListEntry(username);
            if (entry != null && (sender = this.mc.world.getPlayerByUuid(entry.getProfile().getId())) != null && !sender.equals((Object)this.mc.player)) {
                message = Text.empty().append(message).formatted(Formatting.AQUA);
                if (this.dingNearby.get().booleanValue()) {
                    this.mc.world.playSoundFromEntity((PlayerEntity)this.mc.player, (Entity)this.mc.player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 3.0f, 1.0f);
                }
            }
        }
        event.setMessage(message);
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        Object message = event.message;
        if (this.annoy.get().booleanValue()) {
            message = this.applyAnnoy((String)message);
        }
        if (this.fancy.get().booleanValue()) {
            message = this.applyFancy((String)message);
        }
        message = this.getPrefix() + (String)message + this.getSuffix();
        if (this.coordsProtection.get().booleanValue() && this.containsCoordinates((String)message)) {
            MutableText warningMessage = Text.literal((String)"It looks like there are coordinates in your message! ");
            MutableText sendButton = this.getSendButton((String)message);
            warningMessage.append((Text)sendButton);
            ChatUtils.sendMsg((Text)warningMessage);
            event.cancel();
            return;
        }
        event.message = message;
    }

    private Text appendAntiSpam(Text text) {
        String textString = text.getString();
        MutableText returnText = null;
        int messageIndex = -1;
        List<ChatHudLine> messages = ((ChatHudAccessor)this.mc.inGameHud.getChatHud()).getMessages();
        if (messages.isEmpty()) {
            return null;
        }
        for (int i = 0; i < Math.min(this.antiSpamDepth.get(), messages.size()); ++i) {
            String stringToCheck = messages.get(i).comp_893().getString();
            Matcher timestampMatcher = timestampRegex.matcher(stringToCheck);
            if (timestampMatcher.find()) {
                stringToCheck = stringToCheck.substring(8);
            }
            if (textString.equals(stringToCheck)) {
                messageIndex = i;
                returnText = text.copy().append((Text)Text.literal((String)" (2)").formatted(Formatting.GRAY));
                break;
            }
            Matcher matcher = antiSpamRegex.matcher(stringToCheck);
            if (!matcher.find()) continue;
            String group = matcher.group(matcher.groupCount());
            int number = Integer.parseInt(group);
            if (!stringToCheck.substring(0, matcher.start()).equals(textString)) continue;
            messageIndex = i;
            returnText = text.copy().append((Text)Text.literal((String)(" (" + (number + 1) + ")")).formatted(Formatting.GRAY));
            break;
        }
        if (returnText != null) {
            int i;
            List<ChatHudLine.Visible> visible = ((ChatHudAccessor)this.mc.inGameHud.getChatHud()).getVisibleMessages();
            int start = -1;
            for (i = 0; i < messageIndex; ++i) {
                start += this.lines.getInt(i);
            }
            for (i = this.lines.getInt(messageIndex); i > 0; --i) {
                visible.remove(start + 1);
            }
            messages.remove(messageIndex);
            this.lines.removeInt(messageIndex);
        }
        return returnText;
    }

    public void removeLine(int index) {
        if (index >= this.lines.size()) {
            if (this.antiSpam.get().booleanValue()) {
                this.error("Issue detected with the anti-spam system! Likely a compatibility issue with another mod. Disabling anti-spam to protect chat integrity.", new Object[0]);
                this.antiSpam.set(false);
            }
            return;
        }
        this.lines.removeInt(index);
    }

    public static void registerCustomHead(String prefix, Identifier texture) {
        CUSTOM_HEAD_ENTRIES.add(new CustomHeadEntry(prefix, texture));
    }

    public int modifyChatWidth(int width) {
        if (this.isActive() && this.playerHeads.get().booleanValue()) {
            return width + 10;
        }
        return width;
    }

    public void drawPlayerHead(DrawContext context, ChatHudLine.Visible line, int y, int color) {
        if (!this.isActive() || !this.playerHeads.get().booleanValue()) {
            return;
        }
        if (((IChatHudLineVisible)line).meteor$isStartOfEntry()) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)((float)Color.toRGBAA(color) / 255.0f));
            this.drawTexture(context, (IChatHudLine)line, y);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.disableBlend();
        }
        if (!((IChatHudLine)line).meteor$getText().trim().startsWith("[GWare]")) {
            context.getMatrices().translate(10.0f, 0.0f, 0.0f);
        }
    }

    private void drawTexture(DrawContext context, IChatHudLine line, int y) {
        CustomHeadEntry entry2;
        String text = line.meteor$getText().trim();
        int startOffset = 0;
        try {
            Matcher m = TIMESTAMP_REGEX.matcher(text);
            if (m.find()) {
                startOffset = m.end() + 1;
            }
        }
        catch (IllegalStateException m) {
            // empty catch block
        }
        for (CustomHeadEntry entry2 : CUSTOM_HEAD_ENTRIES) {
            if (!text.startsWith(entry2.prefix(), startOffset)) continue;
            context.drawTexture(entry2.texture(), 0, y, 8, 8, 0.0f, 0.0f, 64, 64, 64, 64);
            return;
        }
        GameProfile sender = this.getSender(line, text);
        if (sender == null) {
            return;
        }
        entry2 = this.mc.getNetworkHandler().getPlayerListEntry(sender.getId());
        if (entry2 == null) {
            return;
        }
        Identifier skin = entry2.getSkinTextures().comp_1626();
        context.drawTexture(skin, 0, y, 8, 8, 8.0f, 8.0f, 8, 8, 64, 64);
        context.drawTexture(skin, 0, y, 8, 8, 40.0f, 8.0f, 8, 8, 64, 64);
    }

    private GameProfile getSender(IChatHudLine line, String text) {
        Matcher usernameMatcher;
        GameProfile sender = line.meteor$getSender();
        if (sender == null && (usernameMatcher = usernameRegex.matcher(text)).matches()) {
            String username = usernameMatcher.group(1);
            PlayerListEntry entry = this.mc.getNetworkHandler().getPlayerListEntry(username);
            if (entry != null) {
                sender = entry.getProfile();
            }
        }
        return sender;
    }

    private String applyAnnoy(String message) {
        StringBuilder sb = new StringBuilder(message.length());
        boolean upperCase = true;
        for (int cp : message.codePoints().toArray()) {
            if (upperCase) {
                sb.appendCodePoint(Character.toUpperCase(cp));
            } else {
                sb.appendCodePoint(Character.toLowerCase(cp));
            }
            upperCase = !upperCase;
        }
        message = sb.toString();
        return message;
    }

    private String applyFancy(String message) {
        StringBuilder sb = new StringBuilder();
        for (char ch : message.toCharArray()) {
            sb.append(this.SMALL_CAPS.getOrDefault(ch, ch));
        }
        return sb.toString();
    }

    private void compileFilterRegexList() {
        this.filterRegexList.clear();
        for (int i = 0; i < this.regexFilters.get().size(); ++i) {
            try {
                this.filterRegexList.add(Pattern.compile(this.regexFilters.get().get(i)));
                continue;
            }
            catch (PatternSyntaxException e) {
                String removed = this.regexFilters.get().remove(i);
                this.error("Removing Invalid regex: %s", removed);
            }
        }
    }

    private String getPrefix() {
        return this.prefix.get() != false ? this.getAffix(this.prefixText.get(), this.prefixSmallCaps.get(), this.prefixRandom.get()) : "";
    }

    private String getSuffix() {
        return this.suffix.get() != false ? this.getAffix(this.suffixText.get(), this.suffixSmallCaps.get(), this.suffixRandom.get()) : "";
    }

    private String getAffix(String text, boolean smallcaps, boolean random) {
        if (random) {
            return String.format("(%03d) ", Utils.random(0, 1000));
        }
        if (smallcaps) {
            return this.applyFancy(text);
        }
        return text;
    }

    private boolean containsCoordinates(String message) {
        return coordRegex.matcher(message).find();
    }

    private MutableText getSendButton(String message) {
        MutableText sendButton = Text.literal((String)"[SEND ANYWAY]");
        MutableText hintBaseText = Text.literal((String)"");
        MutableText hintMsg = Text.literal((String)"Send your message to the global chat even if there are coordinates:");
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append((Text)hintMsg);
        hintBaseText.append((Text)Text.literal((String)("\n" + message)));
        sendButton.setStyle(sendButton.getStyle().withFormatting(Formatting.DARK_RED).withClickEvent((ClickEvent)new MeteorClickEvent(ClickEvent.Action.RUN_COMMAND, Commands.get("say").toString(message))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)hintBaseText)));
        return sendButton;
    }

    public boolean isInfiniteChatBox() {
        return this.isActive() && this.infiniteChatBox.get() != false;
    }

    public boolean isLongerChat() {
        return this.isActive() && this.longerChatHistory.get() != false;
    }

    public boolean keepHistory() {
        return this.isActive() && this.keepHistory.get() != false;
    }

    public int getExtraChatLines() {
        return this.longerChatLines.get();
    }

    static {
        BetterChat.registerCustomHead("[Baritone]", MeteorClient.identifier("textures/icons/chat/baritone.png"));
        coordRegex = Pattern.compile("(?<x>-?\\d{3,}(?:\\.\\d*)?)(?:\\s+(?<y>-?\\d{1,3}(?:\\.\\d*)?))?\\s+(?<z>-?\\d{3,}(?:\\.\\d*)?)");
    }

    private record CustomHeadEntry(String prefix, Identifier texture) {
    }
}

