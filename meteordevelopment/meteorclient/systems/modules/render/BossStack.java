/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.HashMap;
import java.util.WeakHashMap;
import meteordevelopment.meteorclient.events.render.RenderBossBarEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

public class BossStack
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> stack;
    public final Setting<Boolean> hideName;
    private final Setting<Double> spacing;
    public static final WeakHashMap<ClientBossBar, Integer> barMap = new WeakHashMap();

    public BossStack() {
        super(Categories.Render, "boss-stack", "Stacks boss bars to make your HUD less cluttered.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.stack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stack")).description("Stacks boss bars and adds a counter to the text.")).defaultValue(true)).build());
        this.hideName = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hide-name")).description("Hides the names of boss bars.")).defaultValue(false)).build());
        this.spacing = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("bar-spacing")).description("The spacing reduction between each boss bar.")).defaultValue(10.0).min(0.0).build());
    }

    @EventHandler
    private void onFetchText(RenderBossBarEvent.BossText event) {
        if (this.hideName.get().booleanValue()) {
            event.name = Text.of((String)"");
            return;
        }
        if (barMap.isEmpty() || !this.stack.get().booleanValue()) {
            return;
        }
        ClientBossBar bar = event.bossBar;
        Integer integer = barMap.get(bar);
        barMap.remove(bar);
        if (integer != null && !this.hideName.get().booleanValue()) {
            event.name = event.name.copy().append(" x" + integer);
        }
    }

    @EventHandler
    private void onSpaceBars(RenderBossBarEvent.BossSpacing event) {
        event.spacing = this.spacing.get().intValue();
    }

    @EventHandler
    private void onGetBars(RenderBossBarEvent.BossIterator event) {
        if (this.stack.get().booleanValue()) {
            HashMap chosenBarMap = new HashMap();
            event.iterator.forEachRemaining(bar -> {
                String name = bar.getName().getString();
                if (chosenBarMap.containsKey(name)) {
                    barMap.compute((ClientBossBar)chosenBarMap.get(name), (clientBossBar, integer) -> integer == null ? 2 : integer + 1);
                } else {
                    chosenBarMap.put(name, bar);
                }
            });
            event.iterator = chosenBarMap.values().iterator();
        }
    }
}

