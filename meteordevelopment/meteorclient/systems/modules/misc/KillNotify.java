/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Formatting;

public class KillNotify
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> chat;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Double> range;
    private final Map<Integer, Long> attackTimestamps;
    private int killStreak;
    private final Random random;
    private final List<String> messages;

    public KillNotify() {
        super(Categories.Misc, "kill-notify", "Lets you know if one of your opps died.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.chat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-message")).description("Sends the kill message in public chat instead of client side only.")).defaultValue(false)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Doesn't count kills on friends.")).defaultValue(true)).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("detection-range")).description("If a player dies within this range, assume it was your kill (Fixes crystal detection).")).defaultValue(10.0).min(0.0).sliderMax(20.0).build());
        this.attackTimestamps = new HashMap<Integer, Long>();
        this.killStreak = 0;
        this.random = new Random();
        this.messages = List.of("lel we sent {name} back to the poor farm", "awarded the award as imperator we sent {name} to death he will not return home", "thanks to this guinness i ordered 3123 attack packets to {name}", "gware used tail whip super effective to poooron {name}", "\u4e0d\u9519\u7684 iq\uff0c\u6211\u4eec\u8ba9\u4f60\u5f88\u7a77 meow {name}", "bob arrived he thought he could win {name}", "wtf is wrong with this boy {name}", "{name} has been sent back to the poor novice village", "LEL {name} just got packed up by gware", "LOL! LOL! LOL! LOL! LOL! EZZZZZZZZZZZZZZZZ {name}");
    }

    @Override
    public void onActivate() {
        this.killStreak = 0;
        this.attackTimestamps.clear();
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (event.entity instanceof PlayerEntity) {
            this.attackTimestamps.put(event.entity.getId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerInteractEntityC2SPacket) {
            PlayerInteractEntityC2SPacket packet2 = (PlayerInteractEntityC2SPacket)packet;
            Entity target = ((IPlayerInteractEntityC2SPacket)packet2).getEntity();
            if (target == null) {
                return;
            }
            if (target instanceof PlayerEntity) {
                this.attackTimestamps.put(target.getId(), System.currentTimeMillis());
            } else if (target instanceof EndCrystalEntity) {
                for (PlayerEntity player : this.mc.world.getPlayers()) {
                    if (player == this.mc.player || !(player.distanceTo(target) < 7.0f)) continue;
                    this.attackTimestamps.put(player.getId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent.Death event) {
        PlayerEntity player = event.getPlayer();
        if (player == null || player == this.mc.player) {
            return;
        }
        if (this.ignoreFriends.get().booleanValue() && Friends.get().isFriend(player)) {
            return;
        }
        boolean isKill = false;
        long lastAttack = this.attackTimestamps.getOrDefault(player.getId(), 0L);
        if (System.currentTimeMillis() - lastAttack < 5000L) {
            isKill = true;
        }
        if (!isKill && (double)this.mc.player.distanceTo((Entity)player) <= this.range.get()) {
            isKill = true;
        }
        if (isKill) {
            ++this.killStreak;
            String rawMessage = this.messages.get(this.random.nextInt(this.messages.size()));
            String formattedName = player.getName().getString();
            String message = rawMessage.replace("{name}", formattedName);
            if (this.chat.get().booleanValue()) {
                this.mc.player.networkHandler.sendChatMessage(message);
            } else {
                this.info(message, new Object[0]);
                this.info(String.valueOf(Formatting.RED) + this.killStreak + " kill(s)", new Object[0]);
            }
            this.attackTimestamps.remove(player.getId());
        }
    }
}

