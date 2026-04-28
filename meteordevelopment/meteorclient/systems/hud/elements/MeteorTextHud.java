/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class MeteorTextHud {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<TextHud>(Hud.GROUP, "text", "Displays arbitrary text with Starscript.", MeteorTextHud::create);
    public static final HudElementInfo.Preset FPS;
    public static final HudElementInfo.Preset TPS;
    public static final HudElementInfo.Preset PING;
    public static final HudElementInfo.Preset SPEED;
    public static final HudElementInfo.Preset GAME_MODE;
    public static final HudElementInfo.Preset DURABILITY;
    public static final HudElementInfo.Preset POSITION;
    public static final HudElementInfo.Preset OPPOSITE_POSITION;
    public static final HudElementInfo.Preset LOOKING_AT;
    public static final HudElementInfo.Preset LOOKING_AT_WITH_POSITION;
    public static final HudElementInfo.Preset BREAKING_PROGRESS;
    public static final HudElementInfo.Preset SERVER;
    public static final HudElementInfo.Preset BIOME;
    public static final HudElementInfo.Preset WORLD_TIME;
    public static final HudElementInfo.Preset REAL_TIME;
    public static final HudElementInfo.Preset ROTATION;
    public static final HudElementInfo.Preset MODULE_ENABLED;
    public static final HudElementInfo.Preset MODULE_ENABLED_WITH_INFO;
    public static final HudElementInfo.Preset WATERMARK;
    public static final HudElementInfo.Preset BARITONE;

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static HudElementInfo.Preset addPreset(String title, String text, int updateDelay) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) {
                textHud.text.set(text);
            }
            if (updateDelay != -1) {
                textHud.updateDelay.set(updateDelay);
            }
        });
    }

    private static HudElementInfo.Preset addPreset(String title, String text) {
        return MeteorTextHud.addPreset(title, text, -1);
    }

    static {
        MeteorTextHud.addPreset("Empty", null);
        FPS = MeteorTextHud.addPreset("FPS", "FPS: #1{fps}", 0);
        TPS = MeteorTextHud.addPreset("TPS", "TPS: #1{round(server.tps, 1)}");
        PING = MeteorTextHud.addPreset("Ping", "Ping: #1{ping}");
        SPEED = MeteorTextHud.addPreset("Speed", "Speed: #1{round(player.speed, 1)}", 0);
        GAME_MODE = MeteorTextHud.addPreset("Game mode", "Game mode: #1{player.gamemode}", 0);
        DURABILITY = MeteorTextHud.addPreset("Durability", "Durability: #1{player.hand_or_offhand.durability}");
        POSITION = MeteorTextHud.addPreset("Position", "Pos: #1{floor(camera.pos.x)}, {floor(camera.pos.y)}, {floor(camera.pos.z)}", 0);
        OPPOSITE_POSITION = MeteorTextHud.addPreset("Opposite Position", "{player.opposite_dimension != \"End\" ? player.opposite_dimension + \":\" : \"\"} #1{player.opposite_dimension != \"End\" ? \"\" + floor(camera.opposite_dim_pos.x) + \", \" + floor(camera.opposite_dim_pos.y) + \", \" + floor(camera.opposite_dim_pos.z) : \"\"}", 0);
        LOOKING_AT = MeteorTextHud.addPreset("Looking at", "Looking at: #1{crosshair_target.value}", 0);
        LOOKING_AT_WITH_POSITION = MeteorTextHud.addPreset("Looking at with position", "Looking at: #1{crosshair_target.value} {crosshair_target.type != \"miss\" ? \"(\" + \"\" + floor(crosshair_target.value.pos.x) + \", \" + floor(crosshair_target.value.pos.y) + \", \" + floor(crosshair_target.value.pos.z) + \")\" : \"\"}", 0);
        BREAKING_PROGRESS = MeteorTextHud.addPreset("Breaking progress", "Breaking progress: #1{round(player.breaking_progress * 100)}%", 0);
        SERVER = MeteorTextHud.addPreset("Server", "Server: #1{server}");
        BIOME = MeteorTextHud.addPreset("Biome", "Biome: #1{player.biome}", 0);
        WORLD_TIME = MeteorTextHud.addPreset("World time", "Time: #1{server.time}");
        REAL_TIME = MeteorTextHud.addPreset("Real time", "Time: #1{time}");
        ROTATION = MeteorTextHud.addPreset("Rotation", "{camera.direction} #1({round(camera.yaw, 1)}, {round(camera.pitch, 1)})", 0);
        MODULE_ENABLED = MeteorTextHud.addPreset("Module enabled", "Kill Aura: {meteor.is_module_active(\"kill-aura\") ? #2 \"ON\" : #3 \"OFF\"}", 0);
        MODULE_ENABLED_WITH_INFO = MeteorTextHud.addPreset("Module enabled with info", "Kill Aura: {meteor.is_module_active(\"kill-aura\") ? #2 \"ON\" : #3 \"OFF\"} #1{meteor.get_module_info(\"kill-aura\")}", 0);
        WATERMARK = MeteorTextHud.addPreset("Watermark", "{meteor.name} #1{meteor.version}");
        BARITONE = MeteorTextHud.addPreset("Baritone", "Baritone: #1{baritone.process_name}");
    }
}

