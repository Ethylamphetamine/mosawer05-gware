/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class AntiAnvil
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> rotate;

    public AntiAnvil() {
        super(Categories.Combat, "anti-anvil", "Automatically prevents Auto Anvil by placing between you and the anvil.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.swing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swings your hand client-side when placing.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Makes you rotate when placing.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        BlockPos pos;
        int i = 0;
        while (!(!((double)i <= this.mc.player.getBlockInteractionRange()) || this.mc.world.getBlockState(pos = this.mc.player.getBlockPos().add(0, i + 3, 0)).getBlock() == Blocks.ANVIL && this.mc.world.getBlockState(pos.down()).isAir() && BlockUtils.place(pos.down(), InvUtils.findInHotbar(Items.OBSIDIAN), this.rotate.get(), 15, this.swing.get(), true))) {
            ++i;
        }
    }
}

