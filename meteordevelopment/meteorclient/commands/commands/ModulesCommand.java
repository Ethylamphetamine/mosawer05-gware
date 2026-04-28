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
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModulesCommand
extends Command {
    public ModulesCommand() {
        super("modules", "Displays a list of all modules.", "features");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ChatUtils.info("--- Modules ((highlight)%d(default)) ---", Modules.get().getCount());
            Modules.loopCategories().forEach(category -> {
                MutableText categoryMessage = Text.literal((String)"");
                Modules.get().getGroup((Category)category).forEach(module -> categoryMessage.append((Text)this.getModuleText((Module)module)));
                ChatUtils.sendMsg(category.name, (Text)categoryMessage);
            });
            return 1;
        });
    }

    private MutableText getModuleText(Module module) {
        MutableText tooltip = Text.literal((String)"");
        tooltip.append((Text)Text.literal((String)module.title).formatted(new Formatting[]{Formatting.BLUE, Formatting.BOLD})).append("\n");
        tooltip.append((Text)Text.literal((String)module.name).formatted(Formatting.GRAY)).append("\n\n");
        tooltip.append((Text)Text.literal((String)module.description).formatted(Formatting.WHITE));
        MutableText finalModule = Text.literal((String)module.title);
        if (!module.isActive()) {
            finalModule.formatted(Formatting.GRAY);
        }
        if (!module.equals(Modules.get().getGroup(module.category).getLast())) {
            finalModule.append((Text)Text.literal((String)", ").formatted(Formatting.GRAY));
        }
        finalModule.setStyle(finalModule.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)tooltip)));
        return finalModule;
    }
}

