/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.Environment
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.Session$AccountType
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.systems.accounts.types;

import com.mojang.authlib.Environment;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import de.florianmichael.waybackauthlib.InvalidCredentialsException;
import de.florianmichael.waybackauthlib.WaybackAuthLib;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.mixin.YggdrasilMinecraftSessionServiceAccessor;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.systems.accounts.TokenAccount;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.session.Session;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public class TheAlteningAccount
extends Account<TheAlteningAccount>
implements TokenAccount {
    private static final Environment ENVIRONMENT = new Environment("http://sessionserver.thealtening.com", "http://authserver.thealtening.com", "The Altening");
    private static final YggdrasilAuthenticationService SERVICE = new YggdrasilAuthenticationService(((MinecraftClientAccessor)MeteorClient.mc).getProxy(), ENVIRONMENT);
    private String token;
    @Nullable
    private WaybackAuthLib auth;

    public TheAlteningAccount(String token) {
        super(AccountType.TheAltening, token);
        this.token = token;
    }

    @Override
    public boolean fetchInfo() {
        this.auth = this.getAuth();
        try {
            this.auth.logIn();
            this.cache.username = this.auth.getCurrentProfile().getName();
            this.cache.uuid = this.auth.getCurrentProfile().getId().toString();
            this.cache.loadHead();
            return true;
        }
        catch (InvalidCredentialsException e) {
            MeteorClient.LOG.error("Invalid TheAltening credentials.");
            return false;
        }
        catch (Exception e) {
            MeteorClient.LOG.error("Failed to fetch info for TheAltening account!");
            return false;
        }
    }

    @Override
    public boolean login() {
        if (this.auth == null) {
            return false;
        }
        TheAlteningAccount.applyLoginEnvironment(SERVICE, (MinecraftSessionService)YggdrasilMinecraftSessionServiceAccessor.createYggdrasilMinecraftSessionService(SERVICE.getServicesKeySet(), SERVICE.getProxy(), ENVIRONMENT));
        try {
            TheAlteningAccount.setSession(new Session(this.auth.getCurrentProfile().getName(), this.auth.getCurrentProfile().getId(), this.auth.getAccessToken(), Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
            return true;
        }
        catch (Exception e) {
            MeteorClient.LOG.error("Failed to login with TheAltening.");
            return false;
        }
    }

    private WaybackAuthLib getAuth() {
        WaybackAuthLib auth = new WaybackAuthLib(ENVIRONMENT.servicesHost());
        auth.setUsername(this.name);
        auth.setPassword("Meteor on Crack!");
        return auth;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("type", this.type.name());
        tag.putString("name", this.name);
        tag.putString("token", this.token);
        tag.put("cache", (NbtElement)this.cache.toTag());
        return tag;
    }

    @Override
    public TheAlteningAccount fromTag(NbtCompound tag) {
        if (!(tag.contains("name") && tag.contains("cache") && tag.contains("token"))) {
            throw new NbtException();
        }
        this.name = tag.getString("name");
        this.token = tag.getString("token");
        this.cache.fromTag(tag.getCompound("cache"));
        return this;
    }
}

