/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.themes.gonbleware.widgets.pressable;

import meteordevelopment.meteorclient.gui.themes.gonbleware.GonbleWareWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WGonbleWareFavorite
extends WFavorite
implements GonbleWareWidget {
    public WGonbleWareFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return this.theme().favoriteColor.get();
    }
}

