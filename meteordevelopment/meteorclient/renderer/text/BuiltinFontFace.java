/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.renderer.text;

import java.io.InputStream;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.utils.render.FontUtils;

public class BuiltinFontFace
extends FontFace {
    private final String name;

    public BuiltinFontFace(FontInfo info, String name) {
        super(info);
        this.name = name;
    }

    @Override
    public InputStream toStream() {
        InputStream in = FontUtils.stream(this.name);
        if (in == null) {
            throw new RuntimeException("Failed to load builtin font " + this.name + ".");
        }
        return in;
    }

    @Override
    public String toString() {
        return super.toString() + " (builtin)";
    }
}

