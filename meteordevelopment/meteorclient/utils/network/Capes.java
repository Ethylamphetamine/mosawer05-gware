/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.utils.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class Capes {
    private static final String CAPE_OWNERS_URL = "https://meteorclient.com/api/capeowners";
    private static final String CAPES_URL = "https://meteorclient.com/api/capes";
    private static final Map<UUID, String> OWNERS = new HashMap<UUID, String>();
    private static final Map<String, String> URLS = new HashMap<String, String>();
    private static final Map<String, Cape> TEXTURES = new HashMap<String, Cape>();
    private static final List<Cape> TO_REGISTER = new ArrayList<Cape>();
    private static final List<Cape> TO_RETRY = new ArrayList<Cape>();
    private static final List<Cape> TO_REMOVE = new ArrayList<Cape>();

    private Capes() {
    }

    @PreInit(dependencies={MeteorExecutor.class})
    public static void init() {
        OWNERS.clear();
        URLS.clear();
        TEXTURES.clear();
        TO_REGISTER.clear();
        TO_RETRY.clear();
        TO_REMOVE.clear();
        MeteorExecutor.execute(() -> {
            Stream<String> lines = Http.get(CAPE_OWNERS_URL).exceptionHandler(e -> MeteorClient.LOG.error("Could not load capes: " + e.getMessage())).sendLines();
            if (lines == null) {
                return;
            }
            lines.forEach(s -> {
                String[] split = s.split(" ");
                if (split.length >= 2) {
                    OWNERS.put(UUID.fromString(split[0]), split[1]);
                    if (!TEXTURES.containsKey(split[1])) {
                        TEXTURES.put(split[1], new Cape(split[1]));
                    }
                }
            });
            lines = Http.get(CAPES_URL).sendLines();
            if (lines != null) {
                lines.forEach(s -> {
                    String[] split = s.split(" ");
                    if (split.length >= 2 && !URLS.containsKey(split[0])) {
                        URLS.put(split[0], split[1]);
                    }
                });
            }
        });
        MeteorClient.EVENT_BUS.subscribe(Capes.class);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private static void onTick(TickEvent.Post event) {
        List<Cape> list = TO_REGISTER;
        synchronized (list) {
            for (Cape cape : TO_REGISTER) {
                cape.register();
            }
            TO_REGISTER.clear();
        }
        list = TO_RETRY;
        synchronized (list) {
            TO_RETRY.removeIf(Cape::tick);
        }
        list = TO_REMOVE;
        synchronized (list) {
            for (Cape cape : TO_REMOVE) {
                URLS.remove(cape.name);
                TEXTURES.remove(cape.name);
                TO_REGISTER.remove(cape);
                TO_RETRY.remove(cape);
            }
            TO_REMOVE.clear();
        }
    }

    public static Identifier get(PlayerEntity player) {
        String capeName = OWNERS.get(player.getUuid());
        if (capeName != null) {
            Cape cape = TEXTURES.get(capeName);
            if (cape == null) {
                return null;
            }
            if (cape.isDownloaded()) {
                return cape.getIdentifier();
            }
            cape.download();
            return null;
        }
        return null;
    }

    private static class Cape {
        private static int COUNT = 0;
        private final String name;
        private final Identifier identifier = MeteorClient.identifier("capes/" + COUNT++);
        private boolean downloaded;
        private boolean downloading;
        private NativeImage img;
        private int retryTimer;

        public Cape(String name) {
            this.name = name;
        }

        public Identifier getIdentifier() {
            return this.identifier;
        }

        public void download() {
            if (this.downloaded || this.downloading || this.retryTimer > 0) {
                return;
            }
            this.downloading = true;
            MeteorExecutor.execute(() -> {
                try {
                    String url = URLS.get(this.name);
                    if (url == null) {
                        List<Cape> list = TO_REMOVE;
                        synchronized (list) {
                            TO_REMOVE.add(this);
                            this.downloading = false;
                            return;
                        }
                    }
                    InputStream in = Http.get(url).sendInputStream();
                    if (in == null) {
                        List<Cape> list = TO_RETRY;
                        synchronized (list) {
                            TO_RETRY.add(this);
                            this.retryTimer = 200;
                            this.downloading = false;
                            return;
                        }
                    }
                    this.img = NativeImage.read((InputStream)in);
                    List<Cape> list = TO_REGISTER;
                    synchronized (list) {
                        TO_REGISTER.add(this);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void register() {
            MeteorClient.mc.getTextureManager().registerTexture(this.identifier, (AbstractTexture)new NativeImageBackedTexture(this.img));
            this.img = null;
            this.downloading = false;
            this.downloaded = true;
        }

        public boolean tick() {
            if (this.retryTimer > 0) {
                --this.retryTimer;
            } else {
                this.download();
                return true;
            }
            return false;
        }

        public boolean isDownloaded() {
            return this.downloaded;
        }
    }
}

