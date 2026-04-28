/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class AutoLog
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgEntities;
    private final SettingGroup sgAntiChainPop;
    private final Setting<Integer> health;
    private final Setting<Boolean> smart;
    private final Setting<Boolean> onlyTrusted;
    private final Setting<Boolean> instantDeath;
    private final Setting<Boolean> smartToggle;
    private final Setting<Boolean> toggleOff;
    private final Setting<Boolean> toggleAutoRecconect;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> useTotalCount;
    private final Setting<Integer> combinedEntityThreshold;
    private final Setting<Integer> individualEntityThreshold;
    private final Setting<Integer> range;
    private final Setting<Boolean> antiChainPop;
    private final Setting<Integer> chainPopLogCount;
    private final Object2IntMap<EntityType<?>> entityCounts;
    private LongSet chainPops;
    private final StaticListener staticListener;

    public AutoLog() {
        super(Categories.Combat, "auto-log", "Automatically disconnects you when certain requirements are met.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgEntities = this.settings.createGroup("Entities");
        this.sgAntiChainPop = this.settings.createGroup("Chain Pop");
        this.health = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("health")).description("Automatically disconnects when health is lower or equal to this value. Set to 0 to disable.")).defaultValue(6)).range(0, 19).sliderMax(19).build());
        this.smart = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart")).description("Disconnects when it detects you're about to take enough damage to set you under the 'health' setting.")).defaultValue(true)).build());
        this.onlyTrusted = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-trusted")).description("Disconnects when a player not on your friends list appears in render distance.")).defaultValue(false)).build());
        this.instantDeath = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("32K")).description("Disconnects when a player near you can instantly kill you.")).defaultValue(false)).build());
        this.smartToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart-toggle")).description("Disables Auto Log after a low-health logout. WILL re-enable once you heal.")).defaultValue(false)).build());
        this.toggleOff = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-off")).description("Disables Auto Log after usage.")).defaultValue(true)).build());
        this.toggleAutoRecconect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-auto-reconnect")).description("Disables Auto Reconnect after usage.")).defaultValue(true)).build());
        this.entities = this.sgEntities.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Disconnects when a specified entity is present within a specified range.")).defaultValue(EntityType.END_CRYSTAL).build());
        this.useTotalCount = this.sgEntities.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-total-count")).description("Toggle between counting the total number of all selected entities or each entity individually.")).defaultValue(true)).visible(() -> !this.entities.get().isEmpty())).build());
        this.combinedEntityThreshold = this.sgEntities.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("combined-entity-threshold")).description("The minimum total number of selected entities that must be near you before disconnection occurs.")).defaultValue(10)).min(1).sliderMax(32).visible(() -> this.useTotalCount.get() != false && !this.entities.get().isEmpty())).build());
        this.individualEntityThreshold = this.sgEntities.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("individual-entity-threshold")).description("The minimum number of entities individually that must be near you before disconnection occurs.")).defaultValue(2)).min(1).sliderMax(16).visible(() -> this.useTotalCount.get() == false && !this.entities.get().isEmpty())).build());
        this.range = this.sgEntities.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("range")).description("How close an entity has to be to you before you disconnect.")).defaultValue(5)).min(1).sliderMax(16).visible(() -> !this.entities.get().isEmpty())).build());
        this.antiChainPop = this.sgAntiChainPop.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-chain-pop")).description("Toggle between counting the total number of all selected entities or each entity individually.")).defaultValue(true)).build());
        this.chainPopLogCount = this.sgAntiChainPop.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("consecutive-pops-to-log")).description("Number of pops to take before disconnecting.")).defaultValue(3)).min(1).sliderMax(4).visible(() -> this.antiChainPop.get())).build());
        this.entityCounts = new Object2IntOpenHashMap();
        this.chainPops = new LongOpenHashSet();
        this.staticListener = new StaticListener();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        long currentTime = System.currentTimeMillis();
        int popsWithinLastSeconds = 0;
        for (Object time : this.chainPops) {
            double difference = (double)(currentTime - (Long)time) / 1000.0;
            if (!(difference < 2.0)) continue;
            ++popsWithinLastSeconds;
        }
        if (popsWithinLastSeconds > this.chainPopLogCount.get()) {
            this.disconnect("Popped " + popsWithinLastSeconds + " totems within 1.5 seconds");
            return;
        }
        float playerHealth = this.mc.player.getHealth();
        if (playerHealth <= 0.0f) {
            this.toggle();
            return;
        }
        if (playerHealth <= (float)this.health.get().intValue()) {
            this.disconnect("Health was lower than " + String.valueOf(this.health.get()) + ".");
            if (this.smartToggle.get().booleanValue()) {
                if (this.isActive()) {
                    this.toggle();
                }
                this.enableHealthListener();
            } else if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
            return;
        }
        if (this.smart.get().booleanValue() && playerHealth + this.mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions() < (float)this.health.get().intValue()) {
            this.disconnect("Health was going to be lower than " + String.valueOf(this.health.get()) + ".");
            if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
            return;
        }
        if (!this.onlyTrusted.get().booleanValue() && !this.instantDeath.get().booleanValue() && this.entities.get().isEmpty()) {
            return;
        }
        for (Entity entity : this.mc.world.getEntities()) {
            PlayerEntity player;
            if (!(entity instanceof PlayerEntity) || (player = (PlayerEntity)entity).getUuid() == this.mc.player.getUuid()) continue;
            if (this.onlyTrusted.get().booleanValue() && player != this.mc.player && !Friends.get().isFriend(player)) {
                this.disconnect((Text)Text.literal((String)("Non-trusted player '" + String.valueOf(Formatting.RED) + player.getName().getString() + String.valueOf(Formatting.WHITE) + "' appeared in your render distance.")));
                if (this.toggleOff.get().booleanValue()) {
                    this.toggle();
                }
                return;
            }
            if (!this.instantDeath.get().booleanValue() || !PlayerUtils.isWithin(entity, 8.0) || !(DamageUtils.getAttackDamage((LivingEntity)player, (LivingEntity)this.mc.player) > playerHealth + this.mc.player.getAbsorptionAmount())) continue;
            this.disconnect("Anti-32k measures.");
            if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
            return;
        }
        if (!this.entities.get().isEmpty()) {
            int totalEntities = 0;
            this.entityCounts.clear();
            for (Entity entity : this.mc.world.getEntities()) {
                if (!PlayerUtils.isWithin(entity, (double)this.range.get().intValue()) || !this.entities.get().contains(entity.getType())) continue;
                ++totalEntities;
                if (this.useTotalCount.get().booleanValue()) continue;
                this.entityCounts.put((Object)entity.getType(), this.entityCounts.getOrDefault((Object)entity.getType(), 0) + 1);
            }
            if (this.useTotalCount.get().booleanValue() && totalEntities >= this.combinedEntityThreshold.get()) {
                this.disconnect("Total number of selected entities within range exceeded the limit.");
                if (this.toggleOff.get().booleanValue()) {
                    this.toggle();
                }
            } else if (!this.useTotalCount.get().booleanValue()) {
                for (Object2IntMap.Entry entry : this.entityCounts.object2IntEntrySet()) {
                    if (entry.getIntValue() < this.individualEntityThreshold.get()) continue;
                    this.disconnect("Number of " + ((EntityType)entry.getKey()).getName().getString() + " within range exceeded the limit.");
                    if (this.toggleOff.get().booleanValue()) {
                        this.toggle();
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (!(packet instanceof EntityStatusS2CPacket)) {
            return;
        }
        EntityStatusS2CPacket p = (EntityStatusS2CPacket)packet;
        if (p.getStatus() != 35) {
            return;
        }
        Entity entity = p.getEntity((World)this.mc.world);
        if (entity == null || !entity.equals((Object)this.mc.player)) {
            return;
        }
        this.chainPops.add(System.currentTimeMillis());
    }

    private void disconnect(String reason) {
        this.disconnect((Text)Text.literal((String)reason));
    }

    private void disconnect(Text reason) {
        AutoReconnect autoReconnect;
        MutableText text = Text.literal((String)"[AutoLog] ");
        text.append(reason);
        if (this.toggleAutoRecconect.get().booleanValue() && (autoReconnect = Modules.get().get(AutoReconnect.class)).isActive()) {
            text.append((Text)Text.literal((String)"\n\nINFO - AutoReconnect was disabled").withColor(-8355712));
            autoReconnect.toggle();
        }
        this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket((Text)text));
    }

    private void enableHealthListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticListener);
    }

    private void disableHealthListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticListener);
    }

    private class StaticListener {
        private StaticListener() {
        }

        @EventHandler
        private void healthListener(TickEvent.Post event) {
            if (AutoLog.this.isActive()) {
                AutoLog.this.disableHealthListener();
            } else if (Utils.canUpdate() && !((AutoLog)AutoLog.this).mc.player.isDead() && ((AutoLog)AutoLog.this).mc.player.getHealth() > (float)AutoLog.this.health.get().intValue()) {
                AutoLog.this.info("Player health greater than minimum, re-enabling module.", new Object[0]);
                AutoLog.this.toggle();
                AutoLog.this.disableHealthListener();
            }
        }
    }
}

