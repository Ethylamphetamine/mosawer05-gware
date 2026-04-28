/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.macros.Macros;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class MacroArgumentType
implements ArgumentType<Macro> {
    private static final MacroArgumentType INSTANCE = new MacroArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_MACRO = new DynamicCommandExceptionType(name -> Text.literal((String)("Macro with name " + String.valueOf(name) + " doesn't exist.")));

    public static MacroArgumentType create() {
        return INSTANCE;
    }

    public static Macro get(CommandContext<?> context) {
        return (Macro)context.getArgument("macro", Macro.class);
    }

    private MacroArgumentType() {
    }

    public Macro parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Macro macro = Macros.get().get(argument);
        if (macro == null) {
            throw NO_SUCH_MACRO.create((Object)argument);
        }
        return macro;
    }

    public CompletableFuture<Suggestions> listSuggestions(CommandContext context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Macros.get().getAll().stream().map(macro -> macro.name.get()), (SuggestionsBuilder)builder);
    }

    public Collection<String> getExamples() {
        return Macros.get().getAll().stream().limit(3L).map(macro -> macro.name.get()).collect(Collectors.toList());
    }
}

