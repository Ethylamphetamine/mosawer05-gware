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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class WaypointArgumentType
implements ArgumentType<String> {
    private static final WaypointArgumentType GREEDY = new WaypointArgumentType(true);
    private static final WaypointArgumentType QUOTED = new WaypointArgumentType(false);
    private static final DynamicCommandExceptionType NO_SUCH_WAYPOINT = new DynamicCommandExceptionType(name -> Text.literal((String)("Waypoint with name '" + String.valueOf(name) + "' doesn't exist.")));
    private final boolean greedyString;

    private WaypointArgumentType(boolean greedyString) {
        this.greedyString = greedyString;
    }

    public static WaypointArgumentType create() {
        return GREEDY;
    }

    public static WaypointArgumentType create(boolean greedy) {
        return greedy ? GREEDY : QUOTED;
    }

    public static Waypoint get(CommandContext<?> context) {
        return Waypoints.get().get((String)context.getArgument("waypoint", String.class));
    }

    public static Waypoint get(CommandContext<?> context, String name) {
        return Waypoints.get().get((String)context.getArgument(name, String.class));
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        String argument;
        if (this.greedyString) {
            argument = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
        } else {
            argument = reader.readString();
        }
        if (Waypoints.get().get(argument) == null) {
            throw NO_SUCH_WAYPOINT.create((Object)argument);
        }
        return argument;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.getExamples(), (SuggestionsBuilder)builder);
    }

    public Collection<String> getExamples() {
        ArrayList<String> names = new ArrayList<String>();
        for (Waypoint waypoint : Waypoints.get()) {
            names.add(waypoint.name.get());
        }
        return names;
    }
}

