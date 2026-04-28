/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.events.render;

import java.util.Iterator;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

public class RenderBossBarEvent {

    public static class BossIterator {
        private static final BossIterator INSTANCE = new BossIterator();
        public Iterator<ClientBossBar> iterator;

        public static BossIterator get(Iterator<ClientBossBar> iterator) {
            BossIterator.INSTANCE.iterator = iterator;
            return INSTANCE;
        }
    }

    public static class BossSpacing {
        private static final BossSpacing INSTANCE = new BossSpacing();
        public int spacing;

        public static BossSpacing get(int spacing) {
            BossSpacing.INSTANCE.spacing = spacing;
            return INSTANCE;
        }
    }

    public static class BossText {
        private static final BossText INSTANCE = new BossText();
        public ClientBossBar bossBar;
        public Text name;

        public static BossText get(ClientBossBar bossBar, Text name) {
            BossText.INSTANCE.bossBar = bossBar;
            BossText.INSTANCE.name = name;
            return INSTANCE;
        }
    }
}

