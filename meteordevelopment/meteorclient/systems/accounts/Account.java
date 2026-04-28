/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.yggdrasil.ServicesKeySet
 *  com.mojang.authlib.yggdrasil.ServicesKeyType
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  net.minecraft.client.network.SocialInteractionsManager
 *  net.minecraft.client.session.ProfileKeys
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.report.AbuseReportContext
 *  net.minecraft.client.session.report.ReporterEnvironment
 *  net.minecraft.client.texture.PlayerSkinProvider
 *  net.minecraft.client.texture.PlayerSkinProvider$FileCache
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.network.encryption.SignatureVerifier
 *  net.minecraft.util.Util
 */
package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.FileCacheAccessor;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.mixin.PlayerSkinProviderAccessor;
import meteordevelopment.meteorclient.systems.accounts.AccountCache;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.util.Util;

public abstract class Account<T extends Account<?>>
implements ISerializable<T> {
    protected AccountType type;
    protected String name;
    protected final AccountCache cache;

    protected Account(AccountType type, String name) {
        this.type = type;
        this.name = name;
        this.cache = new AccountCache();
    }

    public abstract boolean fetchInfo();

    public boolean login() {
        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(((MinecraftClientAccessor)MeteorClient.mc).getProxy());
        Account.applyLoginEnvironment(authenticationService, authenticationService.createMinecraftSessionService());
        return true;
    }

    public String getUsername() {
        if (this.cache.username.isEmpty()) {
            return this.name;
        }
        return this.cache.username;
    }

    public AccountType getType() {
        return this.type;
    }

    public AccountCache getCache() {
        return this.cache;
    }

    public static void setSession(Session session) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor)MeteorClient.mc;
        mca.setSession(session);
        UserApiService apiService = mca.getAuthenticationService().createUserApiService(session.getAccessToken());
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManager(new SocialInteractionsManager(MeteorClient.mc, apiService));
        mca.setProfileKeys(ProfileKeys.create((UserApiService)apiService, (Session)session, (Path)MeteorClient.mc.runDirectory.toPath()));
        mca.setAbuseReportContext(AbuseReportContext.create((ReporterEnvironment)ReporterEnvironment.ofIntegratedServer(), (UserApiService)apiService));
        mca.setGameProfileFuture(CompletableFuture.supplyAsync(() -> MeteorClient.mc.getSessionService().fetchProfile(MeteorClient.mc.getSession().getUuidOrNull(), true), Util.getIoWorkerExecutor()));
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService, MinecraftSessionService sessService) {
        MinecraftClientAccessor mca = (MinecraftClientAccessor)MeteorClient.mc;
        mca.setAuthenticationService(authService);
        SignatureVerifier.create((ServicesKeySet)authService.getServicesKeySet(), (ServicesKeyType)ServicesKeyType.PROFILE_KEY);
        mca.setSessionService(sessService);
        PlayerSkinProvider.FileCache skinCache = ((PlayerSkinProviderAccessor)MeteorClient.mc.getSkinProvider()).getSkinCache();
        Path skinCachePath = ((FileCacheAccessor)skinCache).getDirectory();
        mca.setSkinProvider(new PlayerSkinProvider(MeteorClient.mc.getTextureManager(), skinCachePath, sessService, (Executor)MeteorClient.mc));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("type", this.type.name());
        tag.putString("name", this.name);
        tag.put("cache", (NbtElement)this.cache.toTag());
        return tag;
    }

    @Override
    public T fromTag(NbtCompound tag) {
        if (!tag.contains("name") || !tag.contains("cache")) {
            throw new NbtException();
        }
        this.name = tag.getString("name");
        this.cache.fromTag(tag.getCompound("cache"));
        return (T)this;
    }
}

