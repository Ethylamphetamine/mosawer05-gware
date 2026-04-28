/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.util.crash.CrashException
 *  org.apache.commons.io.FilenameUtils
 */
package meteordevelopment.meteorclient.systems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.files.StreamUtils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.crash.CrashException;
import org.apache.commons.io.FilenameUtils;

public abstract class System<T>
implements ISerializable<T> {
    private final String name;
    private File file;
    protected boolean isFirstInit;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);

    public System(String name) {
        this.name = name;
        if (name != null) {
            this.file = new File(MeteorClient.FOLDER, name + ".nbt");
            this.isFirstInit = !this.file.exists();
        }
    }

    public void init() {
    }

    public void save(File folder) {
        File file = this.getFile();
        if (file == null) {
            return;
        }
        NbtCompound tag = this.toTag();
        if (tag == null) {
            return;
        }
        try {
            File tempFile = File.createTempFile("meteor-client", file.getName());
            NbtIo.write((NbtCompound)tag, (Path)tempFile.toPath());
            if (folder != null) {
                file = new File(folder, file.getName());
            }
            file.getParentFile().mkdirs();
            StreamUtils.copy(tempFile, file);
            tempFile.delete();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        this.save(null);
    }

    public void load(File folder) {
        File file = this.getFile();
        if (file == null) {
            return;
        }
        try {
            if (folder != null) {
                file = new File(folder, file.getName());
            }
            if (file.exists()) {
                try {
                    this.fromTag(NbtIo.read((Path)file.toPath()));
                }
                catch (CrashException e) {
                    String backupName = FilenameUtils.removeExtension((String)file.getName()) + "-" + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + ".backup.nbt";
                    File backup = new File(file.getParentFile(), backupName);
                    StreamUtils.copy(file, backup);
                    MeteorClient.LOG.error("Error loading " + this.name + ". Possibly corrupted?");
                    MeteorClient.LOG.info("Saved settings backup to '" + String.valueOf(backup) + "'.");
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        this.load(null);
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public NbtCompound toTag() {
        return null;
    }

    @Override
    public T fromTag(NbtCompound tag) {
        return null;
    }
}

