/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.BrandCustomPayload
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket
 *  net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket
 *  net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket$Status
 *  net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Identifier
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.text.RunnableClickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

public class ServerSpoof
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> spoofBrand;
    private final Setting<String> brand;
    private final Setting<Boolean> resourcePack;
    private final Setting<Boolean> blockChannels;
    private final Setting<List<String>> channels;
    private MutableText msg;
    public boolean silentAcceptResourcePack;

    public ServerSpoof() {
        super(Categories.Misc, "server-spoof", "Spoof client brand, resource pack and channels.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.spoofBrand = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spoof-brand")).description("Whether or not to spoof the brand.")).defaultValue(true)).build());
        this.brand = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("brand")).description("Specify the brand that will be send to the server.")).defaultValue("vanilla")).visible(this.spoofBrand::get)).build());
        this.resourcePack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("resource-pack")).description("Spoof accepting server resource pack.")).defaultValue(false)).build());
        this.blockChannels = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("block-channels")).description("Whether or not to block some channels.")).defaultValue(true)).build());
        this.channels = this.sgGeneral.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("channels")).description("If the channel contains the keyword, this outgoing channel will be blocked.")).defaultValue("fabric", "minecraft:register").visible(this.blockChannels::get)).build());
        this.silentAcceptResourcePack = false;
        this.runInMainMenu = true;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!this.isActive()) {
            return;
        }
        if (event.packet instanceof CustomPayloadC2SPacket) {
            Identifier id = ((CustomPayloadC2SPacket)event.packet).comp_1647().getId().comp_2242();
            if (this.blockChannels.get().booleanValue()) {
                for (String channel : this.channels.get()) {
                    if (!StringUtils.containsIgnoreCase((CharSequence)id.toString(), (CharSequence)channel)) continue;
                    event.cancel();
                    return;
                }
            }
            if (this.spoofBrand.get().booleanValue() && id.equals((Object)BrandCustomPayload.ID.comp_2242())) {
                CustomPayloadC2SPacket spoofedPacket = new CustomPayloadC2SPacket((CustomPayload)new BrandCustomPayload(this.brand.get()));
                event.connection.send((Packet)spoofedPacket, null, true);
                event.cancel();
            }
        }
        if (this.silentAcceptResourcePack && event.packet instanceof ResourcePackStatusC2SPacket) {
            event.cancel();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!this.isActive() || !this.resourcePack.get().booleanValue()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (!(packet instanceof ResourcePackSendS2CPacket)) {
            return;
        }
        ResourcePackSendS2CPacket packet2 = (ResourcePackSendS2CPacket)packet;
        event.cancel();
        event.connection.send((Packet)new ResourcePackStatusC2SPacket(packet2.comp_2158(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
        event.connection.send((Packet)new ResourcePackStatusC2SPacket(packet2.comp_2158(), ResourcePackStatusC2SPacket.Status.DOWNLOADED));
        event.connection.send((Packet)new ResourcePackStatusC2SPacket(packet2.comp_2158(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
        this.msg = Text.literal((String)"This server has ");
        this.msg.append(packet2.comp_2161() ? "a required " : "an optional ").append("resource pack. ");
        MutableText link = Text.literal((String)"[Open URL]");
        link.setStyle(link.getStyle().withColor(Formatting.BLUE).withUnderline(Boolean.valueOf(true)).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, packet2.comp_2159())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Click to open the pack url"))));
        MutableText acceptance = Text.literal((String)"[Accept Pack]");
        acceptance.setStyle(acceptance.getStyle().withColor(Formatting.DARK_GREEN).withUnderline(Boolean.valueOf(true)).withClickEvent((ClickEvent)new RunnableClickEvent(() -> {
            URL url = ServerSpoof.getParsedResourcePackUrl(packet2.comp_2159());
            if (url == null) {
                this.error("Invalid resource pack URL: " + packet2.comp_2159(), new Object[0]);
            } else {
                this.silentAcceptResourcePack = true;
                this.mc.getServerResourcePackProvider().addResourcePack(packet2.comp_2158(), url, packet2.comp_2160());
            }
        })).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Click to accept and apply the pack."))));
        this.msg.append((Text)link).append(" ");
        this.msg.append((Text)acceptance).append(".");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.isActive() || !Utils.canUpdate() || this.msg == null) {
            return;
        }
        this.info((Text)this.msg);
        this.msg = null;
    }

    private static URL getParsedResourcePackUrl(String url) {
        try {
            URL uRL = new URI(url).toURL();
            String string = uRL.getProtocol();
            return !"http".equals(string) && !"https".equals(string) ? null : uRL;
        }
        catch (MalformedURLException | URISyntaxException var3) {
            return null;
        }
    }
}

