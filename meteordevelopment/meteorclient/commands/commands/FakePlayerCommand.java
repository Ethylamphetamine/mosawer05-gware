/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.FakePlayerArgumentType;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

public class FakePlayerCommand
extends Command {
    public FakePlayerCommand() {
        super("fake-player", "Manages fake players that you can use for testing.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(((LiteralArgumentBuilder)FakePlayerCommand.literal("add").executes(context -> {
            FakePlayer fakePlayer = Modules.get().get(FakePlayer.class);
            FakePlayerManager.add(fakePlayer.name.get(), fakePlayer.health.get().intValue(), fakePlayer.copyInv.get());
            return 1;
        })).then(FakePlayerCommand.argument("name", StringArgumentType.word()).executes(context -> {
            FakePlayer fakePlayer = Modules.get().get(FakePlayer.class);
            FakePlayerManager.add(StringArgumentType.getString((CommandContext)context, (String)"name"), fakePlayer.health.get().intValue(), fakePlayer.copyInv.get());
            return 1;
        })));
        builder.then(FakePlayerCommand.literal("remove").then(FakePlayerCommand.argument("fp", FakePlayerArgumentType.create()).executes(context -> {
            FakePlayerEntity fp = FakePlayerArgumentType.get(context);
            if (fp == null || !FakePlayerManager.contains(fp)) {
                this.error("Couldn't find a Fake Player with that name.", new Object[0]);
                return 1;
            }
            FakePlayerManager.remove(fp);
            this.info("Removed Fake Player %s.".formatted(fp.getName().getString()), new Object[0]);
            return 1;
        })));
        builder.then(FakePlayerCommand.literal("clear").executes(context -> {
            FakePlayerManager.clear();
            return 1;
        }));
        builder.then(FakePlayerCommand.literal("list").executes(context -> {
            this.info("--- Fake Players ((highlight)%s(default)) ---", FakePlayerManager.count());
            FakePlayerManager.forEach(fp -> ChatUtils.info("(highlight)%s".formatted(fp.getName().getString()), new Object[0]));
            return 1;
        }));
    }
}

