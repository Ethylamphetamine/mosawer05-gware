/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class AutoCrawl
extends Module {
    private SilentMine silentMine;

    public AutoCrawl() {
        super(Categories.Movement, "auto-crawl", "Automatically mines your feet block when you are phased, using SilentMine.");
    }

    @Override
    public void onActivate() {
        this.silentMine = Modules.get().get(SilentMine.class);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        boolean isFullyPhased;
        if (this.mc.player == null || this.mc.world == null || this.silentMine == null) {
            return;
        }
        BlockPos feetPos = this.mc.player.getBlockPos();
        BlockPos headPos = feetPos.up();
        boolean isStanding = !this.mc.player.isCrawling() && !this.mc.player.isSwimming();
        boolean bl = isFullyPhased = !this.mc.world.getBlockState(feetPos).isReplaceable() && !this.mc.world.getBlockState(headPos).isReplaceable();
        if (isStanding && isFullyPhased) {
            boolean isAlreadyMining;
            boolean bl2 = isAlreadyMining = feetPos.equals((Object)this.silentMine.getRebreakBlockPos()) || feetPos.equals((Object)this.silentMine.getDelayedDestroyBlockPos());
            if (!isAlreadyMining && !this.mc.world.getBlockState(feetPos).isAir()) {
                this.silentMine.silentBreakBlock(feetPos, 10.0);
            }
        }
    }
}

