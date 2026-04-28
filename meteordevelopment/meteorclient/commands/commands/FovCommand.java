/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixininterface.ISimpleOption;
import net.minecraft.command.CommandSource;

public class FovCommand
extends Command {
    public FovCommand() {
        super("fov", "Changes your fov.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(FovCommand.argument("fov", IntegerArgumentType.integer((int)0, (int)180)).executes(context -> {
            ((ISimpleOption)FovCommand.mc.options.getFov()).set(context.getArgument("fov", Integer.class));
            return 1;
        }));
    }
}

