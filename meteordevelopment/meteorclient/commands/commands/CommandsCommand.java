/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandsCommand
extends Command {
    public CommandsCommand() {
        super("commands", "List of all commands.", "help");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.info("--- Commands ((highlight)%d(default)) ---", Commands.COMMANDS.size());
            MutableText commands = Text.literal((String)"");
            Commands.COMMANDS.forEach(command -> commands.append((Text)this.getCommandText((Command)command)));
            ChatUtils.sendMsg((Text)commands);
            return 1;
        });
    }

    private MutableText getCommandText(Command command) {
        MutableText tooltip = Text.literal((String)"");
        tooltip.append((Text)Text.literal((String)Utils.nameToTitle(command.getName())).formatted(new Formatting[]{Formatting.BLUE, Formatting.BOLD})).append("\n");
        MutableText aliases = Text.literal((String)(Config.get().prefix.get() + command.getName()));
        if (!command.getAliases().isEmpty()) {
            aliases.append(", ");
            for (String alias : command.getAliases()) {
                if (alias.isEmpty()) continue;
                aliases.append(Config.get().prefix.get() + alias);
                if (alias.equals(command.getAliases().getLast())) continue;
                aliases.append(", ");
            }
        }
        tooltip.append((Text)aliases.formatted(Formatting.GRAY)).append("\n\n");
        tooltip.append((Text)Text.literal((String)command.getDescription()).formatted(Formatting.WHITE));
        MutableText text = Text.literal((String)Utils.nameToTitle(command.getName()));
        if (command != Commands.COMMANDS.getLast()) {
            text.append((Text)Text.literal((String)", ").formatted(Formatting.GRAY));
        }
        text.setStyle(text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)tooltip)).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, Config.get().prefix.get() + command.getName())));
        return text;
    }
}

