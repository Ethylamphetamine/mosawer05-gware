/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class MessageAura
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<String> message;
    private final Setting<Boolean> ignoreFriends;

    public MessageAura() {
        super(Categories.Misc, "message-aura", "Sends a specified message to any player that enters render distance.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.message = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("message")).description("The specified message sent to the player.")).defaultValue("Meteor on Crack!")).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Will not send any messages to people friended.")).defaultValue(false)).build());
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof PlayerEntity) || event.entity.getUuid().equals(this.mc.player.getUuid())) {
            return;
        }
        if (!this.ignoreFriends.get().booleanValue() || this.ignoreFriends.get().booleanValue() && !Friends.get().isFriend((PlayerEntity)event.entity)) {
            ChatUtils.sendPlayerMsg("/msg " + event.entity.getName().getString() + " " + this.message.get());
        }
    }
}

