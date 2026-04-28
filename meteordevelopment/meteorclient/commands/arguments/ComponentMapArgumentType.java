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
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.component.ComponentMap
 */
package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.utils.misc.ComponentMapReader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentMap;

public class ComponentMapArgumentType
implements ArgumentType<ComponentMap> {
    private static final Collection<String> EXAMPLES = List.of("{foo=bar}");
    private final ComponentMapReader reader;

    public ComponentMapArgumentType(CommandRegistryAccess commandRegistryAccess) {
        this.reader = new ComponentMapReader(commandRegistryAccess);
    }

    public static ComponentMapArgumentType componentMap(CommandRegistryAccess commandRegistryAccess) {
        return new ComponentMapArgumentType(commandRegistryAccess);
    }

    public static <S extends CommandSource> ComponentMap getComponentMap(CommandContext<S> context, String name) {
        return (ComponentMap)context.getArgument(name, ComponentMap.class);
    }

    public ComponentMap parse(StringReader reader) throws CommandSyntaxException {
        return this.reader.consume(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return this.reader.getSuggestions(builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

