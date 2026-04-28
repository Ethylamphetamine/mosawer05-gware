/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.network.packet.Packet
 */
package meteordevelopment.meteorclient.settings;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.Packet;

public class PacketListSetting
extends Setting<Set<Class<? extends Packet<?>>>> {
    public final Predicate<Class<? extends Packet<?>>> filter;
    private static List<String> suggestions;

    public PacketListSetting(String name, String description, Set<Class<? extends Packet<?>>> defaultValue, Consumer<Set<Class<? extends Packet<?>>>> onChanged, Consumer<Setting<Set<Class<? extends Packet<?>>>>> onModuleActivated, Predicate<Class<? extends Packet<?>>> filter, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        this.value = new ObjectOpenHashSet((Collection)this.defaultValue);
    }

    @Override
    protected Set<Class<? extends Packet<?>>> parseImpl(String str) {
        String[] values = str.split(",");
        ObjectOpenHashSet packets = new ObjectOpenHashSet(values.length);
        try {
            for (String value : values) {
                Class<? extends Packet<?>> packet = PacketUtils.getPacket(value.trim());
                if (packet == null || this.filter != null && !this.filter.test(packet)) continue;
                packets.add(packet);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return packets;
    }

    @Override
    protected boolean isValueValid(Set<Class<? extends Packet<?>>> value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList<String>(PacketUtils.getC2SPackets().size() + PacketUtils.getS2CPackets().size());
            for (Class<? extends Packet<?>> clazz : PacketUtils.getC2SPackets()) {
                suggestions.add(PacketUtils.getName(clazz));
            }
            for (Class<? extends Packet<?>> clazz : PacketUtils.getS2CPackets()) {
                suggestions.add(PacketUtils.getName(clazz));
            }
        }
        return suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (Class packet : (Set)this.get()) {
            valueTag.add((Object)NbtString.of((String)PacketUtils.getName(packet)));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public Set<Class<? extends Packet<?>>> load(NbtCompound tag) {
        ((Set)this.get()).clear();
        NbtElement valueTag = tag.get("value");
        if (valueTag instanceof NbtList) {
            for (NbtElement t : (NbtList)valueTag) {
                Class<? extends Packet<?>> packet = PacketUtils.getPacket(t.asString());
                if (packet == null || this.filter != null && !this.filter.test(packet)) continue;
                ((Set)this.get()).add(packet);
            }
        }
        return (Set)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Set<Class<? extends Packet<?>>>, PacketListSetting> {
        private Predicate<Class<? extends Packet<?>>> filter;

        public Builder() {
            super(new ObjectOpenHashSet(0));
        }

        public Builder filter(Predicate<Class<? extends Packet<?>>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public PacketListSetting build() {
            return new PacketListSetting(this.name, this.description, (Set)this.defaultValue, this.onChanged, this.onModuleActivated, this.filter, this.visible);
        }
    }
}

