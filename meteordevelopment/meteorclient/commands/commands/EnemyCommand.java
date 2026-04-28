/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.FriendArgumentType;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

public class EnemyCommand
extends Command {
    public EnemyCommand() {
        super("enemy", "Manages enemies.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(EnemyCommand.literal("add").then(EnemyCommand.argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile profile = PlayerListEntryArgumentType.get(context).getProfile();
            Friend friend = new Friend(profile.getName(), profile.getId(), Friend.FriendType.Enemy);
            if (Friends.get().add(friend)) {
                ChatUtils.sendMsg(friend.hashCode(), Formatting.GRAY, "Added (highlight)%s (default)to enemies.".formatted(friend.getName()), new Object[0]);
            } else {
                this.error("Already enemies with that player.", new Object[0]);
            }
            return 1;
        })));
        builder.then(EnemyCommand.literal("remove").then(EnemyCommand.argument("friend", FriendArgumentType.create()).executes(context -> {
            Friend friend = FriendArgumentType.get(context);
            if (friend == null) {
                this.error("Not friends with that enemy.", new Object[0]);
                return 1;
            }
            if (Friends.get().remove(friend)) {
                ChatUtils.sendMsg(friend.hashCode(), Formatting.GRAY, "Removed (highlight)%s (default)from enemies.".formatted(friend.getName()), new Object[0]);
            } else {
                this.error("Failed to remove that enemy.", new Object[0]);
            }
            return 1;
        })));
        builder.then(EnemyCommand.literal("list").executes(context -> {
            this.info("--- Enemies ((highlight)%s(default)) ---", Friends.get().count());
            Friends.get().friendStream().forEach(friend -> ChatUtils.info("(highlight)%s".formatted(friend.getName()), new Object[0]));
            return 1;
        }));
    }
}

