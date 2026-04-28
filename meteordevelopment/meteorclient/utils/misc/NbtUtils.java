/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtSizeTracker
 *  org.apache.commons.io.output.ByteArrayOutputStream
 */
package meteordevelopment.meteorclient.utils.misc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class NbtUtils {
    private NbtUtils() {
    }

    public static <T extends ISerializable<?>> NbtList listToTag(Iterable<T> list) {
        NbtList tag = new NbtList();
        for (ISerializable item : list) {
            tag.add((Object)item.toTag());
        }
        return tag;
    }

    public static <T> List<T> listFromTag(NbtList tag, ToValue<T> toItem) {
        ArrayList<T> list = new ArrayList<T>(tag.size());
        for (NbtElement itemTag : tag) {
            T value = toItem.toValue(itemTag);
            if (value == null) continue;
            list.add(value);
        }
        return list;
    }

    public static <K, V extends ISerializable<?>> NbtCompound mapToTag(Map<K, V> map) {
        NbtCompound tag = new NbtCompound();
        for (K key : map.keySet()) {
            tag.put(key.toString(), (NbtElement)((ISerializable)map.get(key)).toTag());
        }
        return tag;
    }

    public static <K, V> Map<K, V> mapFromTag(NbtCompound tag, ToKey<K> toKey, ToValue<V> toValue) {
        HashMap<K, V> map = new HashMap<K, V>(tag.getSize());
        for (String key : tag.getKeys()) {
            map.put(toKey.toKey(key), toValue.toValue(tag.get(key)));
        }
        return map;
    }

    public static boolean toClipboard(System<?> system) {
        return NbtUtils.toClipboard(system.getName(), system.toTag());
    }

    public static boolean toClipboard(String name, NbtCompound nbtCompound) {
        String preClipboard = MeteorClient.mc.keyboard.getClipboard();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed((NbtCompound)nbtCompound, (OutputStream)byteArrayOutputStream);
            MeteorClient.mc.keyboard.setClipboard(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return true;
        }
        catch (Exception e) {
            MeteorClient.LOG.error(String.format("Error copying %s NBT to clipboard!", name));
            ((OkPrompt)((OkPrompt)((OkPrompt)OkPrompt.create().title(String.format("Error copying %s NBT to clipboard!", name))).message("This shouldn't happen, please report it.")).id("nbt-copying")).show();
            MeteorClient.mc.keyboard.setClipboard(preClipboard);
            return false;
        }
    }

    public static boolean fromClipboard(System<?> system) {
        NbtCompound clipboard = NbtUtils.fromClipboard(system.toTag());
        if (clipboard != null) {
            system.fromTag(clipboard);
            return true;
        }
        return false;
    }

    public static NbtCompound fromClipboard(NbtCompound schema) {
        try {
            byte[] data = Base64.getDecoder().decode(MeteorClient.mc.keyboard.getClipboard().trim());
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            NbtCompound pasted = NbtIo.readCompressed((InputStream)new DataInputStream(bis), (NbtSizeTracker)NbtSizeTracker.ofUnlimitedBytes());
            for (String key : schema.getKeys()) {
                if (pasted.getKeys().contains(key)) continue;
                return null;
            }
            if (!pasted.getString("name").equals(schema.getString("name"))) {
                return null;
            }
            return pasted;
        }
        catch (Exception e) {
            MeteorClient.LOG.error("Invalid NBT data pasted!");
            ((OkPrompt)((OkPrompt)((OkPrompt)OkPrompt.create().title("Error pasting NBT data!")).message("Please check that the data you pasted is valid.")).id("nbt-pasting")).show();
            return null;
        }
    }

    public static interface ToValue<T> {
        public T toValue(NbtElement var1);
    }

    public static interface ToKey<T> {
        public T toKey(String var1);
    }
}

