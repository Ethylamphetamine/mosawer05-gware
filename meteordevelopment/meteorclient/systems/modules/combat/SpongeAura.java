/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Collections;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class SpongeAura
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<SortPriority> priority;
    private final Setting<Integer> placeDelay;
    private final Setting<Boolean> onlyWhenAbove;
    private PlayerEntity target;
    private int placeTimer;

    public SpongeAura() {
        super(Categories.Combat, "sponge-aura", "Places sponges under enemies in water.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The maximum distance to target players. This aura only places underneath players feet.")).defaultValue(6.0).min(0.0).sliderMax(8.0).build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.LowestDistance)).build());
        this.placeDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("Delay between placing sponges in ticks.")).defaultValue(12)).min(0).sliderMax(20).build());
        this.onlyWhenAbove = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-when-above")).description("Only places sponges if the target feet-pos is above your feet-pos.")).defaultValue(true)).build());
        this.placeTimer = 0;
    }

    @Override
    public void onDeactivate() {
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player == null || this.mc.world == null) {
            this.target = null;
            return;
        }
        if (this.placeTimer > 0) {
            --this.placeTimer;
            return;
        }
        this.target = TargetUtils.getPlayerTarget(this.range.get(), this.priority.get());
        if (this.target == null) {
            return;
        }
        if (Friends.get().isFriend(this.target)) {
            this.target = null;
            return;
        }
        if (this.onlyWhenAbove.get().booleanValue() && this.mc.player.getBlockY() >= this.target.getBlockY()) {
            return;
        }
        BlockPos targetPos = this.target.getBlockPos().down(2);
        if (!this.mc.world.getBlockState(targetPos).isOf(Blocks.WATER)) {
            return;
        }
        if (!BlockUtils.canPlace(targetPos)) {
            return;
        }
        if (this.placeSponge(targetPos)) {
            this.placeTimer = this.placeDelay.get();
        }
    }

    private boolean placeSponge(BlockPos pos) {
        FindItemResult hot = InvUtils.findInHotbar(Items.SPONGE);
        if (hot.found()) {
            if (!MeteorClient.BLOCK.beginPlacement(Collections.singletonList(pos), Items.SPONGE)) {
                return false;
            }
            boolean placed = MeteorClient.BLOCK.placeBlock(Items.SPONGE, pos);
            MeteorClient.BLOCK.endPlacement();
            return placed;
        }
        FindItemResult inv = InvUtils.find(Items.SPONGE);
        if (!inv.found()) {
            return false;
        }
        int invSlot = inv.slot();
        int hotbarSlot = this.mc.player.getInventory().selectedSlot;
        boolean placed = false;
        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        if (MeteorClient.BLOCK.beginPlacement(Collections.singletonList(pos), Items.SPONGE)) {
            placed = MeteorClient.BLOCK.placeBlock(Items.SPONGE, pos);
            MeteorClient.BLOCK.endPlacement();
        }
        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        return placed;
    }
}

