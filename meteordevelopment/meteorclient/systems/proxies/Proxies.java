/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.proxies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

public class Proxies
extends System<Proxies>
implements Iterable<Proxy> {
    public static final Pattern PROXY_PATTERN = Pattern.compile("^(?:([\\w\\s]+)=)?((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])(?i:@(socks[45]))?$", 8);
    private List<Proxy> proxies = new ArrayList<Proxy>();

    public Proxies() {
        super("proxies");
    }

    public static Proxies get() {
        return Systems.get(Proxies.class);
    }

    public boolean add(Proxy proxy) {
        for (Proxy p : this.proxies) {
            if (!p.type.get().equals((Object)proxy.type.get()) || !p.address.get().equals(proxy.address.get()) || p.port.get() != proxy.port.get()) continue;
            return false;
        }
        if (this.proxies.isEmpty()) {
            proxy.enabled.set(true);
        }
        this.proxies.add(proxy);
        this.save();
        return true;
    }

    public void remove(Proxy proxy) {
        if (this.proxies.remove(proxy)) {
            this.save();
        }
    }

    public Proxy getEnabled() {
        for (Proxy proxy : this.proxies) {
            if (!proxy.enabled.get().booleanValue()) continue;
            return proxy;
        }
        return null;
    }

    public void setEnabled(Proxy proxy, boolean enabled) {
        for (Proxy p : this.proxies) {
            p.enabled.set(false);
        }
        proxy.enabled.set(enabled);
        this.save();
    }

    public boolean isEmpty() {
        return this.proxies.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Proxy> iterator() {
        return this.proxies.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("proxies", (NbtElement)NbtUtils.listToTag(this.proxies));
        return tag;
    }

    @Override
    public Proxies fromTag(NbtCompound tag) {
        this.proxies = NbtUtils.listFromTag(tag.getList("proxies", 10), Proxy::new);
        return this;
    }
}

