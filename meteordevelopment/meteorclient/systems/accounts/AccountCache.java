/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.util.UndashedUuid
 *  net.minecraft.nbt.NbtCompound
 */
package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.util.UndashedUuid;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.nbt.NbtCompound;

public class AccountCache
implements ISerializable<AccountCache> {
    public String username = "";
    public String uuid = "";
    private PlayerHeadTexture headTexture;

    public PlayerHeadTexture getHeadTexture() {
        return this.headTexture != null ? this.headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void loadHead() {
        if (this.uuid == null || this.uuid.isBlank()) {
            return;
        }
        this.headTexture = PlayerHeadUtils.fetchHead(UndashedUuid.fromStringLenient((String)this.uuid));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("username", this.username);
        tag.putString("uuid", this.uuid);
        return tag;
    }

    @Override
    public AccountCache fromTag(NbtCompound tag) {
        if (!tag.contains("username") || !tag.contains("uuid")) {
            throw new NbtException();
        }
        this.username = tag.getString("username");
        this.uuid = tag.getString("uuid");
        this.loadHead();
        return this;
    }
}

