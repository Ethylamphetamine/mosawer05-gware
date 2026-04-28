/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.DeathScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.EditSystemScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class WaypointsModule
extends Module {
    private static final Color GRAY = new Color(200, 200, 200);
    private static final Color TEXT = new Color(255, 255, 255);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDeathPosition;
    public final Setting<Integer> textRenderDistance;
    private final Setting<Integer> waypointFadeDistance;
    private final Setting<Integer> maxDeathPositions;
    private final Setting<Boolean> dpChat;
    private final SimpleDateFormat dateFormat;

    public WaypointsModule() {
        super(Categories.Render, "waypoints", "Allows you to create waypoints.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDeathPosition = this.settings.createGroup("Death Position");
        this.textRenderDistance = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("text-render-distance")).description("Maximum distance from the center of the screen at which text will be rendered.")).defaultValue(100)).min(0).sliderMax(200).build());
        this.waypointFadeDistance = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("waypoint-fade-distance")).description("The distance to a waypoint at which it begins to start fading.")).defaultValue(20)).sliderRange(0, 100).min(0).build());
        this.maxDeathPositions = this.sgDeathPosition.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-death-positions")).description("The amount of death positions to save, 0 to disable")).defaultValue(0)).min(0).sliderMax(20).onChanged(this::cleanDeathWPs)).build());
        this.dpChat = this.sgDeathPosition.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat")).description("Send a chat message with your position once you die")).defaultValue(false)).build());
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        TextRenderer text = TextRenderer.get();
        Vector3d center = new Vector3d((double)this.mc.getWindow().getFramebufferWidth() / 2.0, (double)this.mc.getWindow().getFramebufferHeight() / 2.0, 0.0);
        int textRenderDist = this.textRenderDistance.get();
        for (Waypoint waypoint : Waypoints.get()) {
            if (!waypoint.visible.get().booleanValue() || !Waypoints.checkDimension(waypoint)) continue;
            BlockPos blockPos = waypoint.getPos();
            Vector3d pos = new Vector3d((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
            double dist = PlayerUtils.distanceToCamera(pos.x, pos.y, pos.z);
            if (dist > (double)waypoint.maxVisible.get().intValue() || !NametagUtils.to2D(pos, waypoint.scale.get() - 0.2)) continue;
            double distToCenter = pos.distance((Vector3dc)center);
            double a = 1.0;
            if (dist < (double)this.waypointFadeDistance.get().intValue() && (a = (dist - (double)this.waypointFadeDistance.get().intValue() / 2.0) / ((double)this.waypointFadeDistance.get().intValue() / 2.0)) < 0.01) continue;
            NametagUtils.begin(pos);
            waypoint.renderIcon(-16.0, -16.0, a, 32.0);
            if (distToCenter <= (double)textRenderDist) {
                int preTextA = WaypointsModule.TEXT.a;
                WaypointsModule.TEXT.a *= (int)a;
                text.begin();
                text.render(waypoint.name.get(), -text.getWidth(waypoint.name.get()) / 2.0, -16.0 - text.getHeight(), TEXT, true);
                String distText = String.format("%d blocks", (int)Math.round(dist));
                text.render(distText, -text.getWidth(distText) / 2.0, 16.0, TEXT, true);
                text.end();
                WaypointsModule.TEXT.a = preTextA;
            }
            NametagUtils.end();
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) {
            return;
        }
        if (!event.isCancelled()) {
            this.addDeath(this.mc.player.getPos());
        }
    }

    public void addDeath(Vec3d deathPos) {
        String time = this.dateFormat.format(new Date());
        if (this.dpChat.get().booleanValue()) {
            MutableText text = Text.literal((String)"Died at ");
            text.append((Text)ChatUtils.formatCoords(deathPos));
            text.append(String.format(" on %s.", time));
            this.info((Text)text);
        }
        if (this.maxDeathPositions.get() > 0) {
            Waypoint waypoint = new Waypoint.Builder().name("Death " + time).icon("skull").pos(BlockPos.ofFloored((Position)deathPos).up(2)).dimension(PlayerUtils.getDimension()).build();
            Waypoints.get().add(waypoint);
        }
        this.cleanDeathWPs(this.maxDeathPositions.get());
    }

    private void cleanDeathWPs(int max) {
        int oldWpC = 0;
        Iterator<Waypoint> it = Waypoints.get().iterator();
        while (it.hasNext()) {
            Waypoint wp = it.next();
            if (!wp.name.get().startsWith("Death ") || !wp.icon.get().equals("skull") || ++oldWpC <= max) continue;
            it.remove();
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (!Utils.canUpdate()) {
            return theme.label("You need to be in a world.");
        }
        WTable table = theme.table();
        this.initTable(theme, table);
        return table;
    }

    private void initTable(GuiTheme theme, WTable table) {
        table.clear();
        for (Waypoint waypoint : Waypoints.get()) {
            boolean validDim = Waypoints.checkDimension(waypoint);
            table.add(new WIcon(waypoint));
            WLabel name = table.add(theme.label(waypoint.name.get())).expandCellX().widget();
            if (!validDim) {
                name.color = GRAY;
            }
            WCheckbox visible = table.add(theme.checkbox(waypoint.visible.get())).widget();
            visible.action = () -> {
                waypoint.visible.set(visible.checked);
                Waypoints.get().save();
            };
            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> this.mc.setScreen((Screen)new EditWaypointScreen(theme, waypoint, () -> this.initTable(theme, table)));
            if (validDim) {
                WButton gotoB = table.add(theme.button("Goto")).widget();
                gotoB.action = () -> {
                    if (PathManagers.get().isPathing()) {
                        PathManagers.get().stop();
                    }
                    PathManagers.get().moveTo(waypoint.getPos());
                };
            }
            WMinus remove = table.add(theme.minus()).widget();
            remove.action = () -> {
                Waypoints.get().remove(waypoint);
                this.initTable(theme, table);
            };
            table.row();
        }
        table.add(theme.horizontalSeparator()).expandX();
        table.row();
        WButton create = table.add(theme.button("Create")).expandX().widget();
        create.action = () -> this.mc.setScreen((Screen)new EditWaypointScreen(theme, null, () -> this.initTable(theme, table)));
    }

    private static class WIcon
    extends WWidget {
        private final Waypoint waypoint;

        public WIcon(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        @Override
        protected void onCalculateSize() {
            double s;
            this.width = s = this.theme.scale(32.0);
            this.height = s;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.post(() -> this.waypoint.renderIcon(this.x, this.y, 1.0, this.width));
        }
    }

    private static class EditWaypointScreen
    extends EditSystemScreen<Waypoint> {
        public EditWaypointScreen(GuiTheme theme, Waypoint value, Runnable reload) {
            super(theme, value, reload);
        }

        @Override
        public Waypoint create() {
            return new Waypoint.Builder().pos(MinecraftClient.getInstance().player.getBlockPos().up(2)).dimension(PlayerUtils.getDimension()).build();
        }

        @Override
        public boolean save() {
            if (((Waypoint)this.value).name.get().isBlank()) {
                return false;
            }
            Waypoints.get().add((Waypoint)this.value);
            return true;
        }

        @Override
        public Settings getSettings() {
            return ((Waypoint)this.value).settings;
        }
    }
}

