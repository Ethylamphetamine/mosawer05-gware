/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.command.CommandSource
 *  net.minecraft.network.encryption.NetworkEncryptionUtils$SecureRandomUtil
 *  net.minecraft.network.message.LastSeenMessagesCollector$LastSeenMessages
 *  net.minecraft.network.message.MessageBody
 *  net.minecraft.network.message.MessageSignatureData
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.time.Instant;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.Script;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class SayCommand
extends Command {
    public SayCommand() {
        super("say", "Sends messages in chat.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(SayCommand.argument("message", StringArgumentType.greedyString()).executes(context -> {
            String message;
            String msg = (String)context.getArgument("message", String.class);
            Script script = MeteorStarscript.compile(msg);
            if (script != null && (message = MeteorStarscript.run(script)) != null) {
                Instant instant = Instant.now();
                long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                ClientPlayNetworkHandler handler = mc.getNetworkHandler();
                LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = ((ClientPlayNetworkHandlerAccessor)handler).getLastSeenMessagesCollector().collect();
                MessageSignatureData messageSignatureData = ((ClientPlayNetworkHandlerAccessor)handler).getMessagePacker().pack(new MessageBody(message, instant, l, lastSeenMessages.comp_1073()));
                handler.sendPacket((Packet)new ChatMessageC2SPacket(message, instant, l, messageSignatureData, lastSeenMessages.comp_1074()));
            }
            return 1;
        }));
    }
}

