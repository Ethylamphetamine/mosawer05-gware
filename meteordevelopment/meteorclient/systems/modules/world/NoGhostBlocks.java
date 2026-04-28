/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class NoGhostBlocks
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> breaking;
    public final Setting<Boolean> placing;

    public NoGhostBlocks() {
        super(Categories.World, "no-ghost-blocks", "Attempts to prevent ghost blocks arising.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.breaking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("breaking")).description("Whether to apply for block breaking actions.")).defaultValue(true)).build());
        this.placing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("placing")).description("Whether to apply for block placement actions.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onBreakBlock(BreakBlockEvent event) {
        if (this.mc.isInSingleplayer() || !this.breaking.get().booleanValue()) {
            return;
        }
        event.cancel();
        BlockState blockState = this.mc.world.getBlockState(event.blockPos);
        blockState.getBlock().onBreak((World)this.mc.world, event.blockPos, blockState, (PlayerEntity)this.mc.player);
    }

    @EventHandler
    private void onPlaceBlock(PlaceBlockEvent event) {
        if (!this.placing.get().booleanValue()) {
            return;
        }
        event.cancel();
    }
}

