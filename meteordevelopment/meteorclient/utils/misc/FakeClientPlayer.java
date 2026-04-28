/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientConnectionState
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.client.world.ClientWorld$Properties
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.ClientConnection
 *  net.minecraft.network.NetworkSide
 *  net.minecraft.world.Difficulty
 */
package meteordevelopment.meteorclient.utils.misc;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PreInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.world.Difficulty;

public class FakeClientPlayer {
    private static ClientWorld world;
    private static PlayerEntity player;
    private static PlayerListEntry playerListEntry;
    private static UUID lastId;
    private static boolean needsNewEntry;

    private FakeClientPlayer() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(FakeClientPlayer.class);
    }

    public static PlayerEntity getPlayer() {
        UUID id = MeteorClient.mc.getSession().getUuidOrNull();
        if (player == null || !id.equals(lastId)) {
            if (world == null) {
                world = new ClientWorld(new ClientPlayNetworkHandler(MeteorClient.mc, new ClientConnection(NetworkSide.CLIENTBOUND), new ClientConnectionState(new GameProfile(MeteorClient.mc.getSession().getUuidOrNull(), MeteorClient.mc.getSession().getUsername()), null, null, null, null, MeteorClient.mc.getCurrentServerEntry(), null, null, null, false, null, null)), new ClientWorld.Properties(Difficulty.NORMAL, false, false), world.getRegistryKey(), world.getDimensionEntry(), 1, 1, () -> ((MinecraftClient)MeteorClient.mc).getProfiler(), null, false, 0L);
            }
            player = new OtherClientPlayerEntity(world, new GameProfile(id, MeteorClient.mc.getSession().getUsername()));
            lastId = id;
            needsNewEntry = true;
        }
        return player;
    }

    public static PlayerListEntry getPlayerListEntry() {
        if (playerListEntry == null || needsNewEntry) {
            playerListEntry = new PlayerListEntry(new GameProfile(lastId, MeteorClient.mc.getSession().getUsername()), false);
            needsNewEntry = false;
        }
        return playerListEntry;
    }
}

