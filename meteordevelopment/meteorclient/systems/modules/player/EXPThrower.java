/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class EXPThrower
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> throwsPerTick;

    public EXPThrower() {
        super(Categories.Player, "exp-thrower", "Automatically throws XP bottles from your hotbar.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.throwsPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("throws-per-tick")).description("Number of xp bottles to throw every tick.")).defaultValue(1)).min(1).sliderMax(5).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult result = InvUtils.find(Items.EXPERIENCE_BOTTLE);
        if (!result.found() || this.mc.player.isUsingItem()) {
            return;
        }
        if (MeteorClient.SWAP.beginSwap(result, true)) {
            MeteorClient.ROTATION.requestRotation(this.mc.player.getYaw(), 90.0f, 0.0);
            for (int i = 0; i < this.throwsPerTick.get(); ++i) {
                this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
            }
            MeteorClient.SWAP.endSwap(true);
        }
    }
}

