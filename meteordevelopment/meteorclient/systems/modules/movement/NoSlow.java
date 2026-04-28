/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;

public class NoSlow
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> items;
    private final Setting<WebMode> web;
    private final Setting<Double> webTimer;
    private final Setting<Boolean> honeyBlock;
    private final Setting<Boolean> soulSand;
    private final Setting<Boolean> slimeBlock;
    private final Setting<Boolean> berryBush;
    private final Setting<Boolean> airStrict;
    private final Setting<Boolean> fluidDrag;
    private final Setting<Boolean> sneaking;
    private final Setting<Boolean> crawling;
    private final Setting<Boolean> hunger;
    private final Setting<Boolean> slowness;
    private final Setting<Boolean> climbing;
    private boolean resetTimer;

    public NoSlow() {
        super(Categories.Movement, "no-slow", "Allows you to move normally when using objects that will slow you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.items = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("items")).description("Whether or not using items will slow you.")).defaultValue(true)).build());
        this.web = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("web")).description("Whether or not cobwebs will not slow you down.")).defaultValue(WebMode.Vanilla)).build());
        this.webTimer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("web-timer")).description("The timer value for WebMode Timer.")).defaultValue(10.0).min(1.0).sliderMin(1.0).visible(() -> this.web.get() == WebMode.Timer)).build());
        this.honeyBlock = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("honey-block")).description("Whether or not honey blocks will not slow you down.")).defaultValue(true)).build());
        this.soulSand = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("soul-sand")).description("Whether or not soul sand will not slow you down.")).defaultValue(true)).build());
        this.slimeBlock = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("slime-block")).description("Whether or not slime blocks will not slow you down.")).defaultValue(true)).build());
        this.berryBush = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("berry-bush")).description("Whether or not berry bushes will not slow you down.")).defaultValue(true)).build());
        this.airStrict = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-strict")).description("Will attempt to bypass anti-cheats like 2b2t's. Only works while in air.")).defaultValue(false)).build());
        this.fluidDrag = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fluid-drag")).description("Whether or not fluid drag will not slow you down.")).defaultValue(false)).build());
        this.sneaking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sneaking")).description("Whether or not sneaking will not slow you down.")).defaultValue(false)).build());
        this.crawling = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("crawling")).description("Whether or not crawling will not slow you down.")).defaultValue(false)).build());
        this.hunger = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hunger")).description("Whether or not hunger will not slow you down.")).defaultValue(false)).build());
        this.slowness = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("slowness")).description("Whether or not slowness will not slow you down.")).defaultValue(false)).build());
        this.climbing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("climbing")).description("Whether or not climbing will slow you down.")).defaultValue(false)).build());
    }

    @Override
    public void onActivate() {
        this.resetTimer = false;
    }

    public boolean airStrict() {
        return this.isActive() && this.airStrict.get() != false && this.mc.player.isUsingItem();
    }

    public boolean items() {
        return this.isActive() && this.items.get() != false;
    }

    public boolean honeyBlock() {
        return this.isActive() && this.honeyBlock.get() != false;
    }

    public boolean soulSand() {
        return this.isActive() && this.soulSand.get() != false;
    }

    public boolean slimeBlock() {
        return this.isActive() && this.slimeBlock.get() != false;
    }

    public boolean cobweb() {
        return this.isActive() && this.web.get() == WebMode.Vanilla;
    }

    public boolean cobwebGrim() {
        return this.isActive() && this.web.get() == WebMode.Grim;
    }

    public boolean berryBush() {
        return this.isActive() && this.berryBush.get() != false;
    }

    public boolean fluidDrag() {
        return this.isActive() && this.fluidDrag.get() != false;
    }

    public boolean sneaking() {
        return this.isActive() && this.sneaking.get() != false;
    }

    public boolean crawling() {
        return this.isActive() && this.crawling.get() != false;
    }

    public boolean hunger() {
        return this.isActive() && this.hunger.get() != false;
    }

    public boolean slowness() {
        return this.isActive() && this.slowness.get() != false;
    }

    public boolean climbing() {
        return this.isActive() && this.climbing.get() != false;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.web.get() == WebMode.Timer) {
            if (this.mc.world.getBlockState(this.mc.player.getBlockPos()).getBlock() == Blocks.COBWEB && !this.mc.player.isOnGround()) {
                this.resetTimer = false;
                Modules.get().get(Timer.class).setOverride(this.webTimer.get());
            } else if (!this.resetTimer) {
                Modules.get().get(Timer.class).setOverride(1.0);
                this.resetTimer = true;
            }
        }
        if (this.web.get() == WebMode.Grim) {
            // empty if block
        }
    }

    public static enum WebMode {
        Vanilla,
        Timer,
        Grim,
        None;

    }
}

