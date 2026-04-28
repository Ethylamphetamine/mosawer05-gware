/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.mojang.brigadier.suggestion.Suggestions
 *  joptsimple.internal.Strings
 *  net.minecraft.client.network.ServerAddress
 *  net.minecraft.client.network.ServerInfo
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket
 *  net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket
 *  net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.resource.featuretoggle.FeatureSet
 *  net.minecraft.server.integrated.IntegratedServer
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import joptsimple.internal.Strings;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

public class ServerCommand
extends Command {
    private static final Set<String> ANTICHEAT_LIST = Set.of("nocheatplus", "negativity", "warden", "horizon", "illegalstack", "coreprotect", "exploitsx", "vulcan", "abc", "spartan", "kauri", "anticheatreloaded", "witherac", "godseye", "matrix", "wraith", "antixrayheuristics", "grimac");
    private static final Set<String> VERSION_ALIASES = Set.of("version", "ver", "about", "bukkit:version", "bukkit:ver", "bukkit:about");
    private String alias;
    private int ticks = 0;
    private boolean tick = false;
    private final List<String> plugins = new ArrayList<String>();
    private final List<String> commandTreePlugins = new ArrayList<String>();
    private static final Random RANDOM = new Random();

    public ServerCommand() {
        super("server", "Prints server information", new String[0]);
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            this.basicInfo();
            return 1;
        });
        builder.then(ServerCommand.literal("info").executes(ctx -> {
            this.basicInfo();
            return 1;
        }));
        builder.then(ServerCommand.literal("plugins").executes(ctx -> {
            this.plugins.addAll(this.commandTreePlugins);
            if (this.alias != null) {
                mc.getNetworkHandler().sendPacket((Packet)new RequestCommandCompletionsC2SPacket(RANDOM.nextInt(200), this.alias + " "));
                this.tick = true;
            } else {
                this.printPlugins();
            }
            return 1;
        }));
        builder.then(ServerCommand.literal("tps").executes(ctx -> {
            float tps = TickRate.INSTANCE.getTickRate();
            Formatting color = tps > 17.0f ? Formatting.GREEN : (tps > 12.0f ? Formatting.YELLOW : Formatting.RED);
            this.info("Current TPS: %s%.2f(default).", color, Float.valueOf(tps));
            return 1;
        }));
    }

    private void basicInfo() {
        MutableText ipText;
        if (mc.isIntegratedServerRunning()) {
            IntegratedServer server = mc.getServer();
            this.info("Singleplayer", new Object[0]);
            if (server != null) {
                this.info("Version: %s", server.getVersion());
            }
            return;
        }
        ServerInfo server = mc.getCurrentServerEntry();
        if (server == null) {
            this.info("Couldn't obtain any server information.", new Object[0]);
            return;
        }
        String ipv4 = "";
        try {
            ipv4 = InetAddress.getByName(server.address).getHostAddress();
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
        if (ipv4.isEmpty()) {
            ipText = Text.literal((String)(String.valueOf(Formatting.GRAY) + server.address));
            ipText.setStyle(ipText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, server.address)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Copy to clipboard"))));
        } else {
            ipText = Text.literal((String)(String.valueOf(Formatting.GRAY) + server.address));
            ipText.setStyle(ipText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, server.address)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Copy to clipboard"))));
            MutableText ipv4Text = Text.literal((String)String.format("%s (%s)", Formatting.GRAY, ipv4));
            ipv4Text.setStyle(ipText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ipv4)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Copy to clipboard"))));
            ipText.append((Text)ipv4Text);
        }
        this.info((Text)Text.literal((String)String.format("%sIP: ", Formatting.GRAY)).append((Text)ipText));
        this.info("Port: %d", ServerAddress.parse((String)server.address).getPort());
        this.info("Type: %s", mc.getNetworkHandler().getBrand() != null ? mc.getNetworkHandler().getBrand() : "unknown");
        this.info("Motd: %s", server.label != null ? server.label.getString() : "unknown");
        this.info("Version: %s", server.version.getString());
        this.info("Protocol version: %d", server.protocolVersion);
        this.info("Difficulty: %s (Local: %.2f)", ServerCommand.mc.world.getDifficulty().getTranslatableName().getString(), Float.valueOf(ServerCommand.mc.world.getLocalDifficulty(ServerCommand.mc.player.getBlockPos()).getLocalDifficulty()));
        this.info("Day: %d", ServerCommand.mc.world.getTimeOfDay() / 24000L);
        this.info("Permission level: %s", this.formatPerms());
    }

    public String formatPerms() {
        int p;
        for (p = 5; !ServerCommand.mc.player.hasPermissionLevel(p) && p > 0; --p) {
        }
        return switch (p) {
            case 0 -> "0 (No Perms)";
            case 1 -> "1 (No Perms)";
            case 2 -> "2 (Player Command Access)";
            case 3 -> "3 (Server Command Access)";
            case 4 -> "4 (Operator)";
            default -> p + " (Unknown)";
        };
    }

    private void printPlugins() {
        this.plugins.sort(String.CASE_INSENSITIVE_ORDER);
        this.plugins.replaceAll(this::formatName);
        if (!this.plugins.isEmpty()) {
            this.info("Plugins (%d): %s ", this.plugins.size(), Strings.join((String[])this.plugins.toArray(new String[0]), (String)", "));
        } else {
            this.error("No plugins found.", new Object[0]);
        }
        this.tick = false;
        this.ticks = 0;
        this.plugins.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.tick) {
            return;
        }
        ++this.ticks;
        if (this.ticks >= 100) {
            this.printPlugins();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (this.tick && event.packet instanceof RequestCommandCompletionsC2SPacket) {
            event.cancel();
        }
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        Object handler;
        CommandTreeS2CPacket packet;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof CommandTreeS2CPacket) {
            packet = (CommandTreeS2CPacket)packet2;
            handler = (ClientPlayNetworkHandlerAccessor)event.connection.getPacketListener();
            this.commandTreePlugins.clear();
            this.alias = null;
            packet.getCommandTree(CommandRegistryAccess.of((RegistryWrapper.WrapperLookup)handler.getCombinedDynamicRegistries(), (FeatureSet)handler.getEnabledFeatures())).getChildren().forEach(node -> {
                String[] split = node.getName().split(":");
                if (split.length > 1 && !this.commandTreePlugins.contains(split[0])) {
                    this.commandTreePlugins.add(split[0]);
                }
                if (this.alias == null && VERSION_ALIASES.contains(node.getName())) {
                    this.alias = node.getName();
                }
            });
        }
        if (!this.tick) {
            return;
        }
        try {
            handler = event.packet;
            if (handler instanceof CommandSuggestionsS2CPacket) {
                packet = (CommandSuggestionsS2CPacket)handler;
                Suggestions matches = packet.getSuggestions();
                if (matches.isEmpty()) {
                    this.error("An error occurred while trying to find plugins.", new Object[0]);
                    return;
                }
                for (Suggestion suggestion : matches.getList()) {
                    String pluginName = suggestion.getText();
                    if (this.plugins.contains(pluginName.toLowerCase())) continue;
                    this.plugins.add(pluginName);
                }
                this.printPlugins();
            }
        }
        catch (Exception e) {
            this.error("An error occurred while trying to find plugins.", new Object[0]);
        }
    }

    private String formatName(String name) {
        if (ANTICHEAT_LIST.contains(name.toLowerCase())) {
            return String.format("%s%s(default)", Formatting.RED, name);
        }
        if (StringUtils.containsIgnoreCase((CharSequence)name, (CharSequence)"exploit") || StringUtils.containsIgnoreCase((CharSequence)name, (CharSequence)"cheat") || StringUtils.containsIgnoreCase((CharSequence)name, (CharSequence)"illegal")) {
            return String.format("%s%s(default)", Formatting.RED, name);
        }
        return String.format("(highlight)%s(default)", name);
    }
}

