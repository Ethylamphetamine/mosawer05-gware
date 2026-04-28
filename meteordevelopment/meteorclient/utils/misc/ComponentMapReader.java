/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.component.ComponentMap
 *  net.minecraft.component.ComponentMap$Builder
 *  net.minecraft.component.ComponentType
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtOps
 *  net.minecraft.nbt.StringNbtReader
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.utils.misc;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ComponentMapReader {
    private static final DynamicCommandExceptionType UNKNOWN_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable((String)"arguments.item.component.unknown", (Object[])new Object[]{id}));
    private static final SimpleCommandExceptionType COMPONENT_EXPECTED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable((String)"arguments.item.component.expected"));
    private static final DynamicCommandExceptionType REPEATED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(type -> Text.stringifiedTranslatable((String)"arguments.item.component.repeated", (Object[])new Object[]{type}));
    private static final Dynamic2CommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new Dynamic2CommandExceptionType((type, error) -> Text.stringifiedTranslatable((String)"arguments.item.component.malformed", (Object[])new Object[]{type, error}));
    private final DynamicOps<NbtElement> nbtOps;

    public ComponentMapReader(CommandRegistryAccess commandRegistryAccess) {
        this.nbtOps = commandRegistryAccess.getOps((DynamicOps)NbtOps.INSTANCE);
    }

    public ComponentMap consume(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        try {
            return new Reader(reader, this.nbtOps).read();
        }
        catch (CommandSyntaxException e) {
            reader.setCursor(cursor);
            throw e;
        }
    }

    public CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        Reader reader = new Reader(stringReader, this.nbtOps);
        try {
            reader.read();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return reader.suggestor.apply(builder.createOffset(stringReader.getCursor()));
    }

    private static class Reader {
        private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_DEFAULT = SuggestionsBuilder::buildFuture;
        private final StringReader reader;
        private final DynamicOps<NbtElement> nbtOps;
        public Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestor = this::suggestBracket;

        public Reader(StringReader reader, DynamicOps<NbtElement> nbtOps) {
            this.reader = reader;
            this.nbtOps = nbtOps;
        }

        public ComponentMap read() throws CommandSyntaxException {
            ComponentMap.Builder builder = ComponentMap.builder();
            this.reader.expect('[');
            this.suggestor = this::suggestComponentType;
            ReferenceArraySet set = new ReferenceArraySet();
            while (this.reader.canRead() && this.reader.peek() != ']') {
                this.reader.skipWhitespace();
                ComponentType<?> dataComponentType = Reader.readComponentType(this.reader);
                if (!set.add(dataComponentType)) {
                    throw REPEATED_COMPONENT_EXCEPTION.create(dataComponentType);
                }
                this.suggestor = this::suggestEqual;
                this.reader.skipWhitespace();
                this.reader.expect('=');
                this.suggestor = SUGGEST_DEFAULT;
                this.reader.skipWhitespace();
                this.readComponentValue(this.reader, builder, dataComponentType);
                this.reader.skipWhitespace();
                this.suggestor = this::suggestEndOfComponent;
                if (!this.reader.canRead() || this.reader.peek() != ',') break;
                this.reader.skip();
                this.reader.skipWhitespace();
                this.suggestor = this::suggestComponentType;
                if (this.reader.canRead()) continue;
                throw COMPONENT_EXPECTED_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
            }
            this.reader.expect(']');
            this.suggestor = SUGGEST_DEFAULT;
            return builder.build();
        }

        public static ComponentType<?> readComponentType(StringReader reader) throws CommandSyntaxException {
            if (!reader.canRead()) {
                throw COMPONENT_EXPECTED_EXCEPTION.createWithContext((ImmutableStringReader)reader);
            }
            int i = reader.getCursor();
            Identifier identifier = Identifier.fromCommandInput((StringReader)reader);
            ComponentType dataComponentType = (ComponentType)Registries.DATA_COMPONENT_TYPE.get(identifier);
            if (dataComponentType != null && !dataComponentType.shouldSkipSerialization()) {
                return dataComponentType;
            }
            reader.setCursor(i);
            throw UNKNOWN_COMPONENT_EXCEPTION.createWithContext((ImmutableStringReader)reader, (Object)identifier);
        }

        private CompletableFuture<Suggestions> suggestComponentType(SuggestionsBuilder builder) {
            String string = builder.getRemaining().toLowerCase(Locale.ROOT);
            CommandSource.forEachMatching((Iterable)Registries.DATA_COMPONENT_TYPE.getEntrySet(), (String)string, entry -> ((RegistryKey)entry.getKey()).getValue(), entry -> {
                ComponentType dataComponentType = (ComponentType)entry.getValue();
                if (dataComponentType.getCodec() != null) {
                    Identifier identifier = ((RegistryKey)entry.getKey()).getValue();
                    builder.suggest(identifier.toString() + "=");
                }
            });
            return builder.buildFuture();
        }

        private <T> void readComponentValue(StringReader reader, ComponentMap.Builder builder, ComponentType<T> type) throws CommandSyntaxException {
            int i = reader.getCursor();
            NbtElement nbtElement = new StringNbtReader(reader).parseElement();
            DataResult dataResult = type.getCodecOrThrow().parse(this.nbtOps, (Object)nbtElement);
            builder.add(type, dataResult.getOrThrow(error -> {
                reader.setCursor(i);
                return MALFORMED_COMPONENT_EXCEPTION.createWithContext((ImmutableStringReader)reader, (Object)type.toString(), error);
            }));
        }

        private CompletableFuture<Suggestions> suggestBracket(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf('['));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEndOfComponent(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf(','));
                builder.suggest(String.valueOf(']'));
            }
            return builder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestEqual(SuggestionsBuilder builder) {
            if (builder.getRemaining().isEmpty()) {
                builder.suggest(String.valueOf('='));
            }
            return builder.buildFuture();
        }
    }
}

