/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package meteordevelopment.meteorclient.utils.render;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.accounts.TexturesJson;
import meteordevelopment.meteorclient.systems.accounts.UuidToProfileResponse;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;

public class PlayerHeadUtils {
    public static PlayerHeadTexture STEVE_HEAD;

    private PlayerHeadUtils() {
    }

    @PostInit
    public static void init() {
        STEVE_HEAD = new PlayerHeadTexture();
    }

    public static PlayerHeadTexture fetchHead(UUID id) {
        if (id == null) {
            return null;
        }
        String url = PlayerHeadUtils.getSkinUrl(id);
        return url != null ? new PlayerHeadTexture(url) : null;
    }

    public static String getSkinUrl(UUID id) {
        UuidToProfileResponse res2 = (UuidToProfileResponse)Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + String.valueOf(id)).exceptionHandler(e -> MeteorClient.LOG.error("Could not contact mojang session servers.", (Throwable)e)).sendJson((Type)((Object)UuidToProfileResponse.class));
        if (res2 == null) {
            return null;
        }
        String base64Textures = res2.getPropertyValue("textures");
        if (base64Textures == null) {
            return null;
        }
        TexturesJson textures = (TexturesJson)new Gson().fromJson(new String(Base64.getDecoder().decode(base64Textures)), TexturesJson.class);
        if (textures.textures.SKIN == null) {
            return null;
        }
        return textures.textures.SKIN.url;
    }
}

