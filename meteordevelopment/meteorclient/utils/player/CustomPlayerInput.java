/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.input.Input
 */
package meteordevelopment.meteorclient.utils.player;

import net.minecraft.client.input.Input;

public class CustomPlayerInput
extends Input {
    public void tick(boolean slowDown, float f) {
        float f2 = this.pressingForward == this.pressingBack ? 0.0f : (this.movementForward = this.pressingForward ? 1.0f : -1.0f);
        float f3 = this.pressingLeft == this.pressingRight ? 0.0f : (this.movementSideways = this.pressingLeft ? 1.0f : -1.0f);
        if (this.sneaking) {
            this.movementForward = (float)((double)this.movementForward * 0.3);
            this.movementSideways = (float)((double)this.movementSideways * 0.3);
        }
    }

    public void stop() {
        this.pressingForward = false;
        this.pressingBack = false;
        this.pressingRight = false;
        this.pressingLeft = false;
        this.jumping = false;
        this.sneaking = false;
    }
}

