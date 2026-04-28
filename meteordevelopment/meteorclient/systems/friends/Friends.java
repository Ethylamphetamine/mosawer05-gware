/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.util.UndashedUuid
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.friends;

import com.mojang.util.UndashedUuid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

public class Friends
extends System<Friends> {
    private final List<Friend> friends = new ArrayList<Friend>();

    public Friends() {
        super("friends");
    }

    public static Friends get() {
        return Systems.get(Friends.class);
    }

    public boolean add(Friend friend) {
        if (friend.name.isEmpty() || friend.name.contains(" ")) {
            return false;
        }
        if (!this.friends.contains(friend)) {
            this.friends.add(friend);
            this.save();
            return true;
        }
        Friend friendListFriend = this.friends.get(this.friends.indexOf(friend));
        if (friendListFriend.getFriendType() != friend.getFriendType()) {
            friendListFriend.setfFriendType(friend.getFriendType());
            return true;
        }
        return false;
    }

    public boolean remove(Friend friend) {
        if (this.friends.remove(friend)) {
            this.save();
            return true;
        }
        return false;
    }

    public Friend get(String name) {
        for (Friend friend : this.friends) {
            if (!friend.name.equalsIgnoreCase(name)) continue;
            return friend;
        }
        return null;
    }

    public Friend get(PlayerEntity player) {
        return this.get(player.getName().getString());
    }

    public Friend get(PlayerListEntry player) {
        return this.get(player.getProfile().getName());
    }

    public boolean isFriend(PlayerEntity player) {
        return player != null && this.get(player) != null && this.get(player).getFriendType() == Friend.FriendType.Friend;
    }

    public boolean isFriend(PlayerListEntry player) {
        return this.get(player) != null && this.get(player).getFriendType() == Friend.FriendType.Friend;
    }

    public boolean isEnemy(PlayerEntity player) {
        return player != null && this.get(player) != null && this.get(player).getFriendType() == Friend.FriendType.Enemy;
    }

    public boolean isEnemy(PlayerListEntry player) {
        return this.get(player) != null && this.get(player).getFriendType() == Friend.FriendType.Enemy;
    }

    public boolean shouldAttack(PlayerEntity player) {
        return !this.isFriend(player) || this.isEnemy(player);
    }

    public int count() {
        return this.friends.size();
    }

    public boolean isEmpty() {
        return this.friends.isEmpty();
    }

    @NotNull
    public Stream<Friend> friendStream() {
        return this.friends.stream().filter(x -> x.getFriendType() == Friend.FriendType.Friend);
    }

    @NotNull
    public Stream<Friend> enemyStream() {
        return this.friends.stream().filter(x -> x.getFriendType() == Friend.FriendType.Enemy);
    }

    @NotNull
    public Stream<Friend> stream() {
        return this.friends.stream();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("friends", (NbtElement)NbtUtils.listToTag(this.friends));
        return tag;
    }

    @Override
    public Friends fromTag(NbtCompound tag) {
        this.friends.clear();
        for (NbtElement itemTag : tag.getList("friends", 10)) {
            String uuid;
            String name;
            NbtCompound friendTag = (NbtCompound)itemTag;
            if (!friendTag.contains("name") || this.get(name = friendTag.getString("name")) != null) continue;
            String s_friendType = friendTag.getString("friendType");
            Friend.FriendType type = Friend.FriendType.Friend;
            if (s_friendType != null) {
                if (s_friendType.equals("Friend")) {
                    type = Friend.FriendType.Friend;
                } else if (s_friendType.equals("Enemy")) {
                    type = Friend.FriendType.Enemy;
                }
            }
            Friend friend = !(uuid = friendTag.getString("id")).isBlank() ? new Friend(name, UndashedUuid.fromStringLenient((String)uuid), type) : new Friend(name, type);
            this.friends.add(friend);
        }
        Collections.sort(this.friends);
        MeteorExecutor.execute(() -> this.friends.forEach(Friend::updateInfo));
        return this;
    }
}

