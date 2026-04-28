/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.systems.proxies;

import org.jetbrains.annotations.Nullable;

public enum ProxyType {
    Socks4,
    Socks5;


    @Nullable
    public static ProxyType parse(String group) {
        for (ProxyType type : ProxyType.values()) {
            if (!type.name().equalsIgnoreCase(group)) continue;
            return type;
        }
        return null;
    }
}

