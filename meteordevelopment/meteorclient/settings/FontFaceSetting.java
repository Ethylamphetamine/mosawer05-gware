/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 */
package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

public class FontFaceSetting
extends Setting<FontFace> {
    public FontFaceSetting(String name, String description, FontFace defaultValue, Consumer<FontFace> onChanged, Consumer<Setting<FontFace>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected FontFace parseImpl(String str) {
        String[] split = str.replace(" ", "").split("-");
        if (split.length != 2) {
            return null;
        }
        for (FontFamily family : Fonts.FONT_FAMILIES) {
            if (!family.getName().replace(" ", "").equals(split[0])) continue;
            try {
                return family.get(FontInfo.Type.valueOf(split[1]));
            }
            catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<String> getSuggestions() {
        return List.of("JetBrainsMono-Regular", "Arial-Bold");
    }

    @Override
    protected boolean isValueValid(FontFace value) {
        if (value == null) {
            return false;
        }
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (!fontFamily.hasType(value.info.type())) continue;
            return true;
        }
        return false;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.putString("family", ((FontFace)this.get()).info.family());
        tag.putString("type", ((FontFace)this.get()).info.type().toString());
        return tag;
    }

    @Override
    protected FontFace load(NbtCompound tag) {
        FontInfo.Type type;
        String family = tag.getString("family");
        try {
            type = FontInfo.Type.valueOf(tag.getString("type"));
        }
        catch (IllegalArgumentException ignored) {
            this.set(Fonts.DEFAULT_FONT);
            return (FontFace)this.get();
        }
        boolean changed = false;
        for (FontFamily fontFamily : Fonts.FONT_FAMILIES) {
            if (!fontFamily.getName().equals(family)) continue;
            this.set(fontFamily.get(type));
            changed = true;
        }
        if (!changed) {
            this.set(Fonts.DEFAULT_FONT);
        }
        return (FontFace)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, FontFace, FontFaceSetting> {
        public Builder() {
            super(Fonts.DEFAULT_FONT);
        }

        @Override
        public FontFaceSetting build() {
            return new FontFaceSetting(this.name, this.description, (FontFace)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

