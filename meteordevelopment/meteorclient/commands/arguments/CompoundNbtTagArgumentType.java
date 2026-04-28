/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.StringNbtReader
 */
package meteordevelopment.meteorclient.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

public class CompoundNbtTagArgumentType
implements ArgumentType<NbtCompound> {
    private static final CompoundNbtTagArgumentType INSTANCE = new CompoundNbtTagArgumentType();
    private static final Collection<String> EXAMPLES = List.of("{foo:bar}", "{foo:[aa, bb],bar:15}");

    public static CompoundNbtTagArgumentType create() {
        return INSTANCE;
    }

    public static NbtCompound get(CommandContext<?> context) {
        return (NbtCompound)context.getArgument("nbt", NbtCompound.class);
    }

    private CompoundNbtTagArgumentType() {
    }

    public NbtCompound parse(StringReader reader) throws CommandSyntaxException {
        reader.skipWhitespace();
        if (!reader.canRead()) {
            throw StringNbtReader.EXPECTED_VALUE.createWithContext((ImmutableStringReader)reader);
        }
        StringBuilder b = new StringBuilder();
        int open = 0;
        while (reader.canRead()) {
            if (reader.peek() == '{') {
                ++open;
            } else if (reader.peek() == '}') {
                --open;
            }
            if (open == 0) break;
            b.append(reader.read());
        }
        reader.expect('}');
        b.append('}');
        return StringNbtReader.parse((String)b.toString().replace("$", "\u00a7").replace("\u00a7\u00a7", "$"));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

