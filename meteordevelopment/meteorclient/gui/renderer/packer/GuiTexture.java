/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.renderer.packer;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.gui.renderer.packer.TextureRegion;

public class GuiTexture {
    private final List<TextureRegion> regions = new ArrayList<TextureRegion>(2);

    void add(TextureRegion region) {
        this.regions.add(region);
    }

    public TextureRegion get(double width, double height) {
        double targetDiagonal = Math.sqrt(width * width + height * height);
        double closestDifference = Double.MAX_VALUE;
        TextureRegion closestRegion = null;
        for (TextureRegion region : this.regions) {
            double difference = Math.abs(targetDiagonal - region.diagonal);
            if (!(difference < closestDifference)) continue;
            closestDifference = difference;
            closestRegion = region;
        }
        return closestRegion;
    }
}

