/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.command.CommandSource
 *  net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DisconnectCommand
extends Command {
    public DisconnectCommand() {
        super("disconnect", "Disconnect from the server", "dc");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            DisconnectCommand.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket((Text)Text.literal((String)"%s[%sDisconnectCommand%s] Disconnected by user.".formatted(Formatting.GRAY, Formatting.BLUE, Formatting.GRAY))));
            return 1;
        });
        builder.then(DisconnectCommand.argument("reason", StringArgumentType.greedyString()).executes(context -> {
            DisconnectCommand.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket((Text)Text.literal((String)StringArgumentType.getString((CommandContext)context, (String)"reason"))));
            return 1;
        }));
    }
}

