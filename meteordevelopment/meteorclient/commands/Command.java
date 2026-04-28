/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.registry.BuiltinRegistries
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.server.command.CommandManager
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public abstract class Command {
    protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess((RegistryWrapper.WrapperLookup)BuiltinRegistries.createWrapperLookup());
    protected static final int SINGLE_SUCCESS = 1;
    protected static final MinecraftClient mc = MeteorClient.mc;
    private final String name;
    private final String title;
    private final String description;
    private final List<String> aliases;

    public Command(String name, String description, String ... aliases) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.aliases = List.of(aliases);
    }

    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument((String)name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal((String)name);
    }

    public final void registerTo(CommandDispatcher<CommandSource> dispatcher) {
        this.register(dispatcher, this.name);
        for (String alias : this.aliases) {
            this.register(dispatcher, alias);
        }
    }

    public void register(CommandDispatcher<CommandSource> dispatcher, String name) {
        LiteralArgumentBuilder builder = LiteralArgumentBuilder.literal((String)name);
        this.build((LiteralArgumentBuilder<CommandSource>)builder);
        dispatcher.register(builder);
    }

    public abstract void build(LiteralArgumentBuilder<CommandSource> var1);

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public String toString() {
        return Config.get().prefix.get() + this.name;
    }

    public String toString(String ... args) {
        StringBuilder base = new StringBuilder(this.toString());
        for (String arg : args) {
            base.append(' ').append(arg);
        }
        return base.toString();
    }

    public void info(Text message) {
        ChatUtils.forceNextPrefixClass(this.getClass());
        ChatUtils.sendMsg(this.title, message);
    }

    public void info(String message, Object ... args) {
        ChatUtils.forceNextPrefixClass(this.getClass());
        ChatUtils.infoPrefix(this.title, message, args);
    }

    public void warning(String message, Object ... args) {
        ChatUtils.forceNextPrefixClass(this.getClass());
        ChatUtils.warningPrefix(this.title, message, args);
    }

    public void error(String message, Object ... args) {
        ChatUtils.forceNextPrefixClass(this.getClass());
        ChatUtils.errorPrefix(this.title, message, args);
    }
}

