/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.renderer.text;

public record FontInfo(String family, Type type) {
    @Override
    public String toString() {
        return this.family + " " + String.valueOf((Object)this.type);
    }

    public boolean equals(FontInfo info) {
        if (this == info) {
            return true;
        }
        if (info == null || this.family == null || this.type == null) {
            return false;
        }
        return this.family.equals(info.family) && this.type == info.type;
    }

    public static enum Type {
        Regular,
        Bold,
        Italic,
        BoldItalic;


        public static Type fromString(String str) {
            return switch (str) {
                case "Bold" -> Bold;
                case "Italic" -> Italic;
                case "Bold Italic", "BoldItalic" -> BoldItalic;
                default -> Regular;
            };
        }

        public String toString() {
            return switch (this.ordinal()) {
                case 1 -> "Bold";
                case 2 -> "Italic";
                case 3 -> "Bold Italic";
                default -> "Regular";
            };
        }
    }
}

