/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package meteordevelopment.meteorclient.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IChatHudLine {
    public String meteor$getText();

    public int meteor$getId();

    public void meteor$setId(int var1);

    public GameProfile meteor$getSender();

    public void meteor$setSender(GameProfile var1);
}

