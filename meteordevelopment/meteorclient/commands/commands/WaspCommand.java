/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.command.CommandSource
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWasp;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class WaspCommand
extends Command {
    private static final SimpleCommandExceptionType CANT_WASP_SELF = new SimpleCommandExceptionType((Message)Text.literal((String)"You cannot target yourself!"));

    public WaspCommand() {
        super("wasp", "Sets the auto wasp target.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        AutoWasp wasp = Modules.get().get(AutoWasp.class);
        builder.then(WaspCommand.literal("reset").executes(context -> {
            if (wasp.isActive()) {
                wasp.toggle();
            }
            return 1;
        }));
        builder.then(WaspCommand.argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity player = PlayerArgumentType.get(context);
            if (player == WaspCommand.mc.player) {
                throw CANT_WASP_SELF.create();
            }
            wasp.target = player;
            if (!wasp.isActive()) {
                wasp.toggle();
            }
            this.info(player.getName().getString() + " set as target.", new Object[0]);
            return 1;
        }));
    }
}

