/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.Hand;

public record FindItemResult(int slot, int count) {
    public boolean found() {
        return this.slot != -1;
    }

    public Hand getHand() {
        if (this.slot == 45) {
            return Hand.OFF_HAND;
        }
        if (this.slot == MeteorClient.mc.player.getInventory().selectedSlot) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public boolean isMainHand() {
        return this.getHand() == Hand.MAIN_HAND;
    }

    public boolean isOffhand() {
        return this.getHand() == Hand.OFF_HAND;
    }

    public boolean isHotbar() {
        return this.slot >= 0 && this.slot <= 8;
    }

    public boolean isMain() {
        return this.slot >= 9 && this.slot <= 35;
    }

    public boolean isArmor() {
        return this.slot >= 36 && this.slot <= 39;
    }
}

