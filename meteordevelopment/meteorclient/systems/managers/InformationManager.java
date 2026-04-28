/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Action
 *  net.minecraft.network.packet.s2c.play.PlayerListS2CPacket$Entry
 *  net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.managers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.PlayerJoinLeaveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.world.World;

public class InformationManager {
    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap();
    private boolean isLoginPacket = true;

    public InformationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (MeteorClient.mc.world == null || MeteorClient.mc.player == null) {
            return;
        }
        Packet<?> packet = event.packet;
        Objects.requireNonNull(packet);
        Packet<?> packet2 = packet;
        int n = 0;
        block9: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EntityStatusS2CPacket.class, PlayerListS2CPacket.class, PlayerRemoveS2CPacket.class, EntityStatusS2CPacket.class}, packet2, n)) {
                case 0: {
                    Entity entity;
                    EntityStatusS2CPacket packet3 = (EntityStatusS2CPacket)packet2;
                    if (packet3.getStatus() != 35 || !((entity = packet3.getEntity((World)MeteorClient.mc.world)) instanceof PlayerEntity)) {
                        n = 1;
                        continue block9;
                    }
                    PlayerEntity entity2 = (PlayerEntity)entity;
                    int pops = 0;
                    Object2IntMap<UUID> object2IntMap = this.totemPopMap;
                    synchronized (object2IntMap) {
                        pops = this.totemPopMap.getOrDefault((Object)entity2.getUuid(), 0);
                        this.totemPopMap.put((Object)entity2.getUuid(), ++pops);
                    }
                    MeteorClient.EVENT_BUS.post(PlayerDeathEvent.TotemPop.get(entity2, pops));
                    break block9;
                }
                case 1: {
                    PlayerListS2CPacket packet4 = (PlayerListS2CPacket)packet2;
                    if (this.isLoginPacket) {
                        this.isLoginPacket = false;
                        return;
                    }
                    if (!packet4.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) break block9;
                    for (PlayerListS2CPacket.Entry entry : packet4.getPlayerAdditionEntries()) {
                        MeteorClient.EVENT_BUS.post(PlayerJoinLeaveEvent.Join.get(entry));
                    }
                    break block9;
                }
                case 2: {
                    Entity toRemove;
                    PlayerRemoveS2CPacket packet5 = (PlayerRemoveS2CPacket)packet2;
                    if (MeteorClient.mc.getNetworkHandler() == null) {
                        return;
                    }
                    for (UUID uuid : packet5.comp_1105()) {
                        toRemove = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(uuid);
                        if (toRemove == null) continue;
                        MeteorClient.EVENT_BUS.post(PlayerJoinLeaveEvent.Leave.get((PlayerListEntry)toRemove));
                    }
                    break block9;
                }
                case 3: {
                    Entity toRemove;
                    EntityStatusS2CPacket packet6 = (EntityStatusS2CPacket)packet2;
                    if (packet6.getStatus() != 3 || !((toRemove = packet6.getEntity((World)MeteorClient.mc.world)) instanceof PlayerEntity)) {
                        n = 4;
                        continue block9;
                    }
                    PlayerEntity entity = (PlayerEntity)toRemove;
                    int pops = 0;
                    if (this.totemPopMap.containsKey((Object)entity.getUuid())) {
                        pops = this.totemPopMap.removeInt((Object)entity.getUuid());
                    }
                    MeteorClient.EVENT_BUS.post(PlayerDeathEvent.Death.get(entity, pops));
                    break block9;
                }
            }
            break;
        }
    }

    @EventHandler
    private void onGameLeave(GameLeftEvent event) {
        this.isLoginPacket = true;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (MeteorClient.mc.world == null || MeteorClient.mc.player == null) {
            return;
        }
    }

    public int getPops(Entity entity) {
        return this.totemPopMap.getOrDefault((Object)entity.getUuid(), 0);
    }

    public int getPops(UUID uuid) {
        return this.totemPopMap.getOrDefault((Object)uuid, 0);
    }
}

