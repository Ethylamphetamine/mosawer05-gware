/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.passive.TameableEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Vector3d;

public class EntityOwner
extends Module {
    private static final Color BACKGROUND = new Color(0, 0, 0, 75);
    private static final Color TEXT = new Color(255, 255, 255);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Vector3d pos;
    private final Map<UUID, String> uuidToName;

    public EntityOwner() {
        super(Categories.Render, "entity-owner", "Displays the name of the player who owns the entity you're looking at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the text.")).defaultValue(1.0).min(0.0).build());
        this.pos = new Vector3d();
        this.uuidToName = new HashMap<UUID, String>();
    }

    @Override
    public void onDeactivate() {
        this.uuidToName.clear();
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        for (Entity entity : this.mc.world.getEntities()) {
            TameableEntity tameable;
            UUID ownerUuid;
            if (!(entity instanceof TameableEntity) || (ownerUuid = (tameable = (TameableEntity)entity).getOwnerUuid()) == null) continue;
            Utils.set(this.pos, entity, event.tickDelta);
            this.pos.add(0.0, (double)entity.getEyeHeight(entity.getPose()) + 0.75, 0.0);
            if (!NametagUtils.to2D(this.pos, this.scale.get())) continue;
            this.renderNametag(this.getOwnerName(ownerUuid));
        }
    }

    private void renderNametag(String name) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        text.beginBig();
        double w = text.getWidth(name);
        double x = -w / 2.0;
        double y = -text.getHeight();
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1.0, y - 1.0, w + 2.0, text.getHeight() + 2.0, BACKGROUND);
        Renderer2D.COLOR.render(null);
        text.render(name, x, y, TEXT);
        text.end();
        NametagUtils.end();
    }

    private String getOwnerName(UUID uuid) {
        PlayerEntity player = this.mc.world.getPlayerByUuid(uuid);
        if (player != null) {
            return player.getName().getString();
        }
        String name = this.uuidToName.get(uuid);
        if (name != null) {
            return name;
        }
        MeteorExecutor.execute(() -> {
            if (this.isActive()) {
                ProfileResponse res = (ProfileResponse)Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "")).sendJson((Type)((Object)ProfileResponse.class));
                if (this.isActive()) {
                    if (res == null) {
                        this.uuidToName.put(uuid, "Failed to get name");
                    } else {
                        this.uuidToName.put(uuid, res.name);
                    }
                }
            }
        });
        name = "Retrieving";
        this.uuidToName.put(uuid, name);
        return name;
    }

    private static class ProfileResponse {
        public String name;

        private ProfileResponse() {
        }
    }
}

