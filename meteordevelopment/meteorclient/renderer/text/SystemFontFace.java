/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.renderer.text;

import java.io.InputStream;
import java.nio.file.Path;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.utils.render.FontUtils;

public class SystemFontFace
extends FontFace {
    private final Path path;

    public SystemFontFace(FontInfo info, Path path) {
        super(info);
        this.path = path;
    }

    @Override
    public InputStream toStream() {
        if (!this.path.toFile().exists()) {
            throw new RuntimeException("Tried to load font that no longer exists.");
        }
        InputStream in = FontUtils.stream(this.path.toFile());
        if (in == null) {
            throw new RuntimeException("Failed to load font from " + String.valueOf(this.path) + ".");
        }
        return in;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + this.path.toString() + ")";
    }
}

