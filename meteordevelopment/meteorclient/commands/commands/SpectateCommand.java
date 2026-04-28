/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.entity.Entity
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

public class SpectateCommand
extends Command {
    private final StaticListener shiftListener = new StaticListener();

    public SpectateCommand() {
        super("spectate", "Allows you to spectate nearby players", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(SpectateCommand.literal("reset").executes(context -> {
            mc.setCameraEntity((Entity)SpectateCommand.mc.player);
            return 1;
        }));
        builder.then(SpectateCommand.argument("player", PlayerArgumentType.create()).executes(context -> {
            mc.setCameraEntity((Entity)PlayerArgumentType.get(context));
            SpectateCommand.mc.player.sendMessage((Text)Text.literal((String)"Sneak to un-spectate."), true);
            MeteorClient.EVENT_BUS.subscribe(this.shiftListener);
            return 1;
        }));
    }

    private static class StaticListener {
        private StaticListener() {
        }

        @EventHandler
        private void onKey(KeyEvent event) {
            if (mc.options.sneakKey.matchesKey(event.key, 0) || mc.options.sneakKey.matchesMouse(event.key)) {
                mc.setCameraEntity((Entity)mc.player);
                event.cancel();
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }
        }
    }
}

