/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.commands.arguments.SettingArgumentType;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;

public class SettingValueArgumentType
implements ArgumentType<String> {
    private static final SettingValueArgumentType INSTANCE = new SettingValueArgumentType();

    public static SettingValueArgumentType create() {
        return INSTANCE;
    }

    public static String get(CommandContext<?> context) {
        return (String)context.getArgument("value", String.class);
    }

    private SettingValueArgumentType() {
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        String text = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return text;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Setting<?> setting;
        try {
            setting = SettingArgumentType.get(context);
        }
        catch (CommandSyntaxException ignored) {
            return Suggestions.empty();
        }
        Iterable<Identifier> identifiers = setting.getIdentifierSuggestions();
        if (identifiers != null) {
            return CommandSource.suggestIdentifiers(identifiers, (SuggestionsBuilder)builder);
        }
        return CommandSource.suggestMatching(setting.getSuggestions(), (SuggestionsBuilder)builder);
    }
}

