/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.render.marker;

import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.marker.BaseMarker;
import meteordevelopment.meteorclient.systems.modules.render.marker.CuboidMarker;
import meteordevelopment.meteorclient.systems.modules.render.marker.Marker;
import meteordevelopment.meteorclient.systems.modules.render.marker.Sphere2dMarker;

public class MarkerFactory {
    private final Map<String, Factory> factories = new HashMap<String, Factory>();
    private final String[] names;

    public MarkerFactory() {
        this.factories.put("Cuboid", CuboidMarker::new);
        this.factories.put("Sphere-2D", Sphere2dMarker::new);
        this.names = new String[this.factories.size()];
        int i = 0;
        for (String key : this.factories.keySet()) {
            this.names[i++] = key;
        }
    }

    public String[] getNames() {
        return this.names;
    }

    public BaseMarker createMarker(String name) {
        if (this.factories.containsKey(name)) {
            BaseMarker marker = this.factories.get(name).create();
            marker.settings.registerColorSettings(Modules.get().get(Marker.class));
            return marker;
        }
        return null;
    }

    private static interface Factory {
        public BaseMarker create();
    }
}

