/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.network.OtherClientPlayerEntity
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 *  net.minecraft.entity.player.PlayerEntity
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.utils.entity.fakeplayer;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class FakePlayerEntity
extends OtherClientPlayerEntity {
    public boolean doNotPush;
    public boolean hideWhenInsideCamera;

    public FakePlayerEntity(PlayerEntity player, String name, float health, boolean copyInv) {
        super(MeteorClient.mc.world, new GameProfile(UUID.randomUUID(), name));
        this.copyPositionAndRotation((Entity)player);
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
        this.prevHeadYaw = this.headYaw = player.headYaw;
        this.prevBodyYaw = this.bodyYaw = player.bodyYaw;
        Byte playerModel = (Byte)player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
        this.dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, (Object)playerModel);
        this.getAttributes().setFrom(player.getAttributes());
        this.setPose(player.getPose());
        this.capeX = this.getX();
        this.capeY = this.getY();
        this.capeZ = this.getZ();
        if (health <= 20.0f) {
            this.setHealth(health);
        } else {
            this.setHealth(health);
            this.setAbsorptionAmount(health - 20.0f);
        }
        if (copyInv) {
            this.getInventory().clone(player.getInventory());
        }
    }

    public void spawn() {
        this.unsetRemoved();
        MeteorClient.mc.world.addEntity((Entity)this);
    }

    public void despawn() {
        MeteorClient.mc.world.removeEntity(this.getId(), Entity.RemovalReason.DISCARDED);
        this.setRemoved(Entity.RemovalReason.DISCARDED);
    }

    @Nullable
    protected PlayerListEntry getPlayerListEntry() {
        if (this.playerListEntry == null) {
            this.playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(MeteorClient.mc.player.getUuid());
        }
        return this.playerListEntry;
    }
}

