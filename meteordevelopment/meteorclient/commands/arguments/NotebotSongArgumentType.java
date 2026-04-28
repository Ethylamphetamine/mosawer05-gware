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
 */
package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoders;
import net.minecraft.command.CommandSource;

public class NotebotSongArgumentType
implements ArgumentType<Path> {
    private static final NotebotSongArgumentType INSTANCE = new NotebotSongArgumentType();

    public static NotebotSongArgumentType create() {
        return INSTANCE;
    }

    private NotebotSongArgumentType() {
    }

    public Path parse(StringReader reader) throws CommandSyntaxException {
        String text = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return MeteorClient.FOLDER.toPath().resolve("notebot/" + text);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CompletableFuture completableFuture;
        block8: {
            Stream<Path> suggestions = Files.list(MeteorClient.FOLDER.toPath().resolve("notebot"));
            try {
                completableFuture = CommandSource.suggestMatching(suggestions.filter(SongDecoders::hasDecoder).map(path -> path.getFileName().toString()), (SuggestionsBuilder)builder);
                if (suggestions == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (suggestions != null) {
                        try {
                            suggestions.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    return Suggestions.empty();
                }
            }
            suggestions.close();
        }
        return completableFuture;
    }
}

