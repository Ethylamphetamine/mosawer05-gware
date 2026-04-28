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
 *  net.minecraft.entity.player.PlayerEntity
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
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PlayerArgumentType
implements ArgumentType<PlayerEntity> {
    private static final PlayerArgumentType INSTANCE = new PlayerArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_PLAYER = new DynamicCommandExceptionType(name -> Text.literal((String)("Player with name " + String.valueOf(name) + " doesn't exist.")));
    private static final Collection<String> EXAMPLES = List.of("seasnail8169", "MineGame159");

    public static PlayerArgumentType create() {
        return INSTANCE;
    }

    public static PlayerEntity get(CommandContext<?> context) {
        return (PlayerEntity)context.getArgument("player", PlayerEntity.class);
    }

    private PlayerArgumentType() {
    }

    public PlayerEntity parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        PlayerEntity playerEntity = null;
        for (PlayerEntity p : MeteorClient.mc.world.getPlayers()) {
            if (!p.getName().getString().equalsIgnoreCase(argument)) continue;
            playerEntity = p;
            break;
        }
        if (playerEntity == null) {
            throw NO_SUCH_PLAYER.create((Object)argument);
        }
        return playerEntity;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(MeteorClient.mc.world.getPlayers().stream().map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getName().getString()), (SuggestionsBuilder)builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

