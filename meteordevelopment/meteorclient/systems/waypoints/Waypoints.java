/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.waypoints;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.files.StreamUtils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

public class Waypoints
extends System<Waypoints>
implements Iterable<Waypoint> {
    public static final String[] BUILTIN_ICONS = new String[]{"square", "circle", "triangle", "star", "diamond", "skull"};
    public final Map<String, AbstractTexture> icons = new ConcurrentHashMap<String, AbstractTexture>();
    private final List<Waypoint> waypoints = Collections.synchronizedList(new ArrayList());

    public Waypoints() {
        super(null);
    }

    public static Waypoints get() {
        return Systems.get(Waypoints.class);
    }

    @Override
    public void init() {
        File iconsFolder = new File(new File(MeteorClient.FOLDER, "waypoints"), "icons");
        iconsFolder.mkdirs();
        for (String builtinIcon : BUILTIN_ICONS) {
            File iconFile = new File(iconsFolder, builtinIcon + ".png");
            if (iconFile.exists()) continue;
            this.copyIcon(iconFile);
        }
        File[] files = iconsFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.getName().endsWith(".png")) continue;
            try {
                String name = file.getName().replace(".png", "");
                NativeImageBackedTexture texture = new NativeImageBackedTexture(NativeImage.read((InputStream)new FileInputStream(file)));
                this.icons.put(name, (AbstractTexture)texture);
            }
            catch (IOException e) {
                MeteorClient.LOG.error("Failed to read a waypoint icon", (Throwable)e);
            }
        }
    }

    public boolean add(Waypoint waypoint) {
        if (this.waypoints.contains(waypoint)) {
            this.save();
            return true;
        }
        this.waypoints.add(waypoint);
        this.save();
        return false;
    }

    public boolean remove(Waypoint waypoint) {
        boolean removed = this.waypoints.remove(waypoint);
        if (removed) {
            this.save();
        }
        return removed;
    }

    public Waypoint get(String name) {
        for (Waypoint waypoint : this.waypoints) {
            if (!waypoint.name.get().equalsIgnoreCase(name)) continue;
            return waypoint;
        }
        return null;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        this.load();
    }

    @EventHandler(priority=-200)
    private void onGameDisconnected(GameLeftEvent event) {
        this.waypoints.clear();
    }

    public static boolean checkDimension(Waypoint waypoint) {
        Dimension waypointDim;
        Dimension playerDim = PlayerUtils.getDimension();
        if (playerDim == (waypointDim = waypoint.dimension.get())) {
            return true;
        }
        if (!waypoint.opposite.get().booleanValue()) {
            return false;
        }
        boolean playerOpp = playerDim == Dimension.Overworld || playerDim == Dimension.Nether;
        boolean waypointOpp = waypointDim == Dimension.Overworld || waypointDim == Dimension.Nether;
        return playerOpp && waypointOpp;
    }

    @Override
    public File getFile() {
        if (!Utils.canUpdate()) {
            return null;
        }
        return new File(new File(MeteorClient.FOLDER, "waypoints"), Utils.getFileWorldName() + ".nbt");
    }

    public boolean isEmpty() {
        return this.waypoints.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Waypoint> iterator() {
        return new WaypointIterator();
    }

    private void copyIcon(File file) {
        String path = "/assets/meteor-client/textures/icons/waypoints/" + file.getName();
        InputStream in = Waypoints.class.getResourceAsStream(path);
        if (in == null) {
            MeteorClient.LOG.error("Failed to read a resource: {}", (Object)path);
            return;
        }
        StreamUtils.copy(in, file);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("waypoints", (NbtElement)NbtUtils.listToTag(this.waypoints));
        return tag;
    }

    @Override
    public Waypoints fromTag(NbtCompound tag) {
        this.waypoints.clear();
        for (NbtElement waypointTag : tag.getList("waypoints", 10)) {
            this.waypoints.add(new Waypoint(waypointTag));
        }
        return this;
    }

    private final class WaypointIterator
    implements Iterator<Waypoint> {
        private final Iterator<Waypoint> it;

        private WaypointIterator() {
            this.it = Waypoints.this.waypoints.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.it.hasNext();
        }

        @Override
        public Waypoint next() {
            return this.it.next();
        }

        @Override
        public void remove() {
            this.it.remove();
            Waypoints.this.save();
        }
    }
}

