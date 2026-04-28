/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.util.UndashedUuid
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.nbt.NbtCompound
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.friends;

import com.mojang.util.UndashedUuid;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public class Friend
implements ISerializable<Friend>,
Comparable<Friend> {
    public volatile String name;
    @Nullable
    private volatile UUID id;
    @Nullable
    private volatile PlayerHeadTexture headTexture;
    private volatile boolean updating;
    private volatile FriendType type = FriendType.Friend;

    public Friend(String name, @Nullable UUID id, FriendType type) {
        this.name = name;
        this.id = id;
        this.headTexture = null;
        this.type = type;
    }

    public Friend(PlayerEntity player, FriendType type) {
        this(player.getName().getString(), player.getUuid(), type);
    }

    public Friend(String name, FriendType type) {
        this(name, null, type);
    }

    public String getName() {
        return this.name;
    }

    public PlayerHeadTexture getHead() {
        return this.headTexture != null ? this.headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void updateInfo() {
        this.updating = true;
        APIResponse res = (APIResponse)Http.get("https://api.mojang.com/users/profiles/minecraft/" + this.name).sendJson((Type)((Object)APIResponse.class));
        if (res == null || res.name == null || res.id == null) {
            return;
        }
        this.name = res.name;
        this.id = UndashedUuid.fromStringLenient((String)res.id);
        this.headTexture = PlayerHeadUtils.fetchHead(this.id);
        this.updating = false;
    }

    public boolean headTextureNeedsUpdate() {
        return !this.updating && this.headTexture == null;
    }

    public FriendType getFriendType() {
        return this.type;
    }

    public void setfFriendType(FriendType type) {
        this.type = type;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", this.name);
        if (this.id != null) {
            tag.putString("id", UndashedUuid.toString((UUID)this.id));
        }
        switch (this.type.ordinal()) {
            case 0: {
                tag.putString("friendType", "Friend");
                break;
            }
            case 1: {
                tag.putString("friendType", "Enemy");
            }
        }
        return tag;
    }

    @Override
    public Friend fromTag(NbtCompound tag) {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Friend friend = (Friend)o;
        return Objects.equals(this.name, friend.name);
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public int compareTo(@NotNull Friend friend) {
        return this.name.compareTo(friend.name);
    }

    public static enum FriendType {
        Friend,
        Enemy;

    }

    private static class APIResponse {
        String name;
        String id;

        private APIResponse() {
        }
    }
}

