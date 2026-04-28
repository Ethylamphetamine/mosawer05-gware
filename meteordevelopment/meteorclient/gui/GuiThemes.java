/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtIo
 */
package meteordevelopment.meteorclient.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

public class GuiThemes {
    private static final File FOLDER = new File(MeteorClient.FOLDER, "gui");
    private static final File THEMES_FOLDER = new File(FOLDER, "themes");
    private static final File FILE = new File(FOLDER, "gui.nbt");
    private static final List<GuiTheme> themes = new ArrayList<GuiTheme>();
    private static GuiTheme theme;
    private static boolean hadGonbleWareTheme;

    private GuiThemes() {
    }

    @PreInit
    public static void init() {
        GuiThemes.add(new GonbleWareGuiTheme());
        GuiThemes.add(new MeteorGuiTheme());
    }

    @PostInit
    public static void postInit() {
        NbtCompound tag;
        if (FILE.exists()) {
            try {
                tag = NbtIo.read((Path)FILE.toPath());
                if (tag != null) {
                    GuiThemes.select(tag.getString("currentTheme"));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (theme == null) {
            GuiThemes.select("GonbleWare");
        }
        if (FILE.exists()) {
            try {
                tag = NbtIo.read((Path)FILE.toPath());
                if (tag != null && !tag.getBoolean("hadGonbleWareTheme")) {
                    GuiThemes.select("GonbleWare");
                    hadGonbleWareTheme = true;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void add(GuiTheme theme) {
        Iterator<GuiTheme> it = themes.iterator();
        while (it.hasNext()) {
            if (!it.next().name.equals(theme.name)) continue;
            it.remove();
            MeteorClient.LOG.error("Theme with the name '{}' has already been added.", (Object)theme.name);
            break;
        }
        themes.add(theme);
    }

    public static void select(String name) {
        GuiTheme theme = null;
        for (GuiTheme t : themes) {
            if (!t.name.equals(name)) continue;
            theme = t;
            break;
        }
        if (theme != null) {
            GuiThemes.saveTheme();
            GuiThemes.theme = theme;
            try {
                NbtCompound tag;
                File file = new File(THEMES_FOLDER, GuiThemes.get().name + ".nbt");
                if (file.exists() && (tag = NbtIo.read((Path)file.toPath())) != null) {
                    GuiThemes.get().fromTag(tag);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            GuiThemes.saveGlobal();
        }
    }

    public static GuiTheme get() {
        return theme;
    }

    public static String[] getNames() {
        String[] names = new String[themes.size()];
        for (int i = 0; i < themes.size(); ++i) {
            names[i] = GuiThemes.themes.get((int)i).name;
        }
        return names;
    }

    private static void saveTheme() {
        if (GuiThemes.get() != null) {
            try {
                NbtCompound tag = GuiThemes.get().toTag();
                THEMES_FOLDER.mkdirs();
                NbtIo.write((NbtCompound)tag, (Path)new File(THEMES_FOLDER, GuiThemes.get().name + ".nbt").toPath());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveGlobal() {
        try {
            NbtCompound tag = new NbtCompound();
            tag.putString("currentTheme", GuiThemes.get().name);
            tag.putBoolean("hadGonbleWareTheme", hadGonbleWareTheme);
            FOLDER.mkdirs();
            NbtIo.write((NbtCompound)tag, (Path)FILE.toPath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        GuiThemes.saveTheme();
        GuiThemes.saveGlobal();
    }

    static {
        hadGonbleWareTheme = false;
    }
}

