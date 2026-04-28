/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BindsCommand
extends Command {
    public BindsCommand() {
        super("binds", "List of all bound modules.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            List<Module> modules = Modules.get().getAll().stream().filter(module -> module.keybind.isSet()).toList();
            ChatUtils.info("--- Bound Modules ((highlight)%d(default)) ---", modules.size());
            for (Module module2 : modules) {
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)this.getTooltip(module2));
                MutableText text = Text.literal((String)module2.title).formatted(Formatting.WHITE);
                text.setStyle(text.getStyle().withHoverEvent(hoverEvent));
                MutableText sep = Text.literal((String)" - ");
                sep.setStyle(sep.getStyle().withHoverEvent(hoverEvent));
                text.append((Text)sep.formatted(Formatting.GRAY));
                MutableText key = Text.literal((String)module2.keybind.toString());
                key.setStyle(key.getStyle().withHoverEvent(hoverEvent));
                text.append((Text)key.formatted(Formatting.GRAY));
                ChatUtils.sendMsg((Text)text);
            }
            return 1;
        });
    }

    private MutableText getTooltip(Module module) {
        MutableText tooltip = Text.literal((String)Utils.nameToTitle(module.title)).formatted(new Formatting[]{Formatting.BLUE, Formatting.BOLD}).append("\n\n");
        tooltip.append((Text)Text.literal((String)module.description).formatted(Formatting.WHITE));
        return tooltip;
    }
}

