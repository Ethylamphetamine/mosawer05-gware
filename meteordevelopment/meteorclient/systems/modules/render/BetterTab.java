/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.text.TextColor
 *  net.minecraft.util.Formatting
 *  net.minecraft.world.GameMode
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public class BetterTab
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Integer> tabSize;
    public final Setting<Integer> tabHeight;
    private final Setting<Boolean> self;
    private final Setting<SettingColor> selfColor;
    private final Setting<Boolean> friends;
    private final Setting<Boolean> onlyFriendsAndEnemeies;
    public final Setting<Boolean> accurateLatency;
    private final Setting<Boolean> gamemode;

    public BetterTab() {
        super(Categories.Render, "better-tab", "Various improvements to the tab list.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.tabSize = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tablist-size")).description("How many players in total to display in the tablist.")).defaultValue(100)).min(1).sliderRange(1, 1000).build());
        this.tabHeight = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("column-height")).description("How many players to display in each column.")).defaultValue(20)).min(1).sliderRange(1, 1000).build());
        this.self = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-self")).description("Highlights yourself in the tablist.")).defaultValue(true)).build());
        this.selfColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("self-color")).description("The color to highlight your name with.")).defaultValue(new SettingColor(250, 130, 30)).visible(this.self::get)).build());
        this.friends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("highlight-friends")).description("Highlights friends in the tablist.")).defaultValue(true)).build());
        this.onlyFriendsAndEnemeies = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-friends-and-enemies")).description("Only shows friends and enemies in tab list.")).defaultValue(true)).build());
        this.accurateLatency = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("accurate-latency")).description("Shows latency as a number in the tablist.")).defaultValue(true)).build());
        this.gamemode = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gamemode")).description("Display gamemode next to the nick.")).defaultValue(false)).build());
    }

    public Text getPlayerName(PlayerListEntry playerListEntry) {
        Color color = null;
        Text name = playerListEntry.getDisplayName();
        if (name == null) {
            name = Text.literal((String)playerListEntry.getProfile().getName());
        }
        if (playerListEntry.getProfile().getId().toString().equals(this.mc.player.getGameProfile().getId().toString()) && this.self.get().booleanValue()) {
            color = this.selfColor.get();
        } else if (this.friends.get().booleanValue()) {
            Friend friend;
            if (Friends.get().isFriend(playerListEntry)) {
                friend = Friends.get().get(playerListEntry);
                if (friend != null) {
                    color = Config.get().friendColor.get();
                }
            } else if (Friends.get().isEnemy(playerListEntry) && (friend = Friends.get().get(playerListEntry)) != null) {
                color = Config.get().enemyColor.get();
            }
        }
        if (color != null) {
            String nameString = name.getString();
            for (Formatting format : Formatting.values()) {
                if (!format.isColor()) continue;
                nameString = nameString.replace(format.toString(), "");
            }
            name = Text.literal((String)nameString).setStyle(name.getStyle().withColor(TextColor.fromRgb((int)color.getPacked())));
        }
        if (this.gamemode.get().booleanValue()) {
            GameMode gm = playerListEntry.getGameMode();
            String gmText = "?";
            if (gm != null) {
                gmText = switch (gm) {
                    default -> throw new MatchException(null, null);
                    case GameMode.SPECTATOR -> "Sp";
                    case GameMode.SURVIVAL -> "S";
                    case GameMode.CREATIVE -> "C";
                    case GameMode.ADVENTURE -> "A";
                };
            }
            MutableText text = Text.literal((String)"");
            text.append(name);
            text.append(" [" + gmText + "]");
            name = text;
        }
        return name;
    }

    public boolean shouldShowPlayer(PlayerListEntry playerListEntry) {
        if (this.isActive() && this.onlyFriendsAndEnemeies.get().booleanValue()) {
            return Friends.get().isFriend(playerListEntry) || Friends.get().isEnemy(playerListEntry);
        }
        return true;
    }
}

