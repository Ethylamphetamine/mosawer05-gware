/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  net.minecraft.util.Util$OperatingSystem
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.stb.STBTTFontinfo
 *  org.lwjgl.stb.STBTruetype
 */
package meteordevelopment.meteorclient.utils.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.BuiltinFontFace;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.renderer.text.SystemFontFace;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

public class FontUtils {
    private FontUtils() {
    }

    public static FontInfo getSysFontInfo(File file) {
        return FontUtils.getFontInfo(FontUtils.stream(file));
    }

    public static FontInfo getBuiltinFontInfo(String builtin) {
        return FontUtils.getFontInfo(FontUtils.stream(builtin));
    }

    public static FontInfo getFontInfo(InputStream stream) {
        if (stream == null) {
            return null;
        }
        byte[] bytes = Utils.readBytes(stream);
        if (bytes.length < 5) {
            return null;
        }
        if (bytes[0] != 0 || bytes[1] != 1 || bytes[2] != 0 || bytes[3] != 0 || bytes[4] != 0) {
            return null;
        }
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)bytes.length).put(bytes).flip();
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont((STBTTFontinfo)fontInfo, (ByteBuffer)buffer)) {
            return null;
        }
        ByteBuffer nameBuffer = STBTruetype.stbtt_GetFontNameString((STBTTFontinfo)fontInfo, (int)3, (int)1, (int)1033, (int)1);
        ByteBuffer typeBuffer = STBTruetype.stbtt_GetFontNameString((STBTTFontinfo)fontInfo, (int)3, (int)1, (int)1033, (int)2);
        if (typeBuffer == null || nameBuffer == null) {
            return null;
        }
        return new FontInfo(StandardCharsets.UTF_16.decode(nameBuffer).toString(), FontInfo.Type.fromString(StandardCharsets.UTF_16.decode(typeBuffer).toString()));
    }

    public static Set<String> getSearchPaths() {
        HashSet<String> paths = new HashSet<String>();
        paths.add(System.getProperty("java.home") + "/lib/fonts");
        for (File dir : FontUtils.getUFontDirs()) {
            if (!dir.exists()) continue;
            paths.add(dir.getAbsolutePath());
        }
        for (File dir : FontUtils.getSFontDirs()) {
            if (!dir.exists()) continue;
            paths.add(dir.getAbsolutePath());
        }
        return paths;
    }

    public static List<File> getUFontDirs() {
        return switch (Util.getOperatingSystem()) {
            case Util.OperatingSystem.WINDOWS -> List.of(new File(System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Windows\\Fonts"));
            case Util.OperatingSystem.OSX -> List.of(new File(System.getProperty("user.home") + "/Library/Fonts/"));
            default -> List.of(new File(System.getProperty("user.home") + "/.local/share/fonts"), new File(System.getProperty("user.home") + "/.fonts"));
        };
    }

    public static List<File> getSFontDirs() {
        return switch (Util.getOperatingSystem()) {
            case Util.OperatingSystem.WINDOWS -> List.of(new File(System.getenv("SystemRoot") + "\\Fonts"));
            case Util.OperatingSystem.OSX -> List.of(new File("/System/Library/Fonts/"));
            default -> List.of(new File("/usr/share/fonts/"));
        };
    }

    public static void loadBuiltin(List<FontFamily> fontList, String builtin) {
        FontInfo fontInfo = FontUtils.getBuiltinFontInfo(builtin);
        if (fontInfo == null) {
            return;
        }
        BuiltinFontFace fontFace = new BuiltinFontFace(fontInfo, builtin);
        if (!FontUtils.addFont(fontList, fontFace)) {
            MeteorClient.LOG.warn("Failed to load builtin font {}", (Object)fontFace);
        }
    }

    public static void loadSystem(List<FontFamily> fontList, File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(file -> file.isFile() && file.getName().endsWith(".ttf") || file.isDirectory());
        if (files == null) {
            return;
        }
        for (File file2 : files) {
            SystemFontFace fontFace;
            if (file2.isDirectory()) {
                FontUtils.loadSystem(fontList, file2);
                continue;
            }
            FontInfo fontInfo = FontUtils.getSysFontInfo(file2);
            if (fontInfo == null) continue;
            boolean isBuiltin = false;
            for (String builtinFont : Fonts.BUILTIN_FONTS) {
                if (!builtinFont.equals(fontInfo.family())) continue;
                isBuiltin = true;
                break;
            }
            if (isBuiltin || FontUtils.addFont(fontList, fontFace = new SystemFontFace(fontInfo, file2.toPath()))) continue;
            MeteorClient.LOG.warn("Failed to load system font {}", (Object)fontFace);
        }
    }

    public static boolean addFont(List<FontFamily> fontList, FontFace font) {
        if (font == null) {
            return false;
        }
        FontInfo info = font.info;
        FontFamily family = Fonts.getFamily(info.family());
        if (family == null) {
            family = new FontFamily(info.family());
            fontList.add(family);
        }
        if (family.hasType(info.type())) {
            return false;
        }
        return family.addFont(font);
    }

    public static InputStream stream(String builtin) {
        return FontUtils.class.getResourceAsStream("/assets/meteor-client/fonts/" + builtin + ".ttf");
    }

    public static InputStream stream(File file) {
        try {
            return new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

