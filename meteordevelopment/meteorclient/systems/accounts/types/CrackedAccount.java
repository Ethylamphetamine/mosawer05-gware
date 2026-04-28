/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.Session$AccountType
 *  net.minecraft.util.Uuids
 */
package meteordevelopment.meteorclient.systems.accounts.types;

import java.util.Optional;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;

public class CrackedAccount
extends Account<CrackedAccount> {
    public CrackedAccount(String name) {
        super(AccountType.Cracked, name);
    }

    @Override
    public boolean fetchInfo() {
        this.cache.username = this.name;
        return true;
    }

    @Override
    public boolean login() {
        super.login();
        this.cache.loadHead();
        CrackedAccount.setSession(new Session(this.name, Uuids.getOfflinePlayerUuid((String)this.name), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        return true;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CrackedAccount)) {
            return false;
        }
        return ((CrackedAccount)o).getUsername().equals(this.getUsername());
    }
}

