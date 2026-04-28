/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.SlabBlock
 *  net.minecraft.item.Item
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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.Item;

public class AntiAnchor
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> swing;

    public AntiAnchor() {
        super(Categories.Combat, "anti-anchor", "Automatically prevents Anchor Aura by placing a slab on your head.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Makes you rotate when placing.")).defaultValue(true)).build());
        this.swing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swings your hand when placing.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.world.getBlockState(this.mc.player.getBlockPos().up(2)).getBlock() == Blocks.RESPAWN_ANCHOR && this.mc.world.getBlockState(this.mc.player.getBlockPos().up()).getBlock() == Blocks.AIR) {
            BlockUtils.place(this.mc.player.getBlockPos().add(0, 1, 0), InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem((Item)itemStack.getItem()) instanceof SlabBlock), this.rotate.get(), 15, this.swing.get(), false, true);
        }
    }
}

