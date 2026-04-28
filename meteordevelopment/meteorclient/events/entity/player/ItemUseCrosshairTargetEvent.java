/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.hit.HitResult
 */
package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.util.hit.HitResult;

public class ItemUseCrosshairTargetEvent {
    private static final ItemUseCrosshairTargetEvent INSTANCE = new ItemUseCrosshairTargetEvent();
    public HitResult target;

    public static ItemUseCrosshairTargetEvent get(HitResult target) {
        ItemUseCrosshairTargetEvent.INSTANCE.target = target;
        return INSTANCE;
    }
}

