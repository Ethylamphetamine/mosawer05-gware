/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.command.CommandSource
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.text.TextColor
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class NameHistoryCommand
extends Command {
    public NameHistoryCommand() {
        super("name-history", "Provides a list of a players previous names from the laby.net api.", "history", "names");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(NameHistoryCommand.argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            MeteorExecutor.execute(() -> {
                PlayerListEntry lookUpTarget = PlayerListEntryArgumentType.get(context);
                UUID uuid = lookUpTarget.getProfile().getId();
                NameHistory history = (NameHistory)Http.get("https://laby.net/api/v2/user/" + String.valueOf(uuid) + "/get-profile").exceptionHandler(e -> this.error("There was an error fetching that users name history.", new Object[0])).sendJson((Type)((Object)NameHistory.class));
                if (history == null) {
                    return;
                }
                if (history.username_history == null || history.username_history.length == 0) {
                    this.error("There was an error fetching that users name history.", new Object[0]);
                }
                String name = lookUpTarget.getProfile().getName();
                MutableText initial = Text.literal((String)name);
                initial.append((Text)Text.literal((String)(name.endsWith("s") ? "'" : "'s")));
                Color nameColor = PlayerUtils.getPlayerColor(NameHistoryCommand.mc.world.getPlayerByUuid(uuid), Utils.WHITE);
                initial.setStyle(initial.getStyle().withColor(TextColor.fromRgb((int)nameColor.getPacked())).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://laby.net/@" + name)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"View on laby.net").formatted(Formatting.YELLOW).formatted(Formatting.ITALIC))));
                this.info((Text)initial.append((Text)Text.literal((String)" Username History:").formatted(Formatting.GRAY)));
                for (Name entry : history.username_history) {
                    MutableText nameText = Text.literal((String)entry.name);
                    nameText.formatted(Formatting.AQUA);
                    if (entry.changed_at != null && entry.changed_at.getTime() != 0L) {
                        MutableText changed = Text.literal((String)"Changed at: ");
                        changed.formatted(Formatting.GRAY);
                        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss, dd/MM/yyyy");
                        changed.append((Text)Text.literal((String)formatter.format(entry.changed_at)).formatted(Formatting.WHITE));
                        nameText.setStyle(nameText.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)changed)));
                    }
                    if (!entry.accurate) {
                        MutableText text = Text.literal((String)"*").formatted(Formatting.WHITE);
                        text.setStyle(text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"This name history entry is not accurate according to laby.net"))));
                        nameText.append((Text)text);
                    }
                    ChatUtils.sendMsg((Text)nameText);
                }
            });
            return 1;
        }));
    }

    private static class NameHistory {
        public Name[] username_history;

        private NameHistory() {
        }
    }

    private static class Name {
        public String name;
        public Date changed_at;
        public boolean accurate;

        private Name() {
        }
    }
}

