/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerRadarHud
extends HudElement {
    public static final HudElementInfo<PlayerRadarHud> INFO = new HudElementInfo<PlayerRadarHud>(Hud.GROUP, "player-radar", "Displays players in your visual range.", PlayerRadarHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Integer> limit;
    private final Setting<Boolean> distance;
    private final Setting<Boolean> totemPops;
    private final Setting<Boolean> friends;
    private final Setting<Boolean> shadow;
    private final Setting<SettingColor> primaryColor;
    private final Setting<SettingColor> secondaryColor;
    private final Setting<SettingColor> totemPopColor;
    private final Setting<Alignment> alignment;
    private final Setting<Integer> border;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final List<AbstractClientPlayerEntity> players;

    public PlayerRadarHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.limit = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("limit")).description("The max number of players to show.")).defaultValue(10)).min(1).sliderRange(1, 20).build());
        this.distance = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance")).description("Shows the distance to the player next to their name.")).defaultValue(false)).build());
        this.totemPops = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-totem-pops")).description("Whether to show totem pops or not.")).defaultValue(true)).build());
        this.friends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-friends")).description("Whether to show friends or not.")).defaultValue(true)).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow behind text.")).defaultValue(true)).build());
        this.primaryColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("primary-color")).description("Primary color.")).defaultValue(new SettingColor()).build());
        this.secondaryColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("secondary-color")).description("Secondary color.")).defaultValue(new SettingColor(175, 175, 175)).build());
        this.totemPopColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("totem-pop-color")).description("Totem pop color.")).defaultValue(new SettingColor(225, 120, 20)).build());
        this.alignment = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("alignment")).description("Horizontal alignment.")).defaultValue(Alignment.Auto)).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(0)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.players = new ArrayList<AbstractClientPlayerEntity>();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    @Override
    protected double alignX(double width, Alignment alignment) {
        return this.box.alignX(this.getWidth() - this.border.get() * 2, width, alignment);
    }

    @Override
    public void tick(HudRenderer renderer) {
        double width = renderer.textWidth("Players:", this.shadow.get(), this.getScale());
        double height = renderer.textHeight(this.shadow.get(), this.getScale());
        if (MeteorClient.mc.world == null) {
            this.setSize(width, height);
            return;
        }
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (playerEntity.equals((Object)MeteorClient.mc.player) || !this.friends.get().booleanValue() && Friends.get().isFriend(playerEntity)) continue;
            Object text = playerEntity.getName().getString();
            if (this.distance.get().booleanValue()) {
                text = (String)text + String.format("(%sm)", Math.round(MeteorClient.mc.getCameraEntity().distanceTo((Entity)playerEntity)));
            }
            if (this.totemPops.get().booleanValue() && MeteorClient.INFO.getPops((Entity)playerEntity) > 0) {
                text = (String)text + -MeteorClient.INFO.getPops((Entity)playerEntity);
            }
            width = Math.max(width, renderer.textWidth((String)text, this.shadow.get(), this.getScale()));
            height += renderer.textHeight(this.shadow.get(), this.getScale()) + 2.0;
        }
        this.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double y = this.y + this.border.get();
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
        renderer.text("Players:", (double)(this.x + this.border.get()) + this.alignX(renderer.textWidth("Players:", this.shadow.get(), this.getScale()), this.alignment.get()), y, this.secondaryColor.get(), this.shadow.get(), this.getScale());
        if (MeteorClient.mc.world == null) {
            return;
        }
        double spaceWidth = renderer.textWidth(" ", this.shadow.get(), this.getScale());
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (playerEntity.equals((Object)MeteorClient.mc.player) || !this.friends.get().booleanValue() && Friends.get().isFriend(playerEntity)) continue;
            String text = playerEntity.getName().getString();
            Color color = PlayerUtils.getPlayerColor(playerEntity, this.primaryColor.get());
            String distanceText = null;
            String totemPopsText = null;
            double width = renderer.textWidth(text, this.shadow.get(), this.getScale());
            if (this.distance.get().booleanValue()) {
                width += spaceWidth;
            }
            if (this.totemPops.get().booleanValue() && MeteorClient.INFO.getPops((Entity)playerEntity) > 0) {
                width += spaceWidth;
            }
            if (this.distance.get().booleanValue()) {
                distanceText = String.format("(%sm)", Math.round(MeteorClient.mc.getCameraEntity().distanceTo((Entity)playerEntity)));
                width += renderer.textWidth(distanceText, this.shadow.get(), this.getScale());
            }
            if (this.totemPops.get().booleanValue() && MeteorClient.INFO.getPops((Entity)playerEntity) > 0) {
                totemPopsText = "" + -MeteorClient.INFO.getPops((Entity)playerEntity);
                width += renderer.textWidth(totemPopsText, this.shadow.get(), this.getScale());
            }
            double x = (double)(this.x + this.border.get()) + this.alignX(width, this.alignment.get());
            x = renderer.text(text, x, y += renderer.textHeight(this.shadow.get(), this.getScale()) + 2.0, color, this.shadow.get());
            if (this.distance.get().booleanValue()) {
                renderer.text(distanceText, x + spaceWidth, y, this.secondaryColor.get(), this.shadow.get(), this.getScale());
            }
            if (!this.totemPops.get().booleanValue() || totemPopsText == null) continue;
            renderer.text(totemPopsText, x + spaceWidth, y, this.totemPopColor.get(), this.shadow.get(), this.getScale());
        }
    }

    private List<AbstractClientPlayerEntity> getPlayers() {
        this.players.clear();
        this.players.addAll(MeteorClient.mc.world.getPlayers());
        if (this.players.size() > this.limit.get()) {
            this.players.subList(this.limit.get() - 1, this.players.size() - 1).clear();
        }
        this.players.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(MeteorClient.mc.getCameraEntity())));
        return this.players;
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : -1.0;
    }
}

