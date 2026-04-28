/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class SettingArgumentType
implements ArgumentType<String> {
    private static final SettingArgumentType INSTANCE = new SettingArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_SETTING = new DynamicCommandExceptionType(name -> Text.literal((String)("No such setting '" + String.valueOf(name) + "'.")));

    public static SettingArgumentType create() {
        return INSTANCE;
    }

    public static Setting<?> get(CommandContext<?> context) throws CommandSyntaxException {
        Module module = (Module)context.getArgument("module", Module.class);
        String settingName = (String)context.getArgument("setting", String.class);
        Setting<?> setting = module.settings.get(settingName);
        if (setting == null) {
            throw NO_SUCH_SETTING.create((Object)settingName);
        }
        return setting;
    }

    private SettingArgumentType() {
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Stream<String> stream = Streams.stream(((Module)context.getArgument((String)"module", Module.class)).settings.iterator()).flatMap(settings -> Streams.stream(settings.iterator())).map(setting -> setting.name);
        return CommandSource.suggestMatching(stream, (SuggestionsBuilder)builder);
    }
}

