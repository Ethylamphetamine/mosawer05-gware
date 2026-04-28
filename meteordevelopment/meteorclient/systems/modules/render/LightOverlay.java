/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.DrawMode;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.ShaderMesh;
import meteordevelopment.meteorclient.renderer.Shaders;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class LightOverlay
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    private final Setting<Integer> horizontalRange;
    private final Setting<Integer> verticalRange;
    private final Setting<Boolean> seeThroughBlocks;
    private final Setting<Boolean> newMobSpawnLightLevel;
    private final Setting<SettingColor> color;
    private final Setting<SettingColor> potentialColor;
    private final Pool<Cross> crossPool;
    private final List<Cross> crosses;
    private final Mesh mesh;

    public LightOverlay() {
        super(Categories.Render, "light-overlay", "Shows blocks where mobs can spawn.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.horizontalRange = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("horizontal-range")).description("Horizontal range in blocks.")).defaultValue(8)).min(0).build());
        this.verticalRange = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("vertical-range")).description("Vertical range in blocks.")).defaultValue(4)).min(0).build());
        this.seeThroughBlocks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("see-through-blocks")).description("Allows you to see the lines through blocks.")).defaultValue(false)).build());
        this.newMobSpawnLightLevel = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("new-mob-spawn-light-level")).description("Use the new (1.18+) mob spawn behavior")).defaultValue(true)).build());
        this.color = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("Color of places where mobs can currently spawn.")).defaultValue(new SettingColor(225, 25, 25)).build());
        this.potentialColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("potential-color")).description("Color of places where mobs can potentially spawn (eg at night).")).defaultValue(new SettingColor(225, 225, 25)).build());
        this.crossPool = new Pool<Cross>(() -> new Cross());
        this.crosses = new ArrayList<Cross>();
        this.mesh = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Lines, Mesh.Attrib.Vec3, Mesh.Attrib.Color);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Cross cross : this.crosses) {
            this.crossPool.free(cross);
        }
        this.crosses.clear();
        int spawnLightLevel = this.newMobSpawnLightLevel.get() != false ? 0 : 7;
        BlockIterator.register(this.horizontalRange.get(), this.verticalRange.get(), (blockPos, blockState) -> {
            switch (BlockUtils.isValidMobSpawn(blockPos, blockState, spawnLightLevel)) {
                case Potential: {
                    this.crosses.add(this.crossPool.get().set((BlockPos)blockPos, true));
                    break;
                }
                case Always: {
                    this.crosses.add(this.crossPool.get().set((BlockPos)blockPos, false));
                }
            }
        });
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.crosses.isEmpty()) {
            return;
        }
        this.mesh.depthTest = this.seeThroughBlocks.get() == false;
        this.mesh.begin();
        for (Cross cross : this.crosses) {
            cross.render();
        }
        this.mesh.end();
        this.mesh.render(event.matrices);
    }

    private void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        this.mesh.line(this.mesh.vec3(x1, y1, z1).color(color).next(), this.mesh.vec3(x2, y2, z2).color(color).next());
    }

    private class Cross {
        private double x;
        private double y;
        private double z;
        private boolean potential;

        private Cross() {
        }

        public Cross set(BlockPos blockPos, boolean potential) {
            this.x = blockPos.getX();
            this.y = (double)blockPos.getY() + 0.0075;
            this.z = blockPos.getZ();
            this.potential = potential;
            return this;
        }

        public void render() {
            Color c = this.potential ? (Color)LightOverlay.this.potentialColor.get() : (Color)LightOverlay.this.color.get();
            LightOverlay.this.line(this.x, this.y, this.z, this.x + 1.0, this.y, this.z + 1.0, c);
            LightOverlay.this.line(this.x + 1.0, this.y, this.z, this.x, this.y, this.z + 1.0, c);
        }
    }

    public static enum Spawn {
        Never,
        Potential,
        Always;

    }
}

