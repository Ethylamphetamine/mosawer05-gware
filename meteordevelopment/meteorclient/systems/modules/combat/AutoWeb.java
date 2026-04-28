/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class AutoWeb
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> pauseEat;
    private final Setting<Double> range;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> placeHead;
    private final Setting<Boolean> placeFeet;
    private final Setting<Boolean> placeCrawling;
    private PlayerEntity target;

    public AutoWeb() {
        super(Categories.Combat, "auto-web", "Automatically places webs on other players.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat")).description("Pauses while eating.")).defaultValue(true)).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("The maximum distance to target players.")).defaultValue(5.0).range(0.0, 5.0).sliderMax(5.0).build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to filter targets within range.")).defaultValue(SortPriority.LowestDistance)).build());
        this.placeHead = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-head")).description("Places webs in the target's upper hitbox.")).defaultValue(true)).build());
        this.placeFeet = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-feet")).description("Places webs in the target's lower hitbox.")).defaultValue(false)).build());
        this.placeCrawling = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-crawling")).description("Places webs in the taget's lower hitbox when they're swimming.")).defaultValue(true)).build());
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(this.target, this.range.get())) {
            this.target = TargetUtils.getPlayerTarget(this.range.get(), this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.range.get())) {
                return;
            }
        }
        ArrayList<BlockPos> placePoses = new ArrayList<BlockPos>();
        if (this.placeHead.get().booleanValue()) {
            placePoses.add(this.target.getBlockPos().up());
        }
        if ((this.placeFeet.get().booleanValue() || this.placeCrawling.get().booleanValue() && this.target.isCrawling()) && !PlayerUtils.isPlayerPhased(this.target)) {
            placePoses.add(this.target.getBlockPos());
        }
        if (this.pauseEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        if (!MeteorClient.BLOCK.beginPlacement(placePoses, Items.COBWEB)) {
            return;
        }
        placePoses.forEach(blockPos -> MeteorClient.BLOCK.placeBlock(Items.COBWEB, (BlockPos)blockPos));
        MeteorClient.BLOCK.endPlacement();
    }
}

