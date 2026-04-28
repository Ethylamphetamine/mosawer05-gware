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
 *  net.minecraft.client.network.PlayerListEntry
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class PlayerListEntryArgumentType
implements ArgumentType<PlayerListEntry> {
    private static final PlayerListEntryArgumentType INSTANCE = new PlayerListEntryArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(name -> Text.literal((String)("Player list entry with name " + String.valueOf(name) + " doesn't exist.")));
    private static final Collection<String> EXAMPLES = List.of("seasnail8169", "MineGame159");

    public static PlayerListEntryArgumentType create() {
        return INSTANCE;
    }

    public static PlayerListEntry get(CommandContext<?> context) {
        return (PlayerListEntry)context.getArgument("player", PlayerListEntry.class);
    }

    private PlayerListEntryArgumentType() {
    }

    public PlayerListEntry parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        PlayerListEntry playerListEntry = null;
        for (PlayerListEntry p : MeteorClient.mc.getNetworkHandler().getPlayerList()) {
            if (!p.getProfile().getName().equalsIgnoreCase(argument)) continue;
            playerListEntry = p;
            break;
        }
        if (playerListEntry == null) {
            throw NO_SUCH_PLAYER.create((Object)argument);
        }
        return playerListEntry;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(MeteorClient.mc.getNetworkHandler().getPlayerList().stream().map(playerListEntry -> playerListEntry.getProfile().getName()), (SuggestionsBuilder)builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

