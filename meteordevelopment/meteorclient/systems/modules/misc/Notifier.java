/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.collection.ArrayListDeque
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.PlayerJoinLeaveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.ArrayListDeque;
import net.minecraft.util.math.Vec3d;

public class Notifier
extends Module {
    private final SettingGroup sgTotemPops;
    private final SettingGroup sgVisualRange;
    private final SettingGroup sgPearl;
    private final SettingGroup sgJoinsLeaves;
    private final Setting<Boolean> totemPops;
    private final Setting<Boolean> totemsDistanceCheck;
    private final Setting<Integer> totemsDistance;
    private final Setting<Boolean> totemsIgnoreOwn;
    private final Setting<Boolean> totemsIgnoreFriends;
    private final Setting<Boolean> totemsIgnoreOthers;
    private final Setting<Boolean> visualRange;
    private final Setting<Event> event;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> visualRangeIgnoreNakeds;
    private final Setting<Boolean> visualRangeIgnoreFriends;
    private final Setting<Boolean> visualRangeIgnoreFakes;
    private final Setting<Boolean> visualMakeSound;
    private final Setting<Boolean> pearl;
    private final Setting<Boolean> pearlIgnoreOwn;
    private final Setting<Boolean> pearlIgnoreFriends;
    private final Setting<Boolean> showPosition;
    private final Setting<JoinLeaveModes> joinsLeavesMode;
    private final Setting<Integer> notificationDelay;
    private final Setting<Boolean> simpleNotifications;
    private int timer;
    private final Object2IntMap<UUID> chatIdMap;
    private final Map<Integer, Vec3d> pearlStartPosMap;
    private final ArrayListDeque<Text> messageQueue;
    private Set<AbstractClientPlayerEntity> lastPlayerVisualRangeList;
    private final Random random;

    public Notifier() {
        super(Categories.Misc, "notifier", "Notifies you of different events.");
        this.sgTotemPops = this.settings.createGroup("Totem Pops");
        this.sgVisualRange = this.settings.createGroup("Visual Range");
        this.sgPearl = this.settings.createGroup("Pearl");
        this.sgJoinsLeaves = this.settings.createGroup("Joins/Leaves");
        this.totemPops = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("totem-pops")).description("Notifies you when a player pops a totem.")).defaultValue(true)).build());
        this.totemsDistanceCheck = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance-check")).description("Limits the distance in which the pops are recognized.")).defaultValue(false)).visible(this.totemPops::get)).build());
        this.totemsDistance = this.sgTotemPops.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("player-radius")).description("The radius in which to log totem pops.")).defaultValue(30)).sliderRange(1, 50).range(1, 100).visible(() -> this.totemPops.get() != false && this.totemsDistanceCheck.get() != false)).build());
        this.totemsIgnoreOwn = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignores your own totem pops.")).defaultValue(false)).build());
        this.totemsIgnoreFriends = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores friends totem pops.")).defaultValue(false)).build());
        this.totemsIgnoreOthers = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-others")).description("Ignores other players totem pops.")).defaultValue(false)).build());
        this.visualRange = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("visual-range")).description("Notifies you when an entity enters your render distance.")).defaultValue(false)).build());
        this.event = this.sgVisualRange.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("event")).description("When to log the entities.")).defaultValue(Event.Both)).build());
        this.entities = this.sgVisualRange.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Which entities to notify about.")).defaultValue(EntityType.PLAYER).build());
        this.visualRangeIgnoreNakeds = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Ignore players with no items.")).defaultValue(true)).build());
        this.visualRangeIgnoreFriends = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores friends.")).defaultValue(true)).build());
        this.visualRangeIgnoreFakes = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-fake-players")).description("Ignores fake players.")).defaultValue(true)).build());
        this.visualMakeSound = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sound")).description("Emits a sound effect on enter / leave")).defaultValue(true)).build());
        this.pearl = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pearl")).description("Notifies you when a player is teleported using an ender pearl.")).defaultValue(true)).build());
        this.pearlIgnoreOwn = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignores your own pearls.")).defaultValue(false)).build());
        this.pearlIgnoreFriends = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores friends pearls.")).defaultValue(false)).build());
        this.showPosition = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-position")).description("Whether or not to show the position of the pearl when it lands")).defaultValue(false)).build());
        this.joinsLeavesMode = this.sgJoinsLeaves.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("player-joins-leaves")).description("How to handle player join/leave notifications.")).defaultValue(JoinLeaveModes.None)).build());
        this.notificationDelay = this.sgJoinsLeaves.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("notification-delay")).description("How long to wait in ticks before posting the next join/leave notification in your chat.")).range(0, 1000).sliderRange(0, 100).defaultValue(0)).build());
        this.simpleNotifications = this.sgJoinsLeaves.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("simple-notifications")).description("Display join/leave notifications without a prefix, to reduce chat clutter.")).defaultValue(true)).build());
        this.chatIdMap = new Object2IntOpenHashMap();
        this.pearlStartPosMap = new HashMap<Integer, Vec3d>();
        this.messageQueue = new ArrayListDeque();
        this.lastPlayerVisualRangeList = new HashSet<AbstractClientPlayerEntity>();
        this.random = new Random();
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        Entity entity;
        if (!event.entity.getUuid().equals(this.mc.player.getUuid()) && this.entities.get().contains(event.entity.getType()) && this.visualRange.get().booleanValue() && this.event.get() != Event.Despawn && !(event.entity instanceof PlayerEntity)) {
            MutableText text = Text.literal((String)event.entity.getType().getName().getString()).formatted(Formatting.WHITE);
            text.append((Text)Text.literal((String)" has spawned at ").formatted(Formatting.GRAY));
            text.append((Text)ChatUtils.formatCoords(event.entity.getPos()));
            text.append((Text)Text.literal((String)".").formatted(Formatting.GRAY));
            this.info((Text)text);
        }
        if (this.pearl.get().booleanValue() && (entity = event.entity) instanceof EnderPearlEntity) {
            EnderPearlEntity pearlEntity = (EnderPearlEntity)entity;
            this.pearlStartPosMap.put(pearlEntity.getId(), new Vec3d(pearlEntity.getX(), pearlEntity.getY(), pearlEntity.getZ()));
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        Entity e;
        int i;
        if (!event.entity.getUuid().equals(this.mc.player.getUuid()) && this.entities.get().contains(event.entity.getType()) && this.visualRange.get().booleanValue() && this.event.get() != Event.Spawn && !(event.entity instanceof PlayerEntity)) {
            MutableText text = Text.literal((String)event.entity.getType().getName().getString()).formatted(Formatting.WHITE);
            text.append((Text)Text.literal((String)" has despawned at ").formatted(Formatting.GRAY));
            text.append((Text)ChatUtils.formatCoords(event.entity.getPos()));
            text.append((Text)Text.literal((String)".").formatted(Formatting.GRAY));
            this.info((Text)text);
        }
        if (this.pearl.get().booleanValue() && this.pearlStartPosMap.containsKey(i = (e = event.entity).getId())) {
            Entity entity;
            EnderPearlEntity pearl = (EnderPearlEntity)e;
            if (pearl.getOwner() != null && (entity = pearl.getOwner()) instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity)entity;
                double d = this.pearlStartPosMap.get(i).distanceTo(e.getPos());
                if (!(Friends.get().isFriend(p) && this.pearlIgnoreFriends.get().booleanValue() || p.equals((Object)this.mc.player) && this.pearlIgnoreOwn.get().booleanValue())) {
                    if (this.showPosition.get().booleanValue()) {
                        this.info("(highlight)%s's(default) pearl landed at %d, %d, %d (highlight)(%.1fm away, travelled %.1fm)(default).", pearl.getOwner().getName().getString(), pearl.getBlockPos().getX(), pearl.getBlockPos().getY(), pearl.getBlockPos().getZ(), Float.valueOf(pearl.distanceTo((Entity)this.mc.player)), d);
                    } else {
                        this.info("(highlight)%s's(default) pearl landed at (highlight)(%.1fm away, travelled %.1fm)(default).", pearl.getOwner().getName().getString(), Float.valueOf(pearl.distanceTo((Entity)this.mc.player)), d);
                    }
                }
            }
            this.pearlStartPosMap.remove(i);
        }
    }

    @Override
    public void onActivate() {
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
    }

    @Override
    public void onDeactivate() {
        this.timer = 0;
        this.messageQueue.clear();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.timer = 0;
        this.chatIdMap.clear();
        this.messageQueue.clear();
        this.pearlStartPosMap.clear();
    }

    @EventHandler
    private void onTotemPop(PlayerDeathEvent.TotemPop event) {
        if (!this.totemPops.get().booleanValue()) {
            return;
        }
        if (this.totemsIgnoreOwn.get().booleanValue() && event.getPlayer() == this.mc.player) {
            return;
        }
        if (this.totemsIgnoreFriends.get().booleanValue() && Friends.get().isFriend(event.getPlayer())) {
            return;
        }
        if (this.totemsIgnoreOthers.get().booleanValue() && event.getPlayer() != this.mc.player) {
            return;
        }
        double distance = PlayerUtils.distanceTo((Entity)event.getPlayer());
        if (this.totemsDistanceCheck.get().booleanValue() && distance > (double)this.totemsDistance.get().intValue()) {
            return;
        }
        ChatUtils.sendMsg(this.getChatId((Entity)event.getPlayer()), Formatting.GRAY, "(highlight)%s (default)popped (highlight)%d (default)%s.", event.getPlayer().getName().getString(), event.getPops(), event.getPops() == 1 ? "totem" : "totems");
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent.Death event) {
        if (!this.totemPops.get().booleanValue()) {
            return;
        }
        if (this.totemsIgnoreOwn.get().booleanValue() && event.getPlayer() == this.mc.player) {
            return;
        }
        if (this.totemsIgnoreFriends.get().booleanValue() && Friends.get().isFriend(event.getPlayer())) {
            return;
        }
        if (this.totemsIgnoreOthers.get().booleanValue() && event.getPlayer() != this.mc.player) {
            return;
        }
        ChatUtils.sendMsg(this.getChatId((Entity)event.getPlayer()), Formatting.GRAY, "(highlight)%s (default)died after popping (highlight)%d (default)%s.", event.getPlayer().getName().getString(), event.getPops(), event.getPops() == 1 ? "totem" : "totems");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.joinsLeavesMode.get() != JoinLeaveModes.None) {
            ++this.timer;
            while (this.timer >= this.notificationDelay.get() && !this.messageQueue.isEmpty()) {
                this.timer = 0;
                if (this.simpleNotifications.get().booleanValue()) {
                    this.mc.player.sendMessage((Text)this.messageQueue.removeFirst());
                    continue;
                }
                ChatUtils.sendMsg((Text)this.messageQueue.removeFirst());
            }
        }
        if (!this.visualRange.get().booleanValue()) {
            return;
        }
        Set currentPlayers = this.mc.world.getPlayers().stream().collect(Collectors.toSet());
        if (this.event.get() != Event.Despawn) {
            for (AbstractClientPlayerEntity player : currentPlayers) {
                if (this.lastPlayerVisualRangeList.contains(player) || this.visualRangeIgnoreFriends.get().booleanValue() && Friends.get().isFriend((PlayerEntity)player) || this.visualRangeIgnoreFakes.get().booleanValue() && player instanceof FakePlayerEntity || this.visualRangeIgnoreNakeds.get().booleanValue() && player.getInventory().armor.stream().allMatch(x -> x.isEmpty())) continue;
                ChatUtils.sendMsg(player.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has entered your visual range!", player.getName().getString());
                if (!this.visualMakeSound.get().booleanValue()) continue;
                this.mc.world.playSoundFromEntity((PlayerEntity)this.mc.player, (Entity)this.mc.player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 3.0f, 1.0f);
            }
        }
        if (this.event.get() != Event.Spawn) {
            for (AbstractClientPlayerEntity player : this.lastPlayerVisualRangeList) {
                if (currentPlayers.contains(player) || this.visualRangeIgnoreFriends.get().booleanValue() && Friends.get().isFriend((PlayerEntity)player) || this.visualRangeIgnoreFakes.get().booleanValue() && player instanceof FakePlayerEntity || this.visualRangeIgnoreNakeds.get().booleanValue() && player.getInventory().armor.stream().allMatch(x -> x.isEmpty())) continue;
                ChatUtils.sendMsg(player.getId() + 100, Formatting.GRAY, "(highlight)%s(default) has left your visual range!", player.getName().getString());
                if (!this.visualMakeSound.get().booleanValue()) continue;
                this.mc.world.playSoundFromEntity((PlayerEntity)this.mc.player, (Entity)this.mc.player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 3.0f, 1.0f);
            }
        }
        this.lastPlayerVisualRangeList = currentPlayers;
    }

    private int getChatId(Entity entity) {
        return this.chatIdMap.computeIfAbsent((Object)entity.getUuid(), value -> this.random.nextInt());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinLeaveEvent.Join event) {
        if (this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.None) || this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.Leaves)) {
            return;
        }
        if (event.getEntry().comp_1107() == null) {
            return;
        }
        if (this.simpleNotifications.get().booleanValue()) {
            this.messageQueue.addLast((Object)Text.literal((String)(String.valueOf(Formatting.GRAY) + "[" + String.valueOf(Formatting.GREEN) + "+" + String.valueOf(Formatting.GRAY) + "] " + event.getEntry().comp_1107().getName())));
        } else {
            this.messageQueue.addLast((Object)Text.literal((String)(String.valueOf(Formatting.WHITE) + event.getEntry().comp_1107().getName() + String.valueOf(Formatting.GRAY) + " joined.")));
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerJoinLeaveEvent.Leave event) {
        if (this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.None) || this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.Joins)) {
            return;
        }
        if (this.simpleNotifications.get().booleanValue()) {
            this.messageQueue.addLast((Object)Text.literal((String)(String.valueOf(Formatting.GRAY) + "[" + String.valueOf(Formatting.RED) + "-" + String.valueOf(Formatting.GRAY) + "] " + event.getEntry().getProfile().getName())));
        } else {
            this.messageQueue.addLast((Object)Text.literal((String)(String.valueOf(Formatting.WHITE) + event.getEntry().getProfile().getName() + String.valueOf(Formatting.GRAY) + " left.")));
        }
    }

    private void createJoinNotifications(PlayerListS2CPacket packet) {
    }

    private void createLeaveNotification(PlayerRemoveS2CPacket packet) {
    }

    public static enum Event {
        Spawn,
        Despawn,
        Both;

    }

    public static enum JoinLeaveModes {
        None,
        Joins,
        Leaves,
        Both;

    }
}

