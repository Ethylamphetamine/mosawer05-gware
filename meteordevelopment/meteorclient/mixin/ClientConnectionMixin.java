/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelPipeline
 *  io.netty.handler.timeout.TimeoutException
 *  net.minecraft.network.ClientConnection
 *  net.minecraft.network.NetworkSide
 *  net.minecraft.network.PacketCallbacks
 *  net.minecraft.network.handler.PacketEncoderException
 *  net.minecraft.network.handler.PacketSizeLogger
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.BundleS2CPacket
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.TimeoutException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ServerConnectEndEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.handler.PacketSizeLogger;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientConnection.class})
public abstract class ClientConnectionMixin {
    @Inject(method={"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", shift=At.Shift.BEFORE)}, cancellable=true)
    private void onHandlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof BundleS2CPacket) {
            BundleS2CPacket bundle = (BundleS2CPacket)packet;
            Iterator it = bundle.getPackets().iterator();
            while (it.hasNext()) {
                if (!MeteorClient.EVENT_BUS.post(new PacketEvent.Receive((Packet)it.next(), (ClientConnection)this)).isCancelled()) continue;
                it.remove();
            }
        } else if (MeteorClient.EVENT_BUS.post(new PacketEvent.Receive(packet, (ClientConnection)this)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"disconnect(Lnet/minecraft/text/Text;)V"}, at={@At(value="HEAD")})
    private void disconnect(Text disconnectReason, CallbackInfo ci) {
        if (Modules.get().get(HighwayBuilder.class).isActive()) {
            MutableText text = Text.literal((String)"%n%n%s[%sHighway Builder%s] Statistics:%n".formatted(Formatting.GRAY, Formatting.BLUE, Formatting.GRAY));
            text.append((Text)Modules.get().get(HighwayBuilder.class).getStatsText());
            ((MutableText)disconnectReason).append((Text)text);
        }
    }

    @Inject(method={"connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;"}, at={@At(value="HEAD")})
    private static void onConnect(InetSocketAddress address, boolean useEpoll, ClientConnection connection, CallbackInfoReturnable<?> cir) {
        MeteorClient.EVENT_BUS.post(ServerConnectEndEvent.get(address));
    }

    @Inject(at={@At(value="HEAD")}, method={"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"}, cancellable=true)
    private void onSendPacketHead(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (MeteorClient.EVENT_BUS.post(new PacketEvent.Send(packet, (ClientConnection)this)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"}, at={@At(value="TAIL")})
    private void onSendPacketTail(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new PacketEvent.Sent(packet, (ClientConnection)this));
    }

    @Inject(method={"exceptionCaught"}, at={@At(value="HEAD")}, cancellable=true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        AntiPacketKick apk = Modules.get().get(AntiPacketKick.class);
        if (!(throwable instanceof TimeoutException) && !(throwable instanceof PacketEncoderException) && apk.catchExceptions()) {
            if (apk.logExceptions.get().booleanValue()) {
                apk.warning("Caught exception: %s", throwable);
            }
            ci.cancel();
        }
    }

    @Inject(method={"addHandlers"}, at={@At(value="RETURN")})
    private static void onAddHandlers(ChannelPipeline pipeline, NetworkSide side, boolean local, PacketSizeLogger packetSizeLogger, CallbackInfo ci) {
        if (side != NetworkSide.CLIENTBOUND) {
            return;
        }
        Proxy proxy = Proxies.get().getEnabled();
        if (proxy == null) {
            return;
        }
        switch (proxy.type.get()) {
            case Socks4: {
                pipeline.addFirst(new ChannelHandler[]{new Socks4ProxyHandler(new InetSocketAddress(proxy.address.get(), (int)proxy.port.get()), proxy.username.get())});
                break;
            }
            case Socks5: {
                pipeline.addFirst(new ChannelHandler[]{new Socks5ProxyHandler(new InetSocketAddress(proxy.address.get(), (int)proxy.port.get()), proxy.username.get(), proxy.password.get())});
            }
        }
    }
}

