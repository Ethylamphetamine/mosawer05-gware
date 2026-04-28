/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 */
package meteordevelopment.meteorclient.events.entity;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerDeathEvent {

    public static class Death
    extends PlayerDeathEvent {
        private static final Death INSTANCE = new Death();
        private PlayerEntity player;
        private int pops;

        public static Death get(PlayerEntity player, int pop) {
            Death.INSTANCE.player = player;
            Death.INSTANCE.pops = pop;
            return INSTANCE;
        }

        public PlayerEntity getPlayer() {
            return this.player;
        }

        public int getPops() {
            return this.pops;
        }
    }

    public static class TotemPop
    extends PlayerDeathEvent {
        private static final TotemPop INSTANCE = new TotemPop();
        private PlayerEntity player;
        private int pops;

        public static PlayerDeathEvent get(PlayerEntity player, int pop) {
            TotemPop.INSTANCE.player = player;
            TotemPop.INSTANCE.pops = pop;
            return INSTANCE;
        }

        public PlayerEntity getPlayer() {
            return this.player;
        }

        public int getPops() {
            return this.pops;
        }
    }
}

