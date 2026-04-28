/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.world;

import java.net.InetSocketAddress;

public class ServerConnectEndEvent {
    private static final ServerConnectEndEvent INSTANCE = new ServerConnectEndEvent();
    public InetSocketAddress address;

    public static ServerConnectEndEvent get(InetSocketAddress address) {
        ServerConnectEndEvent.INSTANCE.address = address;
        return INSTANCE;
    }
}

