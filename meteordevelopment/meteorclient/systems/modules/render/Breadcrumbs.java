/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.dimension.DimensionType
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayDeque;
import java.util.Queue;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.dimension.DimensionType;

public class Breadcrumbs
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<SettingColor> color;
    private final Setting<Integer> maxSections;
    private final Setting<Double> sectionLength;
    private final Pool<Section> sectionPool;
    private final Queue<Section> sections;
    private Section section;
    private DimensionType lastDimension;

    public Breadcrumbs() {
        super(Categories.Render, "breadcrumbs", "Displays a trail behind where you have walked.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The color of the Breadcrumbs trail.")).defaultValue(new SettingColor(225, 25, 25)).build());
        this.maxSections = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-sections")).description("The maximum number of sections.")).defaultValue(1000)).min(1).sliderRange(1, 5000).build());
        this.sectionLength = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("section-length")).description("The section length in blocks.")).defaultValue(0.5).min(0.0).sliderMax(1.0).build());
        this.sectionPool = new Pool<Section>(() -> new Section());
        this.sections = new ArrayDeque<Section>();
    }

    @Override
    public void onActivate() {
        this.section = this.sectionPool.get();
        this.section.set1();
        this.lastDimension = this.mc.world.getDimension();
    }

    @Override
    public void onDeactivate() {
        for (Section section : this.sections) {
            this.sectionPool.free(section);
        }
        this.sections.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.lastDimension != this.mc.world.getDimension()) {
            for (Section sec : this.sections) {
                this.sectionPool.free(sec);
            }
            this.sections.clear();
        }
        if (this.isFarEnough(this.section.x1, this.section.y1, this.section.z1)) {
            Section section;
            this.section.set2();
            if (this.sections.size() >= this.maxSections.get() && (section = this.sections.poll()) != null) {
                this.sectionPool.free(section);
            }
            this.sections.add(this.section);
            this.section = this.sectionPool.get();
            this.section.set1();
        }
        this.lastDimension = this.mc.world.getDimension();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        int iLast = -1;
        for (Section section : this.sections) {
            if (iLast == -1) {
                iLast = event.renderer.lines.vec3(section.x1, section.y1, section.z1).color(this.color.get()).next();
            }
            int i = event.renderer.lines.vec3(section.x2, section.y2, section.z2).color(this.color.get()).next();
            event.renderer.lines.line(iLast, i);
            iLast = i;
        }
    }

    private boolean isFarEnough(double x, double y, double z) {
        return Math.abs(this.mc.player.getX() - x) >= this.sectionLength.get() || Math.abs(this.mc.player.getY() - y) >= this.sectionLength.get() || Math.abs(this.mc.player.getZ() - z) >= this.sectionLength.get();
    }

    private class Section {
        public float x1;
        public float y1;
        public float z1;
        public float x2;
        public float y2;
        public float z2;

        private Section() {
        }

        public void set1() {
            this.x1 = (float)((Breadcrumbs)Breadcrumbs.this).mc.player.getX();
            this.y1 = (float)((Breadcrumbs)Breadcrumbs.this).mc.player.getY();
            this.z1 = (float)((Breadcrumbs)Breadcrumbs.this).mc.player.getZ();
        }

        public void set2() {
            this.x2 = (float)((Breadcrumbs)Breadcrumbs.this).mc.player.getX();
            this.y2 = (float)((Breadcrumbs)Breadcrumbs.this).mc.player.getY();
            this.z2 = (float)((Breadcrumbs)Breadcrumbs.this).mc.player.getZ();
        }

        public void render(Render3DEvent event) {
            event.renderer.line(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2, Breadcrumbs.this.color.get());
        }
    }
}

