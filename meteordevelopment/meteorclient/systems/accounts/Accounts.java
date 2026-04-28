/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.accounts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.systems.accounts.types.CrackedAccount;
import meteordevelopment.meteorclient.systems.accounts.types.MicrosoftAccount;
import meteordevelopment.meteorclient.systems.accounts.types.TheAlteningAccount;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

public class Accounts
extends System<Accounts>
implements Iterable<Account<?>> {
    private List<Account<?>> accounts = new ArrayList();

    public Accounts() {
        super("accounts");
    }

    public static Accounts get() {
        return Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        this.accounts.add(account);
        this.save();
    }

    public boolean exists(Account<?> account) {
        return this.accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (this.accounts.remove(account)) {
            this.save();
        }
    }

    public int size() {
        return this.accounts.size();
    }

    @Override
    @NotNull
    public Iterator<Account<?>> iterator() {
        return this.accounts.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("accounts", (NbtElement)NbtUtils.listToTag(this.accounts));
        return tag;
    }

    @Override
    public Accounts fromTag(NbtCompound tag) {
        MeteorExecutor.execute(() -> {
            this.accounts = NbtUtils.listFromTag(tag.getList("accounts", 10), tag1 -> {
                NbtCompound t = (NbtCompound)tag1;
                if (!t.contains("type")) {
                    return null;
                }
                AccountType type = AccountType.valueOf(t.getString("type"));
                try {
                    return switch (type) {
                        default -> throw new MatchException(null, null);
                        case AccountType.Cracked -> (CrackedAccount)new CrackedAccount(null).fromTag(t);
                        case AccountType.Microsoft -> (MicrosoftAccount)new MicrosoftAccount(null).fromTag(t);
                        case AccountType.TheAltening -> new TheAlteningAccount(null).fromTag(t);
                    };
                }
                catch (NbtException e) {
                    return null;
                }
            });
        });
        return this;
    }
}

