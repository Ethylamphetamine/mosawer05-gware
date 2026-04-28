/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.SocialInteractionsManager
 *  net.minecraft.client.resource.ResourceReloadLogger
 *  net.minecraft.client.session.ProfileKeys
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.report.AbuseReportContext
 *  net.minecraft.client.texture.PlayerSkinProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.net.Proxy;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={MinecraftClient.class})
public interface MinecraftClientAccessor {
    @Accessor(value="currentFps")
    public static int getFps() {
        return 0;
    }

    @Mutable
    @Accessor(value="session")
    public void setSession(Session var1);

    @Accessor(value="networkProxy")
    public Proxy getProxy();

    @Accessor(value="resourceReloadLogger")
    public ResourceReloadLogger getResourceReloadLogger();

    @Invoker(value="doAttack")
    public boolean leftClick();

    @Mutable
    @Accessor(value="profileKeys")
    public void setProfileKeys(ProfileKeys var1);

    @Accessor(value="authenticationService")
    public YggdrasilAuthenticationService getAuthenticationService();

    @Mutable
    @Accessor
    public void setUserApiService(UserApiService var1);

    @Mutable
    @Accessor(value="sessionService")
    public void setSessionService(MinecraftSessionService var1);

    @Mutable
    @Accessor(value="authenticationService")
    public void setAuthenticationService(YggdrasilAuthenticationService var1);

    @Mutable
    @Accessor(value="skinProvider")
    public void setSkinProvider(PlayerSkinProvider var1);

    @Mutable
    @Accessor(value="socialInteractionsManager")
    public void setSocialInteractionsManager(SocialInteractionsManager var1);

    @Mutable
    @Accessor(value="abuseReportContext")
    public void setAbuseReportContext(AbuseReportContext var1);

    @Mutable
    @Accessor(value="gameProfileFuture")
    public void setGameProfileFuture(CompletableFuture<ProfileResult> var1);
}

