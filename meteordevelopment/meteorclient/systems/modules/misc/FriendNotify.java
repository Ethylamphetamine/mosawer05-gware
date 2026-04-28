/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.HashSet;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class FriendNotify
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<String> message;
    private final Set<String> friendsCache;

    public FriendNotify() {
        super(Categories.Misc, "friend-notify", "Automatically messages players when you add them as a friend.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.message = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("message")).description("The message to send. Use {name} for the player name.")).defaultValue("/w {name} I just added you on Gware client!")).build());
        this.friendsCache = new HashSet<String>();
    }

    @Override
    public void onActivate() {
        this.friendsCache.clear();
        Friends.get().friendStream().forEach(friend -> this.friendsCache.add(friend.name));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.player == null) {
            return;
        }
        Friends.get().friendStream().forEach(friend -> {
            if (!this.friendsCache.contains(friend.name)) {
                String textToSend = this.message.get().replace("{name}", friend.name);
                if (textToSend.startsWith("/")) {
                    this.mc.getNetworkHandler().sendChatCommand(textToSend.substring(1));
                } else {
                    this.mc.getNetworkHandler().sendChatMessage(textToSend);
                }
                this.friendsCache.add(friend.name);
            }
        });
        this.friendsCache.removeIf(name -> Friends.get().get((String)name) == null);
    }
}

