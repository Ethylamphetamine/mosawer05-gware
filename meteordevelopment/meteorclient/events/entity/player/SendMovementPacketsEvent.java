/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.entity.player;

public class SendMovementPacketsEvent {

    public static class Post {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }

    public static class Rotation {
        public float yaw;
        public float pitch;
        public boolean forceFull;
        public boolean forceFullOnRotate;
        public boolean forceRotation = false;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static class Pre {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() {
            return INSTANCE;
        }
    }
}

