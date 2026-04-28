/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.rekit.RekitSystem;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

public class SaveKitCommand
extends Command {
    public SaveKitCommand() {
        super("save", "Saves your current inventory layout into a kit (1-24).", "savekit");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(SaveKitCommand.argument("id", IntegerArgumentType.integer((int)1, (int)24)).executes(ctx -> {
            int id = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"id");
            RekitSystem.get().saveKit(id);
            ChatUtils.info("Saved kit (highlight)%d(default).", id);
            return 1;
        }));
    }
}

