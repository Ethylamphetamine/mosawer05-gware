/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.util.hit.BlockHitResult
 *  org.apache.commons.compress.utils.Sets
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.hit.BlockHitResult;
import org.apache.commons.compress.utils.Sets;

public class DebugModule
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets;
    private final Setting<Set<Class<? extends Packet<?>>>> s2cPackets;
    private static final Set<Class<?>> PRIMITIVE_TYPES = Sets.newHashSet((Object[])new Class[]{Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class, String.class});

    public DebugModule() {
        super(Categories.Misc, "debug-module", "A module for debugging. Don't touch this unless you know what you're doing.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.c2sPackets = this.sgGeneral.add(((PacketListSetting.Builder)((PacketListSetting.Builder)new PacketListSetting.Builder().name("C2S-packets")).description("Client-to-server packets to log.")).filter(aClass -> PacketUtils.getC2SPackets().contains(aClass)).build());
        this.s2cPackets = this.sgGeneral.add(((PacketListSetting.Builder)((PacketListSetting.Builder)new PacketListSetting.Builder().name("S2C-packets")).description("Server-to-client packets to log.")).filter(aClass -> PacketUtils.getS2CPackets().contains(aClass)).build());
        this.runInMainMenu = true;
    }

    @EventHandler(priority=-200)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (this.s2cPackets.get().contains(event.packet.getClass())) {
            this.info(this.packetToString(event.packet), new Object[0]);
        }
    }

    @EventHandler(priority=-200)
    private void onSendPacket(PacketEvent.Send event) {
        if (this.c2sPackets.get().contains(event.packet.getClass())) {
            this.info(this.packetToString(event.packet), new Object[0]);
        }
    }

    private String packetToString(Packet<?> packet) {
        try {
            return DebugModule.reflectiveToString(packet);
        }
        catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private static String reflectiveToString(Object object) throws Exception {
        return DebugModule.reflectiveToString(object, 0);
    }

    private static String reflectiveToString(Object object, int indentLevel) throws Exception {
        if (object == null) {
            return "null";
        }
        if (PRIMITIVE_TYPES.contains(object.getClass())) {
            if (object instanceof String) {
                return "\"" + String.valueOf(object) + "\"";
            }
            return object.toString();
        }
        if (object.getClass().isEnum()) {
            return object.toString();
        }
        if (object instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)object;
            return "{ dir: " + bhr.getSide().toString() + ", bpos: " + bhr.getBlockPos().toShortString() + ", pos: " + bhr.getPos().toString() + ", inside: " + Boolean.toString(bhr.isInsideBlock()) + "}";
        }
        if (!(object instanceof Packet)) {
            return object.toString();
        }
        StringBuilder sb = new StringBuilder();
        String indent = "    ".repeat(indentLevel);
        sb.append(indent).append(object.getClass().getSimpleName()).append(" {\n");
        for (Method method : object.getClass().getMethods()) {
            String methodName = method.getName();
            if (method.getParameterCount() != 0 || method.getReturnType() == Void.TYPE || Modifier.isStatic(method.getModifiers())) continue;
            Object value = method.invoke(object, new Object[0]);
            sb.append(indent).append("    ").append(method.getReturnType().getSimpleName()).append(" ").append(methodName).append(": ").append(DebugModule.reflectiveToString(value, indentLevel + 1)).append("\n");
        }
        sb.append(indent).append("}");
        return sb.toString();
    }
}

