/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractPressurePlateBlock
 *  net.minecraft.block.AnvilBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.ButtonBlock
 *  net.minecraft.client.gui.screen.ingame.AnvilScreen
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class AutoAnvil
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<SortPriority> priority;
    private final Setting<Integer> height;
    private final Setting<Integer> delay;
    private final Setting<Boolean> placeButton;
    private final Setting<Boolean> multiPlace;
    private final Setting<Boolean> toggleOnBreak;
    private final Setting<Boolean> rotate;
    private PlayerEntity target;
    private int timer;

    public AutoAnvil() {
        super(Categories.Combat, "auto-anvil", "Automatically places anvils above players to destroy helmets.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("The radius in which players get targeted.")).defaultValue(4.0).min(0.0).sliderMax(5.0).build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.height = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("height")).description("The height to place anvils at.")).defaultValue(2)).range(0, 5).sliderMax(5).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The delay in between anvil placements.")).defaultValue(10)).min(0).sliderMax(50).build());
        this.placeButton = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-at-feet")).description("Automatically places a button or pressure plate at the targets feet to break the anvils.")).defaultValue(true)).build());
        this.multiPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("multi-place")).description("Places multiple anvils at once..")).defaultValue(true)).build());
        this.toggleOnBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-break")).description("Toggles when the target's helmet slot is empty.")).defaultValue(false)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically rotates towards the position anvils/pressure plates/buttons are placed.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.timer = 0;
        this.target = null;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AnvilScreen) {
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.toggleOnBreak.get().booleanValue() && this.target != null && this.target.getInventory().getArmorStack(3).isEmpty()) {
            this.error("Target head slot is empty... disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (TargetUtils.isBadTarget(this.target, this.range.get())) {
            this.target = TargetUtils.getPlayerTarget(this.range.get(), this.priority.get());
            if (TargetUtils.isBadTarget(this.target, this.range.get())) {
                return;
            }
        }
        if (this.placeButton.get().booleanValue()) {
            FindItemResult floorBlock = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem((Item)itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem((Item)itemStack.getItem()) instanceof ButtonBlock);
            BlockUtils.place(this.target.getBlockPos(), floorBlock, this.rotate.get(), 0, false);
        }
        if (this.timer >= this.delay.get()) {
            this.timer = 0;
            FindItemResult anvil = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem((Item)itemStack.getItem()) instanceof AnvilBlock);
            if (!anvil.found()) {
                return;
            }
            for (int i = this.height.get().intValue(); i > 1; --i) {
                BlockPos blockPos = this.target.getBlockPos().up().add(0, i, 0);
                for (int j = 0; j < i && this.mc.world.getBlockState(this.target.getBlockPos().up(j + 1)).isReplaceable(); ++j) {
                }
                if (!BlockUtils.place(blockPos, anvil, this.rotate.get(), 0) || this.multiPlace.get().booleanValue()) {
                    continue;
                }
                break;
            }
        } else {
            ++this.timer;
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }
}

