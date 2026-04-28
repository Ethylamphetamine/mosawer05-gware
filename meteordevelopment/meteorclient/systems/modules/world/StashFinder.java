/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.reflect.TypeToken
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.block.entity.AbstractFurnaceBlockEntity
 *  net.minecraft.block.entity.BarrelBlockEntity
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.BlockEntityType
 *  net.minecraft.block.entity.ChestBlockEntity
 *  net.minecraft.block.entity.DispenserBlockEntity
 *  net.minecraft.block.entity.EnderChestBlockEntity
 *  net.minecraft.block.entity.HopperBlockEntity
 *  net.minecraft.block.entity.ShulkerBoxBlockEntity
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.toast.Toast
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.ChunkPos
 */
package meteordevelopment.meteorclient.systems.modules.world;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StorageBlockListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.Toast;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class StashFinder
extends Module {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final SettingGroup sgGeneral;
    private final Setting<List<BlockEntityType<?>>> storageBlocks;
    private final Setting<Integer> minimumStorageCount;
    private final Setting<Integer> minimumDistance;
    private final Setting<Boolean> sendNotifications;
    private final Setting<Mode> notificationMode;
    public List<Chunk> chunks;

    public StashFinder() {
        super(Categories.World, "stash-finder", "Searches loaded chunks for storage blocks. Saves to <your minecraft folder>/meteor-client");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.storageBlocks = this.sgGeneral.add(((StorageBlockListSetting.Builder)((StorageBlockListSetting.Builder)new StorageBlockListSetting.Builder().name("storage-blocks")).description("Select the storage blocks to search for.")).defaultValue(StorageBlockListSetting.STORAGE_BLOCKS).build());
        this.minimumStorageCount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-storage-count")).description("The minimum amount of storage blocks in a chunk to record the chunk.")).defaultValue(4)).min(1).sliderMin(1).build());
        this.minimumDistance = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-distance")).description("The minimum distance you must be from spawn to record a certain chunk.")).defaultValue(0)).min(0).sliderMax(10000).build());
        this.sendNotifications = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("notifications")).description("Sends Minecraft notifications when new stashes are found.")).defaultValue(true)).build());
        this.notificationMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("notification-mode")).description("The mode to use for notifications.")).defaultValue(Mode.Both)).visible(this.sendNotifications::get)).build());
        this.chunks = new ArrayList<Chunk>();
    }

    @Override
    public void onActivate() {
        this.load();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        double chunkZAbs;
        double chunkXAbs = Math.abs(event.chunk().getPos().x * 16);
        if (Math.sqrt(chunkXAbs * chunkXAbs + (chunkZAbs = (double)Math.abs(event.chunk().getPos().z * 16)) * chunkZAbs) < (double)this.minimumDistance.get().intValue()) {
            return;
        }
        Chunk chunk = new Chunk(event.chunk().getPos());
        for (BlockEntity blockEntity : event.chunk().getBlockEntities().values()) {
            if (!this.storageBlocks.get().contains(blockEntity.getType())) continue;
            if (blockEntity instanceof ChestBlockEntity) {
                ++chunk.chests;
                continue;
            }
            if (blockEntity instanceof BarrelBlockEntity) {
                ++chunk.barrels;
                continue;
            }
            if (blockEntity instanceof ShulkerBoxBlockEntity) {
                ++chunk.shulkers;
                continue;
            }
            if (blockEntity instanceof EnderChestBlockEntity) {
                ++chunk.enderChests;
                continue;
            }
            if (blockEntity instanceof AbstractFurnaceBlockEntity) {
                ++chunk.furnaces;
                continue;
            }
            if (blockEntity instanceof DispenserBlockEntity) {
                ++chunk.dispensersDroppers;
                continue;
            }
            if (!(blockEntity instanceof HopperBlockEntity)) continue;
            ++chunk.hoppers;
        }
        if (chunk.getTotal() >= this.minimumStorageCount.get()) {
            Chunk prevChunk = null;
            int i = this.chunks.indexOf(chunk);
            if (i < 0) {
                this.chunks.add(chunk);
            } else {
                prevChunk = this.chunks.set(i, chunk);
            }
            this.saveJson();
            this.saveCsv();
            if (!(!this.sendNotifications.get().booleanValue() || chunk.equals(prevChunk) && chunk.countsEqual(prevChunk))) {
                switch (this.notificationMode.get().ordinal()) {
                    case 0: {
                        this.info("Found stash at (highlight)%s(default), (highlight)%s(default).", chunk.x, chunk.z);
                        break;
                    }
                    case 1: {
                        this.mc.getToastManager().add((Toast)new MeteorToast(Items.CHEST, this.title, "Found Stash!"));
                        break;
                    }
                    case 2: {
                        this.info("Found stash at (highlight)%s(default), (highlight)%s(default).", chunk.x, chunk.z);
                        this.mc.getToastManager().add((Toast)new MeteorToast(Items.CHEST, this.title, "Found Stash!"));
                    }
                }
            }
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        this.chunks.sort(Comparator.comparingInt(value -> -value.getTotal()));
        WVerticalList list = theme.verticalList();
        WButton clear = list.add(theme.button("Clear")).widget();
        WTable table = new WTable();
        if (!this.chunks.isEmpty()) {
            list.add(table);
        }
        clear.action = () -> {
            this.chunks.clear();
            table.clear();
        };
        this.fillTable(theme, table);
        return list;
    }

    private void fillTable(GuiTheme theme, WTable table) {
        for (Chunk chunk : this.chunks) {
            table.add(theme.label("Pos: " + chunk.x + ", " + chunk.z));
            table.add(theme.label("Total: " + chunk.getTotal()));
            WButton open = table.add(theme.button("Open")).widget();
            open.action = () -> this.mc.setScreen((Screen)new ChunkScreen(theme, chunk));
            WButton gotoBtn = table.add(theme.button("Goto")).widget();
            gotoBtn.action = () -> PathManagers.get().moveTo(new BlockPos(chunk.x, 0, chunk.z), true);
            WMinus delete = table.add(theme.minus()).widget();
            delete.action = () -> {
                if (this.chunks.remove(chunk)) {
                    table.clear();
                    this.fillTable(theme, table);
                    this.saveJson();
                    this.saveCsv();
                }
            };
            table.row();
        }
    }

    private void load() {
        block9: {
            Reader reader;
            File file;
            boolean loaded;
            block8: {
                loaded = false;
                file = this.getJsonFile();
                if (file.exists()) {
                    try {
                        reader = new FileReader(file);
                        this.chunks = (List)GSON.fromJson(reader, new TypeToken<List<Chunk>>(){}.getType());
                        ((InputStreamReader)reader).close();
                        for (Chunk chunk : this.chunks) {
                            chunk.calculatePos();
                        }
                        loaded = true;
                    }
                    catch (Exception ignored) {
                        if (this.chunks != null) break block8;
                        this.chunks = new ArrayList<Chunk>();
                    }
                }
            }
            file = this.getCsvFile();
            if (!loaded && file.exists()) {
                try {
                    String line;
                    reader = new BufferedReader(new FileReader(file));
                    ((BufferedReader)reader).readLine();
                    while ((line = ((BufferedReader)reader).readLine()) != null) {
                        String[] values = line.split(" ");
                        Chunk chunk = new Chunk(new ChunkPos(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
                        chunk.chests = Integer.parseInt(values[2]);
                        chunk.shulkers = Integer.parseInt(values[3]);
                        chunk.enderChests = Integer.parseInt(values[4]);
                        chunk.furnaces = Integer.parseInt(values[5]);
                        chunk.dispensersDroppers = Integer.parseInt(values[6]);
                        chunk.hoppers = Integer.parseInt(values[7]);
                        this.chunks.add(chunk);
                    }
                    ((BufferedReader)reader).close();
                }
                catch (Exception ignored) {
                    if (this.chunks != null) break block9;
                    this.chunks = new ArrayList<Chunk>();
                }
            }
        }
    }

    private void saveCsv() {
        try {
            File file = this.getCsvFile();
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write("X,Z,Chests,Barrels,Shulkers,EnderChests,Furnaces,DispensersDroppers,Hoppers\n");
            for (Chunk chunk : this.chunks) {
                chunk.write(writer);
            }
            ((Writer)writer).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveJson() {
        try {
            File file = this.getJsonFile();
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            GSON.toJson(this.chunks, (Appendable)writer);
            ((Writer)writer).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getJsonFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "stashes"), Utils.getFileWorldName()), "stashes.json");
    }

    private File getCsvFile() {
        return new File(new File(new File(MeteorClient.FOLDER, "stashes"), Utils.getFileWorldName()), "stashes.csv");
    }

    @Override
    public String getInfoString() {
        return String.valueOf(this.chunks.size());
    }

    public static enum Mode {
        Chat,
        Toast,
        Both;

    }

    public static class Chunk {
        private static final StringBuilder sb = new StringBuilder();
        public ChunkPos chunkPos;
        public transient int x;
        public transient int z;
        public int chests;
        public int barrels;
        public int shulkers;
        public int enderChests;
        public int furnaces;
        public int dispensersDroppers;
        public int hoppers;

        public Chunk(ChunkPos chunkPos) {
            this.chunkPos = chunkPos;
            this.calculatePos();
        }

        public void calculatePos() {
            this.x = this.chunkPos.x * 16 + 8;
            this.z = this.chunkPos.z * 16 + 8;
        }

        public int getTotal() {
            return this.chests + this.barrels + this.shulkers + this.enderChests + this.furnaces + this.dispensersDroppers + this.hoppers;
        }

        public void write(Writer writer) throws IOException {
            sb.setLength(0);
            sb.append(this.x).append(',').append(this.z).append(',');
            sb.append(this.chests).append(',').append(this.barrels).append(',').append(this.shulkers).append(',').append(this.enderChests).append(',').append(this.furnaces).append(',').append(this.dispensersDroppers).append(',').append(this.hoppers).append('\n');
            writer.write(sb.toString());
        }

        public boolean countsEqual(Chunk c) {
            if (c == null) {
                return false;
            }
            return this.chests != c.chests || this.barrels != c.barrels || this.shulkers != c.shulkers || this.enderChests != c.enderChests || this.furnaces != c.furnaces || this.dispensersDroppers != c.dispensersDroppers || this.hoppers != c.hoppers;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Chunk chunk = (Chunk)o;
            return Objects.equals(this.chunkPos, chunk.chunkPos);
        }

        public int hashCode() {
            return Objects.hash(this.chunkPos);
        }
    }

    private static class ChunkScreen
    extends WindowScreen {
        private final Chunk chunk;

        public ChunkScreen(GuiTheme theme, Chunk chunk) {
            super(theme, "Chunk at " + chunk.x + ", " + chunk.z);
            this.chunk = chunk;
        }

        @Override
        public void initWidgets() {
            WTable t = this.add(this.theme.table()).expandX().widget();
            t.add(this.theme.label("Total:"));
            t.add(this.theme.label("" + this.chunk.getTotal()));
            t.row();
            t.add(this.theme.horizontalSeparator()).expandX();
            t.row();
            t.add(this.theme.label("Chests:"));
            t.add(this.theme.label("" + this.chunk.chests));
            t.row();
            t.add(this.theme.label("Barrels:"));
            t.add(this.theme.label("" + this.chunk.barrels));
            t.row();
            t.add(this.theme.label("Shulkers:"));
            t.add(this.theme.label("" + this.chunk.shulkers));
            t.row();
            t.add(this.theme.label("Ender Chests:"));
            t.add(this.theme.label("" + this.chunk.enderChests));
            t.row();
            t.add(this.theme.label("Furnaces:"));
            t.add(this.theme.label("" + this.chunk.furnaces));
            t.row();
            t.add(this.theme.label("Dispensers and droppers:"));
            t.add(this.theme.label("" + this.chunk.dispensersDroppers));
            t.row();
            t.add(this.theme.label("Hoppers:"));
            t.add(this.theme.label("" + this.chunk.hoppers));
        }
    }
}

