/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTrap;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class AutoEXP
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> startBind;
    private final Setting<Boolean> chatFeedback;
    private final Setting<Boolean> replenish;
    private final Setting<Integer> slot;
    private final Setting<Boolean> debugFeedback;
    private final double DURABILITY_PER_BOTTLE = 14.0;
    private int ticks;
    private boolean active;
    private boolean keyUnpressed;
    private final List<Long> throwTimestamps;
    private int previousRawNeeded;
    private boolean waitingForLanding;

    public AutoEXP() {
        super(Categories.Combat, "auto-exp", "Predictive repair with Batch Sync.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.startBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("start-bind")).description("The key to start the repair process.")).defaultValue(Keybind.none())).build());
        this.chatFeedback = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("feedback")).description("Shows a message when starting/stopping.")).defaultValue(true)).build());
        this.replenish = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("replenish")).description("Automatically replenishes exp into a selected hotbar slot.")).defaultValue(true)).build());
        this.slot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("exp-slot")).description("The slot to replenish exp into (1-9).")).visible(this.replenish::get)).defaultValue(6)).range(1, 9).sliderRange(1, 9).build());
        this.debugFeedback = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("debug-feedback")).description("Sends technical calculation info.")).defaultValue(false)).build());
        this.DURABILITY_PER_BOTTLE = 14.0;
        this.ticks = 0;
        this.active = false;
        this.keyUnpressed = true;
        this.throwTimestamps = new ArrayList<Long>();
        this.previousRawNeeded = 0;
        this.waitingForLanding = false;
    }

    @Override
    public void onActivate() {
        this.ticks = 100;
        this.throwTimestamps.clear();
        this.active = false;
        this.waitingForLanding = false;
        this.keyUnpressed = true;
        this.previousRawNeeded = this.getBottlesNeeded();
    }

    @Override
    public void onDeactivate() {
        this.active = false;
    }

    @Override
    public String getInfoString() {
        long currentTime = System.currentTimeMillis();
        return String.format("%d", this.throwTimestamps.stream().filter(t -> (double)(currentTime - t) <= 500.0).count());
    }

    private int getLimit() {
        boolean surround = Modules.get().get(Surround.class).isActive();
        boolean autoTrap = Modules.get().get(AutoTrap.class).isActive();
        return surround || autoTrap ? 6 : 9;
    }

    private int getBottlesNeeded() {
        int totalMissingDurability = 0;
        for (ItemStack stack : this.mc.player.getInventory().armor) {
            if (!this.shouldRepair(stack)) continue;
            totalMissingDurability += stack.getDamage();
        }
        if (this.shouldRepair(this.mc.player.getMainHandStack())) {
            totalMissingDurability += this.mc.player.getMainHandStack().getDamage();
        }
        if (this.shouldRepair(this.mc.player.getOffHandStack())) {
            totalMissingDurability += this.mc.player.getOffHandStack().getDamage();
        }
        if (totalMissingDurability == 0) {
            return 0;
        }
        return (int)Math.ceil((double)totalMissingDurability / 14.0);
    }

    private boolean shouldRepair(ItemStack stack) {
        return !stack.isEmpty() && Utils.hasEnchantments(stack, Enchantments.MENDING) && stack.isDamaged();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult exp;
        ItemStack hotbarStack;
        int burst;
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        long timeoutMs = 500L;
        this.throwTimestamps.removeIf(t -> currentTime - t > timeoutMs);
        if (!this.startBind.get().isPressed()) {
            this.keyUnpressed = true;
        }
        if (this.startBind.get().isPressed() && this.keyUnpressed && !(this.mc.currentScreen instanceof ChatScreen)) {
            this.active = !this.active;
            this.keyUnpressed = false;
            if (this.active) {
                this.ticks = 100;
                this.waitingForLanding = false;
                this.previousRawNeeded = this.getBottlesNeeded();
                if (this.chatFeedback.get().booleanValue()) {
                    this.sendChatFeedback("started");
                }
            } else if (this.chatFeedback.get().booleanValue()) {
                this.sendChatFeedback("stopped");
            }
        }
        if (!this.active) {
            return;
        }
        int currentRawNeeded = this.getBottlesNeeded();
        if (currentRawNeeded < this.previousRawNeeded && !this.throwTimestamps.isEmpty()) {
            long oldestTime = this.throwTimestamps.get(0);
            this.throwTimestamps.removeIf(t -> Math.abs(t - oldestTime) < 100L);
        }
        this.previousRawNeeded = currentRawNeeded;
        int inAir = this.throwTimestamps.size();
        int smartNeeded = currentRawNeeded - inAir;
        if (currentRawNeeded <= 0) {
            this.active = false;
            this.waitingForLanding = false;
            if (this.chatFeedback.get().booleanValue()) {
                this.sendChatFeedback("finished");
            }
            return;
        }
        if (smartNeeded <= 0) {
            if (!this.waitingForLanding && this.debugFeedback.get().booleanValue()) {
                this.info("Landing Phase... (Raw Need: " + currentRawNeeded + " | In Air: " + inAir + ")", new Object[0]);
            }
            this.waitingForLanding = true;
            return;
        }
        this.waitingForLanding = false;
        int limit = this.getLimit();
        int factor = limit / (burst = 9);
        if (factor < 1) {
            factor = 1;
        }
        int delayTicks = 6 / factor;
        int finalThrowCount = Math.min(smartNeeded, burst);
        if (this.replenish.get().booleanValue() && ((hotbarStack = this.mc.player.getInventory().getStack(this.slot.get() - 1)).getCount() < finalThrowCount || hotbarStack.getItem() != Items.EXPERIENCE_BOTTLE) && (exp = InvUtils.find(Items.EXPERIENCE_BOTTLE)).found() && !exp.isHotbar() && !exp.isOffhand()) {
            InvUtils.move().from(exp.slot()).toHotbar(this.slot.get() - 1);
        }
        if (this.ticks < delayTicks) {
            ++this.ticks;
            return;
        }
        this.ticks = 0;
        if (finalThrowCount > 0 && MeteorClient.BLOCK.beginUseXp(Items.EXPERIENCE_BOTTLE, this.mc.player.getYaw(), 90.0f, this.mc.player.isUsingItem())) {
            int sentCount = 0;
            for (int i = 0; i < finalThrowCount; ++i) {
                if (!MeteorClient.BLOCK.useItem(Hand.MAIN_HAND, this.mc.player.getYaw(), 90.0f)) continue;
                this.throwTimestamps.add(System.currentTimeMillis());
                ++sentCount;
            }
            MeteorClient.BLOCK.endUse();
            if (this.debugFeedback.get().booleanValue()) {
                this.info("Raw: " + currentRawNeeded + " | Air: " + inAir + " | Smart: " + smartNeeded + " | Threw: " + sentCount, new Object[0]);
            }
        }
    }

    private void sendChatFeedback(String state) {
        MutableText body;
        if (this.mc.player == null) {
            return;
        }
        MutableText prefix = Text.literal((String)"[AutoEXP] ").formatted(Formatting.GOLD);
        switch (state) {
            case "started": {
                body = Text.literal((String)"Repairing...").formatted(Formatting.GREEN);
                break;
            }
            case "stopped": {
                body = Text.literal((String)"Stopped manually.").formatted(Formatting.RED);
                break;
            }
            case "finished": {
                body = Text.literal((String)"Repair finished.").formatted(Formatting.AQUA);
                break;
            }
            default: {
                return;
            }
        }
        this.mc.player.sendMessage((Text)prefix.append((Text)body), true);
    }
}

