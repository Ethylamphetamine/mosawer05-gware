/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.util.UndashedUuid
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.Session$AccountType
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.systems.accounts.types;

import com.mojang.util.UndashedUuid;
import java.util.Optional;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.systems.accounts.MicrosoftLogin;
import net.minecraft.client.session.Session;
import org.jetbrains.annotations.Nullable;

public class MicrosoftAccount
extends Account<MicrosoftAccount> {
    @Nullable
    private String token;

    public MicrosoftAccount(String refreshToken) {
        super(AccountType.Microsoft, refreshToken);
    }

    @Override
    public boolean fetchInfo() {
        this.token = this.auth();
        return this.token != null;
    }

    @Override
    public boolean login() {
        if (this.token == null) {
            return false;
        }
        super.login();
        this.cache.loadHead();
        MicrosoftAccount.setSession(new Session(this.cache.username, UndashedUuid.fromStringLenient((String)this.cache.uuid), this.token, Optional.empty(), Optional.empty(), Session.AccountType.MSA));
        return true;
    }

    @Nullable
    private String auth() {
        MicrosoftLogin.LoginData data = MicrosoftLogin.login(this.name);
        if (!data.isGood()) {
            return null;
        }
        this.name = data.newRefreshToken;
        this.cache.username = data.username;
        this.cache.uuid = data.uuid;
        return data.mcToken;
    }

    public boolean equals(Object o) {
        if (!(o instanceof MicrosoftAccount)) {
            return false;
        }
        return ((MicrosoftAccount)o).name.equals(this.name);
    }
}

