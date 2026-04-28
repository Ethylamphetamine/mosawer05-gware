/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.registry.tag.FluidTags
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.managers.PacketPriority;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FakeItem;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SilentMine
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Mode> mode;
    private final Setting<PauseMode> pauseMode;
    private final Setting<Integer> breakDelay;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Double> range;
    public final Setting<Boolean> antiRubberband;
    private final Setting<Boolean> doubleMine;
    public final Setting<Boolean> preSwitchSinglebreak;
    private final Setting<Boolean> syncRebreak;
    private final Setting<Integer> singleBreakFailTicks;
    private final Setting<Integer> rebreakFailTicks;
    private final Setting<Integer> rebreakSpeed;
    public final Setting<Boolean> rebreakSetBlockBroken;
    private final Setting<Boolean> render;
    private final Setting<Boolean> renderBlock;
    private final Setting<RenderMode> renderMode;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Double> pulseFrequency;
    private final Setting<Double> pulseAmplitude;
    private final Setting<SettingColor> pulseSideColor;
    private final Setting<SettingColor> pulseLineColor;
    private final Setting<Boolean> debugRenderPrimary;
    private final Setting<Boolean> hideSwap;
    private SilentMineBlock rebreakBlock;
    private SilentMineBlock delayedDestroyBlock;
    private BlockPos lastDelayedDestroyBlockPos;
    private double currentGameTickCalculated;
    private boolean needDelayedDestroySwapBack;
    private boolean needRebreakSwapBack;
    private long lastDelayedDestroyFinishTick;
    private boolean lastMineWasFast;
    private long rebreakWindowStartMs;
    private int rebreakPacketsInWindow;
    private int lastRebreakPPS;
    private long lastClickTime;
    private BlockPos lastClickPos;
    private long lastFinishTick;
    private int normalFinishCount;
    public boolean stopRebreakPromotion;
    public boolean fromAutomine;
    private final Map<BlockPos, Integer> ignoredBlocks;
    private ItemStack renderStack;

    public void addIgnoredBlock(BlockPos pos, int ticks) {
        if (pos != null && ticks > 0) {
            this.ignoredBlocks.put(pos, ticks);
        }
    }

    public ItemStack getRenderStack() {
        return this.renderStack;
    }

    public SilentMine() {
        super(Categories.Player, "silent-mine", "Allows you to mine blocks without holding a pickaxe");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode to use.")).defaultValue(Mode.Old2b)).build());
        this.pauseMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("pause-on")).description("What screens should pause the mining process.")).defaultValue(PauseMode.Containers)).build());
        this.breakDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-delay")).description("Ticks to wait after breaking a block before starting the next one (New2b).")).defaultValue(1)).min(0).sliderMax(20).visible(() -> this.mode.get() == Mode.New2b)).build());
        this.pauseOnEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-eat")).description("Pauses all mining operations while eating/using items.")).defaultValue(true)).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Range to activate use at")).defaultValue(5.14).min(0.0).sliderMax(7.0).build());
        this.antiRubberband = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("strict-anti-rubberband")).description("Attempts to prevent you from rubberbanding extra hard. May result in kicks.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Old2b)).build());
        this.doubleMine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("double-mine")).description("Mines the block behind the targeted block if you double click.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Old2b)).build());
        this.preSwitchSinglebreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pre-switch-single-break")).description("Pre-switches to your pickaxe when the singlebreak block is almost done, for more responsive breaking.")).defaultValue(true)).build());
        this.syncRebreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sync-rebreak")).description("Waits for rebreak to finish every 2nd mine to maintain sync (Old2b).")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Old2b)).build());
        this.singleBreakFailTicks = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("single-break-fail-ticks")).description("Number of ticks to wait before retrying a singlebreak in case of fail.")).defaultValue(20)).min(5).sliderMax(50).build());
        this.rebreakFailTicks = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rebreak-fail-ticks")).description("Number of ticks to wait before retrying a rebreak if it remains solid.")).defaultValue(60)).min(5).sliderMax(100).visible(() -> this.mode.get() == Mode.Old2b)).build());
        this.rebreakSpeed = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rebreak-speed")).description("Rebreak attempts per second.")).defaultValue(30)).min(1).sliderRange(1, 30).visible(() -> this.mode.get() == Mode.Old2b)).build());
        this.rebreakSetBlockBroken = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("set-rebreak-block-broken")).description("Breaks the rebreak client side instantly.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Old2b)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("do-render")).description("Renders the blocks in queue to be broken.")).defaultValue(true)).build());
        this.renderBlock = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-block")).description("Whether to render the block being broken.")).defaultValue(true)).build());
        this.renderMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("render-mode")).description("Render style for the breaking box.")).defaultValue(RenderMode.Simple)).visible(this.renderBlock::get)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.renderBlock::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the rendering.")).defaultValue(new SettingColor(255, 180, 255, 15)).visible(() -> this.renderBlock.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the rendering.")).defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> this.renderBlock.get() != false && this.shapeMode.get().lines())).build());
        this.pulseFrequency = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pulse-frequency")).description("Pulse frequency (Hz)")).defaultValue(2.0).min(0.1).sliderMax(8.0).visible(() -> this.renderBlock.get() != false && this.renderMode.get() == RenderMode.Pulse)).build());
        this.pulseAmplitude = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pulse-amplitude")).description("Pulse size amplitude (0-0.5)")).defaultValue(0.2).min(0.0).sliderMax(0.5).visible(() -> this.renderBlock.get() != false && this.renderMode.get() == RenderMode.Pulse)).build());
        this.pulseSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("pulse-side-color")).description("Side color during pulse mode")).defaultValue(new SettingColor(255, 160, 40, 40)).visible(() -> this.renderBlock.get() != false && this.renderMode.get() == RenderMode.Pulse)).build());
        this.pulseLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("pulse-line-color")).description("Line color during pulse mode")).defaultValue(new SettingColor(255, 160, 40, 80)).visible(() -> this.renderBlock.get() != false && this.renderMode.get() == RenderMode.Pulse)).build());
        this.debugRenderPrimary = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("debug-render-primary")).description("Render the primary block differently for debugging.")).defaultValue(false)).build());
        this.hideSwap = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hide-swap")).description("Visually holds your original item while delayed destroy swaps to a pickaxe.")).defaultValue(true)).build());
        this.rebreakBlock = null;
        this.delayedDestroyBlock = null;
        this.lastDelayedDestroyBlockPos = null;
        this.currentGameTickCalculated = 0.0;
        this.needDelayedDestroySwapBack = false;
        this.needRebreakSwapBack = false;
        this.lastDelayedDestroyFinishTick = -1L;
        this.lastMineWasFast = false;
        this.rebreakWindowStartMs = -1L;
        this.rebreakPacketsInWindow = 0;
        this.lastRebreakPPS = 0;
        this.lastClickTime = 0L;
        this.lastClickPos = null;
        this.lastFinishTick = 0L;
        this.normalFinishCount = 0;
        this.stopRebreakPromotion = false;
        this.fromAutomine = false;
        this.ignoredBlocks = new ConcurrentHashMap<BlockPos, Integer>();
        this.renderStack = ItemStack.EMPTY;
        this.currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
    }

    @Override
    public void onDeactivate() {
        this.delayedDestroyBlock = null;
        this.rebreakBlock = null;
        this.lastFinishTick = 0L;
        this.normalFinishCount = 0;
        this.lastDelayedDestroyFinishTick = -1L;
        this.lastMineWasFast = false;
        this.ignoredBlocks.clear();
        this.renderStack = ItemStack.EMPTY;
    }

    private boolean canMine() {
        if (this.mc.currentScreen == null) {
            return true;
        }
        return switch (this.pauseMode.get().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 2 -> true;
            case 1 -> !(this.mc.currentScreen instanceof GenericContainerScreen) && !(this.mc.currentScreen instanceof ShulkerBoxScreen);
        };
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        boolean delayedDestroyFinished;
        FindItemResult result;
        this.currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
        if (!this.ignoredBlocks.isEmpty()) {
            for (BlockPos pos : this.ignoredBlocks.keySet()) {
                int val = this.ignoredBlocks.get(pos);
                if (val <= 1) {
                    this.ignoredBlocks.remove(pos);
                    continue;
                }
                this.ignoredBlocks.put(pos, val - 1);
            }
        }
        long now = System.currentTimeMillis();
        if (this.rebreakWindowStartMs == -1L || now - this.rebreakWindowStartMs >= 1000L) {
            this.lastRebreakPPS = this.rebreakPacketsInWindow;
            this.rebreakWindowStartMs = now;
            this.rebreakPacketsInWindow = 0;
        }
        if (this.pauseOnEat.get().booleanValue() && this.mc.player != null && this.mc.player.isUsingItem() && this.mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }
        this.lastDelayedDestroyBlockPos = this.hasDelayedDestroy() ? this.delayedDestroyBlock.blockPos : null;
        if (this.hasDelayedDestroy()) {
            if (this.ignoredBlocks.containsKey(this.delayedDestroyBlock.blockPos)) {
                this.lastMineWasFast = this.delayedDestroyBlock.getBreakProgressSingleTick() >= 0.16666666666666666;
                this.delayedDestroyBlock.cancelBreaking();
                this.delayedDestroyBlock = null;
                if (!this.lastMineWasFast) {
                    this.renderStack = ItemStack.EMPTY;
                }
                ++this.normalFinishCount;
                this.lastFinishTick = this.mc.world.getTime();
            } else if (this.mc.world.getBlockState(this.delayedDestroyBlock.blockPos).isAir() || !BlockUtils.canBreak(this.delayedDestroyBlock.blockPos)) {
                this.lastMineWasFast = this.delayedDestroyBlock.getBreakProgressSingleTick() >= 0.16666666666666666;
                MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Post(this.delayedDestroyBlock.blockPos, false));
                this.delayedDestroyBlock = null;
                if (!this.lastMineWasFast) {
                    this.renderStack = ItemStack.EMPTY;
                }
                ++this.normalFinishCount;
                this.lastFinishTick = this.mc.world.getTime();
            }
        }
        if (this.rebreakBlock != null) {
            if (this.ignoredBlocks.containsKey(this.rebreakBlock.blockPos)) {
                this.rebreakBlock.cancelBreaking();
                this.rebreakBlock = null;
            } else if (this.mc.world.getBlockState(this.rebreakBlock.blockPos).isAir() || !BlockUtils.canBreak(this.rebreakBlock.blockPos)) {
                this.rebreakBlock.beenAir = true;
            }
        }
        if (this.hasRebreakBlock() && this.rebreakBlock.timesSendBreakPacket > 40 && !this.canRebreakRebreakBlock()) {
            this.rebreakBlock.cancelBreaking();
            this.rebreakBlock = null;
        }
        if (this.hasDelayedDestroy()) {
            if (this.delayedDestroyBlock.waitingForRetry) {
                this.delayedDestroyBlock.startBreaking(true);
                this.delayedDestroyBlock.waitingForRetry = false;
                return;
            }
            BlockState blockState = this.mc.world.getBlockState(this.delayedDestroyBlock.blockPos);
            result = InvUtils.findFastestTool(blockState);
            if (this.delayedDestroyBlock.isReady() && (!result.found() || this.mc.player.getInventory().selectedSlot == result.slot())) {
                boolean isPickaxeBlock;
                boolean bl = isPickaxeBlock = result.found() && this.mc.player.getInventory().getStack(result.slot()).getItem() instanceof PickaxeItem;
                if (isPickaxeBlock) {
                    ++this.delayedDestroyBlock.ticksHeldPickaxe;
                }
            }
            if (this.delayedDestroyBlock.ticksHeldPickaxe > this.singleBreakFailTicks.get()) {
                if (this.inBreakRange(this.delayedDestroyBlock.blockPos)) {
                    ++this.delayedDestroyBlock.retries;
                    this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.delayedDestroyBlock.blockPos, this.delayedDestroyBlock.direction));
                    PacketManager.INSTANCE.incrementInteract();
                    this.delayedDestroyBlock.ticksHeldPickaxe = 0;
                    this.delayedDestroyBlock.waitingForRetry = true;
                } else {
                    this.delayedDestroyBlock.cancelBreaking();
                    this.delayedDestroyBlock = null;
                    this.lastFinishTick = this.mc.world.getTime();
                }
            } else if (this.delayedDestroyBlock.isReady()) {
                boolean shouldWait = false;
                if (this.mode.get() == Mode.Old2b && this.syncRebreak.get().booleanValue() && this.normalFinishCount % 2 != 0 && this.rebreakBlock != null && !this.rebreakBlock.beenAir && !this.rebreakBlock.isReady() && this.rebreakBlock.getBreakProgress() >= 0.35) {
                    shouldWait = true;
                }
                if (this.delayedDestroyBlock.retries > 0) {
                    shouldWait = false;
                }
                if (shouldWait) {
                    return;
                }
                boolean alreadyHoldingBestTool = result.found() && this.mc.player.getInventory().selectedSlot == result.slot();
                this.lastDelayedDestroyFinishTick = -1L;
                if (this.canMine() && result.found() && !alreadyHoldingBestTool) {
                    if (this.hideSwap.get().booleanValue()) {
                        this.renderStack = this.getItemToRender();
                    }
                    if (MeteorClient.SWAP.beginSwap(result, false)) {
                        this.needDelayedDestroySwapBack = true;
                        PacketManager.INSTANCE.incrementGlobal();
                    }
                }
                if (!(this.delayedDestroyBlock.stopPacketSent || !alreadyHoldingBestTool && result.found())) {
                    this.delayedDestroyBlock.stopBreaking();
                }
            }
        }
        if (this.mode.get() == Mode.Old2b && this.rebreakBlock != null) {
            long fallbackNow;
            if (this.rebreakBlock.waitingForRetry) {
                this.rebreakBlock.startBreaking(false);
                this.rebreakBlock.waitingForRetry = false;
            }
            BlockState blockState = this.mc.world.getBlockState(this.rebreakBlock.blockPos);
            if (this.rebreakBlock.isReady() && !blockState.isAir()) {
                if (this.inBreakRange(this.rebreakBlock.blockPos)) {
                    result = blockState.isAir() ? InvUtils.findInHotbar(stack -> stack.getItem() instanceof PickaxeItem) : InvUtils.findFastestTool(blockState);
                    MeteorClient.EVENT_BUS.post(new SilentMineFinishedEvent.Pre(this.rebreakBlock.blockPos, true));
                    if (this.canMine()) {
                        int remaining;
                        if (result.found() && this.mc.player.getInventory().selectedSlot != result.slot() && MeteorClient.SWAP.beginSwap(result, true)) {
                            this.needRebreakSwapBack = true;
                            PacketManager.INSTANCE.incrementGlobal();
                        }
                        int targetSpeed = this.rebreakSpeed.get();
                        if (!this.rebreakBlock.beenAir) {
                            targetSpeed = 1;
                        }
                        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.LOW)) {
                            targetSpeed = Math.min(targetSpeed, 7);
                        }
                        if ((remaining = targetSpeed - this.rebreakPacketsInWindow) < 0) {
                            remaining = 0;
                        }
                        for (int i = 0; i < remaining; ++i) {
                            if (this.ignoredBlocks.containsKey(this.rebreakBlock.blockPos)) {
                                this.rebreakBlock.cancelBreaking();
                                this.rebreakBlock = null;
                                break;
                            }
                            if (!this.mc.world.getBlockState(this.rebreakBlock.blockPos).isAir()) {
                                this.rebreakBlock.tryBreak();
                                ++this.rebreakPacketsInWindow;
                            }
                            if (this.canRebreakRebreakBlock()) break;
                        }
                        if (this.needRebreakSwapBack) {
                            MeteorClient.SWAP.endSwap(true);
                            this.needRebreakSwapBack = false;
                            PacketManager.INSTANCE.incrementGlobal();
                        }
                        if (this.rebreakSetBlockBroken.get().booleanValue() && this.canRebreakRebreakBlock()) {
                            this.mc.world.setBlockState(this.rebreakBlock.blockPos, Blocks.AIR.getDefaultState());
                        }
                    }
                } else {
                    this.rebreakBlock = null;
                }
            }
            if (this.rebreakBlock != null && this.rebreakBlock.beenAir && this.canMine() && this.inBreakRange(this.rebreakBlock.blockPos) && (fallbackNow = System.currentTimeMillis()) - this.rebreakBlock.lastFallbackBreakMs >= 500L) {
                this.rebreakBlock.lastFallbackBreakMs = fallbackNow;
                BlockState fallbackState = this.mc.world.getBlockState(this.rebreakBlock.blockPos).isAir() ? this.rebreakBlock.initialState : this.mc.world.getBlockState(this.rebreakBlock.blockPos);
                FindItemResult fallbackTool = InvUtils.findFastestTool(fallbackState);
                boolean fallbackSwapped = false;
                if (fallbackTool.found() && this.mc.player.getInventory().selectedSlot != fallbackTool.slot() && MeteorClient.SWAP.beginSwap(fallbackTool, true)) {
                    fallbackSwapped = true;
                    PacketManager.INSTANCE.incrementGlobal();
                }
                this.rebreakBlock.tryBreak();
                ++this.rebreakPacketsInWindow;
                if (fallbackSwapped) {
                    MeteorClient.SWAP.endSwap(true);
                    PacketManager.INSTANCE.incrementGlobal();
                }
                if (this.rebreakSetBlockBroken.get().booleanValue()) {
                    this.mc.world.setBlockState(this.rebreakBlock.blockPos, Blocks.AIR.getDefaultState());
                }
            }
        }
        if (this.rebreakBlock != null) {
            if (!this.mc.world.getBlockState(this.rebreakBlock.blockPos).isAir()) {
                boolean rebreakIsPickaxe;
                boolean inWater = this.mc.world.getFluidState(this.rebreakBlock.blockPos).isIn(FluidTags.WATER) || this.mc.player != null && this.mc.player.isSubmergedInWater();
                boolean isBedrock = this.mc.world.getBlockState(this.rebreakBlock.blockPos).isOf(Blocks.BEDROCK);
                FindItemResult rebreakTool = InvUtils.findFastestTool(this.mc.world.getBlockState(this.rebreakBlock.blockPos));
                boolean bl = rebreakIsPickaxe = rebreakTool.found() && this.mc.player.getInventory().getStack(rebreakTool.slot()).getItem() instanceof PickaxeItem;
                if (!inWater && !isBedrock && rebreakIsPickaxe) {
                    ++this.rebreakBlock.ticksSolid;
                }
                if (this.rebreakBlock.ticksSolid > this.rebreakFailTicks.get()) {
                    if (this.inBreakRange(this.rebreakBlock.blockPos)) {
                        this.rebreakBlock.beenAir = false;
                        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.rebreakBlock.blockPos, this.rebreakBlock.direction));
                        PacketManager.INSTANCE.incrementInteract();
                        this.rebreakBlock.ticksSolid = 0;
                        this.rebreakBlock.waitingForRetry = true;
                    } else {
                        this.rebreakBlock = null;
                    }
                }
            } else {
                this.rebreakBlock.ticksSolid = 0;
            }
        }
        boolean bl = delayedDestroyFinished = !this.hasDelayedDestroy() || !this.delayedDestroyBlock.isReady();
        if (this.needDelayedDestroySwapBack && delayedDestroyFinished) {
            if (this.lastMineWasFast) {
                if (this.lastDelayedDestroyFinishTick == -1L) {
                    this.lastDelayedDestroyFinishTick = this.mc.world.getTime();
                }
                if (this.mc.world.getTime() - this.lastDelayedDestroyFinishTick >= 6L) {
                    MeteorClient.SWAP.endSwap(false);
                    this.renderStack = ItemStack.EMPTY;
                    this.needDelayedDestroySwapBack = false;
                    this.lastDelayedDestroyFinishTick = -1L;
                    this.lastMineWasFast = false;
                    PacketManager.INSTANCE.incrementGlobal();
                }
            } else {
                MeteorClient.SWAP.endSwap(false);
                this.renderStack = ItemStack.EMPTY;
                this.needDelayedDestroySwapBack = false;
                this.lastDelayedDestroyFinishTick = -1L;
                this.lastMineWasFast = false;
                PacketManager.INSTANCE.incrementGlobal();
            }
        }
    }

    public void silentBreakBlock(BlockPos pos, double priority) {
        if (this.pauseOnEat.get().booleanValue() && this.mc.player != null && this.mc.player.isUsingItem()) {
            return;
        }
        this.silentBreakBlock(pos, Direction.UP, priority);
    }

    public void silentBreakBlock(BlockPos blockPos, Direction direction, double priority) {
        if (!this.isActive()) {
            return;
        }
        if (this.pauseOnEat.get().booleanValue() && this.mc.player != null && this.mc.player.isUsingItem()) {
            return;
        }
        if (blockPos == null || this.alreadyBreaking(blockPos)) {
            return;
        }
        if (!BlockUtils.canBreak(blockPos, this.mc.world.getBlockState(blockPos))) {
            return;
        }
        if (!this.inBreakRange(blockPos)) {
            return;
        }
        if (this.ignoredBlocks.containsKey(blockPos)) {
            return;
        }
        if (this.hasDelayedDestroy() && this.delayedDestroyBlock.retries >= 1) {
            return;
        }
        if (this.mode.get() == Mode.New2b) {
            if (this.delayedDestroyBlock != null) {
                return;
            }
            if (this.mc.world != null && this.mc.world.getTime() - this.lastFinishTick < (long)this.breakDelay.get().intValue()) {
                return;
            }
            this.currentGameTickCalculated -= 0.1;
            this.delayedDestroyBlock = new SilentMineBlock(blockPos, direction, priority, false);
            this.delayedDestroyBlock.startBreaking(true);
            return;
        }
        if (!(this.hasDelayedDestroy() || this.stopRebreakPromotion && this.fromAutomine)) {
            if (!(this.rebreakBlock == null || this.rebreakBlock.beenAir || this.alreadyBreaking(blockPos) || this.stopRebreakPromotion)) {
                this.delayedDestroyBlock = new SilentMineBlock(this.rebreakBlock, false);
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.delayedDestroyBlock.blockPos, this.delayedDestroyBlock.direction, this.getSeq()));
                PacketManager.INSTANCE.incrementInteract();
                this.rebreakBlock = new SilentMineBlock(blockPos, direction, priority, true);
                this.rebreakBlock.startBreaking(false);
                return;
            }
            if (this.stopRebreakPromotion && this.rebreakBlock != null && !this.rebreakBlock.beenAir) {
                return;
            }
            this.currentGameTickCalculated -= 0.1;
            this.delayedDestroyBlock = new SilentMineBlock(blockPos, direction, priority, false);
            this.delayedDestroyBlock.startBreaking(true);
            return;
        }
        if (this.alreadyBreaking(blockPos)) {
            return;
        }
        if (this.rebreakBlock == null || this.canRebreakRebreakBlock() && (!this.stopRebreakPromotion || !this.fromAutomine)) {
            this.rebreakBlock = new SilentMineBlock(blockPos, direction, priority, true);
            this.rebreakBlock.startBreaking(false);
            return;
        }
        if (!(!(priority > this.rebreakBlock.priority) || this.stopRebreakPromotion && this.fromAutomine)) {
            this.rebreakBlock = new SilentMineBlock(blockPos, direction, priority, true);
            this.rebreakBlock.startBreaking(false);
        }
    }

    @EventHandler
    public void onStartBreakingBlock(StartBreakingBlockEvent event) {
        long diff;
        if (this.pauseOnEat.get().booleanValue() && this.mc.player != null && this.mc.player.isUsingItem() && this.mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }
        event.cancel();
        if (this.mode.get() == Mode.Old2b && !this.alreadyBreaking(event.blockPos) && this.rebreakBlock != null && !this.rebreakBlock.beenAir) {
            if (!this.hasDelayedDestroy()) {
                this.delayedDestroyBlock = new SilentMineBlock(this.rebreakBlock, false);
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.delayedDestroyBlock.blockPos, this.delayedDestroyBlock.direction, this.getSeq()));
                PacketManager.INSTANCE.incrementInteract();
            } else {
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.rebreakBlock.blockPos, this.rebreakBlock.direction));
                PacketManager.INSTANCE.incrementInteract();
            }
            this.rebreakBlock = null;
        }
        long time = System.currentTimeMillis();
        boolean isDoubleClick = false;
        if (this.mode.get() == Mode.Old2b && this.doubleMine.get().booleanValue() && this.lastClickPos != null && this.lastClickPos.equals((Object)event.blockPos) && (diff = time - this.lastClickTime) > 70L && diff < 500L) {
            isDoubleClick = true;
        }
        this.silentBreakBlock(event.blockPos, event.direction, 100.0);
        if (isDoubleClick) {
            BlockPos behind = event.blockPos.offset(event.direction.getOpposite());
            this.silentBreakBlock(behind, event.direction, 100.0);
        }
        this.lastClickPos = event.blockPos;
        this.lastClickTime = time;
    }

    public boolean canSwapBack() {
        boolean result = this.needDelayedDestroySwapBack;
        if (this.hasDelayedDestroy() && this.delayedDestroyBlock.isReady()) {
            result = false;
        }
        return result;
    }

    public boolean hasDelayedDestroy() {
        return this.delayedDestroyBlock != null;
    }

    public boolean hasRebreakBlock() {
        return this.rebreakBlock != null && !this.rebreakBlock.beenAir;
    }

    public BlockPos getDelayedDestroyBlockPos() {
        if (this.delayedDestroyBlock == null) {
            return null;
        }
        return this.delayedDestroyBlock.blockPos;
    }

    public BlockPos getLastDelayedDestroyBlockPos() {
        return this.lastDelayedDestroyBlockPos;
    }

    public double getDelayedDestroyProgress() {
        if (this.delayedDestroyBlock == null) {
            return 0.0;
        }
        return this.delayedDestroyBlock.getBreakProgress();
    }

    public BlockPos getRebreakBlockPos() {
        if (this.rebreakBlock == null) {
            return null;
        }
        return this.rebreakBlock.blockPos;
    }

    public double getRebreakBlockProgress() {
        if (this.rebreakBlock == null) {
            return 0.0;
        }
        return this.rebreakBlock.getBreakProgress();
    }

    public boolean canRebreakRebreakBlock() {
        if (this.rebreakBlock == null) {
            return false;
        }
        return this.rebreakBlock.beenAir;
    }

    public int getRebreaksPerSecond() {
        long now = System.currentTimeMillis();
        if (this.rebreakWindowStartMs == -1L || now - this.rebreakWindowStartMs > 1000L) {
            return 0;
        }
        return this.rebreakPacketsInWindow;
    }

    public boolean inBreakRange(BlockPos blockPos) {
        Box box = new Box(blockPos);
        return !(box.squaredMagnitude(this.mc.player.getEyePos()) > this.range.get() * this.range.get());
    }

    public boolean alreadyBreaking(BlockPos blockPos) {
        return this.rebreakBlock != null && blockPos.equals((Object)this.rebreakBlock.blockPos) || this.delayedDestroyBlock != null && blockPos.equals((Object)this.delayedDestroyBlock.blockPos);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            double calculatedDrawGameTick = RenderUtils.getCurrentGameTickCalculated();
            if (this.rebreakBlock != null) {
                this.rebreakBlock.render(event, calculatedDrawGameTick, true);
            }
            if (this.delayedDestroyBlock != null) {
                this.delayedDestroyBlock.render(event, calculatedDrawGameTick, false);
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        PlayerActionC2SPacket packet;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof PlayerActionC2SPacket && (packet = (PlayerActionC2SPacket)packet2).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK && this.antiRubberband.get().booleanValue() && this.mode.get() == Mode.Old2b && packet.getPos().equals((Object)this.getRebreakBlockPos())) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos(), packet.getDirection()));
            PacketManager.INSTANCE.incrementInteract();
        }
    }

    private int getSeq() {
        return this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
    }

    private ItemStack getItemToRender() {
        ItemStack fake;
        ItemStack current = this.mc.player.getMainHandStack();
        FakeItem fakeItem = Modules.get().get(FakeItem.class);
        if (fakeItem.isActive() && !(fake = fakeItem.getRenderStack(Hand.MAIN_HAND, this.mc.player, current)).isEmpty()) {
            return fake.copy();
        }
        return current.copy();
    }

    public static enum Mode {
        Old2b,
        New2b;

    }

    public static enum PauseMode {
        AllScreens,
        Containers,
        None;

    }

    public static enum RenderMode {
        Simple,
        BoxIn,
        BoxOut,
        Pulse;

    }

    class SilentMineBlock {
        public BlockPos blockPos;
        public Direction direction;
        public boolean started = false;
        public boolean stopPacketSent = false;
        public int timesSendBreakPacket = 0;
        public int ticksHeldPickaxe = 0;
        public int ticksSolid = 0;
        public boolean beenAir = false;
        public int retries = 0;
        public boolean waitingForRetry = false;
        public long lastFallbackBreakMs = 0L;
        private double destroyProgressStart = 0.0;
        private double priority = 0.0;
        private boolean isRebreak;
        private final BlockState initialState;

        public SilentMineBlock(BlockPos blockPos, Direction direction, double priority, boolean isRebreak) {
            this.blockPos = blockPos;
            this.direction = direction;
            this.priority = priority;
            this.isRebreak = isRebreak;
            this.initialState = ((SilentMine)SilentMine.this).mc.world.getBlockState(blockPos);
            this.retries = 0;
        }

        public SilentMineBlock(SilentMineBlock original, boolean isRebreak) {
            this.blockPos = original.blockPos;
            this.direction = original.direction;
            this.priority = original.priority;
            this.isRebreak = isRebreak;
            this.initialState = original.initialState;
            this.destroyProgressStart = original.destroyProgressStart;
            this.started = original.started;
            this.retries = 0;
            this.waitingForRetry = false;
            if (!isRebreak) {
                this.stopPacketSent = false;
                this.timesSendBreakPacket = 0;
            } else {
                this.stopPacketSent = original.stopPacketSent;
                this.timesSendBreakPacket = original.timesSendBreakPacket;
            }
            this.ticksHeldPickaxe = 0;
            this.ticksSolid = original.ticksSolid;
            this.beenAir = original.beenAir;
        }

        public boolean isReady() {
            if (!BlockUtils.canBreak(this.blockPos) && !((SilentMine)SilentMine.this).mc.world.getBlockState(this.blockPos).isAir()) {
                return false;
            }
            double breakProgressSingleTick = this.getBreakProgressSingleTick();
            double threshold = this.isRebreak ? 0.7 : 1.0 - (SilentMine.this.preSwitchSinglebreak.get() != false ? breakProgressSingleTick / 2.0 : 0.0);
            return this.getBreakProgress() >= threshold || this.timesSendBreakPacket > 0;
        }

        public void startBreaking(boolean isDelayedDestroy) {
            FindItemResult result;
            this.ticksHeldPickaxe = 0;
            this.ticksSolid = 0;
            this.timesSendBreakPacket = 0;
            this.stopPacketSent = false;
            this.destroyProgressStart = SilentMine.this.currentGameTickCalculated;
            if (isDelayedDestroy && SilentMine.this.canRebreakRebreakBlock()) {
                SilentMine.this.rebreakBlock = null;
            }
            boolean swapped = false;
            if (SilentMine.this.canMine() && SilentMine.this.mode.get() == Mode.New2b && (result = InvUtils.findFastestTool(this.initialState)).found() && ((SilentMine)SilentMine.this).mc.player.getInventory().selectedSlot != result.slot()) {
                if (SilentMine.this.hideSwap.get().booleanValue() && isDelayedDestroy) {
                    SilentMine.this.renderStack = SilentMine.this.getItemToRender();
                }
                if (MeteorClient.SWAP.beginSwap(result, false)) {
                    swapped = true;
                    PacketManager.INSTANCE.incrementGlobal();
                }
            }
            SilentMine.this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, this.blockPos, this.direction, SilentMine.this.getSeq()));
            PacketManager.INSTANCE.incrementInteract();
            if (swapped) {
                MeteorClient.SWAP.endSwap(false);
                SilentMine.this.renderStack = ItemStack.EMPTY;
                PacketManager.INSTANCE.incrementGlobal();
            }
            if (SilentMine.this.mode.get() == Mode.Old2b) {
                SilentMine.this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction, SilentMine.this.getSeq()));
                PacketManager.INSTANCE.incrementInteract();
                this.stopPacketSent = true;
            }
            this.started = true;
        }

        public void stopBreaking() {
            if (this.stopPacketSent) {
                return;
            }
            SilentMine.this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction, SilentMine.this.getSeq()));
            PacketManager.INSTANCE.incrementInteract();
            this.stopPacketSent = true;
            ++this.timesSendBreakPacket;
        }

        public void tryBreak() {
            SilentMine.this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction, SilentMine.this.getSeq()));
            PacketManager.INSTANCE.incrementInteract();
            if (!SilentMine.this.antiRubberband.get().booleanValue()) {
                SilentMine.this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.blockPos, this.direction));
                PacketManager.INSTANCE.incrementInteract();
            }
            ++this.timesSendBreakPacket;
        }

        public void cancelBreaking() {
            SilentMine.this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, this.blockPos, this.direction));
            PacketManager.INSTANCE.incrementInteract();
        }

        public double getBreakProgress() {
            return this.getBreakProgress(SilentMine.this.currentGameTickCalculated);
        }

        public double getBreakProgress(double gameTick) {
            BlockState liveState = ((SilentMine)SilentMine.this).mc.world.getBlockState(this.blockPos);
            BlockState state = liveState.isAir() ? this.initialState : liveState;
            FindItemResult slot = this.findBestToolFor(state);
            Box boundingBox = ((SilentMine)SilentMine.this).mc.player.getBoundingBox();
            double playerFeetY = boundingBox.minY;
            Box groundBox = new Box(boundingBox.minX, playerFeetY - 0.2, boundingBox.minZ, boundingBox.maxX, playerFeetY, boundingBox.maxZ);
            boolean willBeOnGround = false;
            for (BlockPos pos : BlockUtils.iterate(groundBox)) {
                double blockTopY;
                double distanceToBlock;
                BlockState blockState = ((SilentMine)SilentMine.this).mc.world.getBlockState(pos);
                if (!blockState.isSolidBlock((BlockView)((SilentMine)SilentMine.this).mc.world, pos) || !((distanceToBlock = playerFeetY - (blockTopY = (double)pos.getY() + 1.0)) >= 0.0) || !(distanceToBlock < Math.abs(((SilentMine)SilentMine.this).mc.player.getVelocity().y * 2.0))) continue;
                willBeOnGround = true;
            }
            double breakingSpeed = BlockUtils.getBlockBreakingSpeed(slot.found() ? slot.slot() : ((SilentMine)SilentMine.this).mc.player.getInventory().selectedSlot, state, RotationManager.lastGround || willBeOnGround && !this.isRebreak);
            return Math.min(BlockUtils.getBreakDelta(breakingSpeed, state) * (gameTick - this.destroyProgressStart), 1.0);
        }

        public double getBreakProgressSingleTick() {
            return this.getBreakProgress(this.destroyProgressStart + 1.0);
        }

        private FindItemResult findBestToolFor(BlockState state) {
            ItemStack target;
            FindItemResult res;
            FindItemResult res2;
            if (state.isToolRequired() && (res2 = InvUtils.findFastestToolHotbar(state)).found()) {
                return res2;
            }
            int bestSlot = -1;
            float bestSpeed = 1.0f;
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = ((SilentMine)SilentMine.this).mc.player.getInventory().getStack(i);
                float spd = stack.getMiningSpeedMultiplier(state);
                if (!(spd > bestSpeed)) continue;
                bestSpeed = spd;
                bestSlot = i;
            }
            if (bestSlot != -1 && (res = InvUtils.findInHotbar(arg_0 -> SilentMineBlock.lambda$findBestToolFor$0(target = ((SilentMine)SilentMine.this).mc.player.getInventory().getStack(bestSlot), arg_0))).found()) {
                return res;
            }
            FindItemResult inv = InvUtils.findFastestTool(state);
            return inv;
        }

        public double getPriority() {
            return this.priority;
        }

        public void render(Render3DEvent event, double renderTick, boolean isPrimary) {
            VoxelShape shape = ((SilentMine)SilentMine.this).mc.world.getBlockState(this.blockPos).getOutlineShape((BlockView)((SilentMine)SilentMine.this).mc.world, this.blockPos);
            if (shape == null || shape.isEmpty()) {
                event.renderer.box(this.blockPos, (Color)SilentMine.this.sideColor.get(), (Color)SilentMine.this.lineColor.get(), SilentMine.this.shapeMode.get(), 0);
                return;
            }
            Box orig = shape.getBoundingBox();
            double shrinkFactor = 1.0 - Math.clamp(isPrimary ? this.getBreakProgress(renderTick) * 1.4285714285714286 : this.getBreakProgress(renderTick), 0.0, 1.0);
            BlockPos pos = this.blockPos;
            Box box = orig.shrink(orig.getLengthX() * shrinkFactor, orig.getLengthY() * shrinkFactor, orig.getLengthZ() * shrinkFactor);
            double xShrink = orig.getLengthX() * shrinkFactor / 2.0;
            double yShrink = orig.getLengthY() * shrinkFactor / 2.0;
            double zShrink = orig.getLengthZ() * shrinkFactor / 2.0;
            double x1 = (double)pos.getX() + box.minX + xShrink;
            double y1 = (double)pos.getY() + box.minY + yShrink;
            double z1 = (double)pos.getZ() + box.minZ + zShrink;
            double x2 = (double)pos.getX() + box.maxX + xShrink;
            double y2 = (double)pos.getY() + box.maxY + yShrink;
            double z2 = (double)pos.getZ() + box.maxZ + zShrink;
            Color color = SilentMine.this.sideColor.get();
            Color lines = SilentMine.this.lineColor.get();
            if (SilentMine.this.debugRenderPrimary.get().booleanValue() && isPrimary) {
                color = Color.ORANGE.a(40);
            }
            switch (SilentMine.this.renderMode.get().ordinal()) {
                case 0: 
                case 2: {
                    event.renderer.box(x1, y1, z1, x2, y2, z2, color, lines, SilentMine.this.shapeMode.get(), 0);
                    break;
                }
                case 1: {
                    double shrinkIn = Math.clamp(isPrimary ? this.getBreakProgress(renderTick) * 1.4285714285714286 : this.getBreakProgress(renderTick), 0.0, 1.0);
                    double xShrinkI = orig.getLengthX() * shrinkIn / 2.0;
                    double yShrinkI = orig.getLengthY() * shrinkIn / 2.0;
                    double zShrinkI = orig.getLengthZ() * shrinkIn / 2.0;
                    double x1i = (double)pos.getX() + orig.minX + xShrinkI;
                    double y1i = (double)pos.getY() + orig.minY + yShrinkI;
                    double z1i = (double)pos.getZ() + orig.minZ + zShrinkI;
                    double x2i = (double)pos.getX() + orig.maxX - xShrinkI;
                    double y2i = (double)pos.getY() + orig.maxY - yShrinkI;
                    double z2i = (double)pos.getZ() + orig.maxZ - zShrinkI;
                    event.renderer.box(x1i, y1i, z1i, x2i, y2i, z2i, color, lines, SilentMine.this.shapeMode.get(), 0);
                    break;
                }
                case 3: {
                    double elapsedTicks = renderTick - this.destroyProgressStart;
                    double seconds = Math.max(0.0, elapsedTicks / 20.0);
                    double phase = seconds * SilentMine.this.pulseFrequency.get() * Math.PI * 2.0;
                    double add = Math.sin(phase) * SilentMine.this.pulseAmplitude.get();
                    double shrinkAdj = Math.max(0.0, Math.min(1.0, shrinkFactor + add));
                    double xShrink2 = orig.getLengthX() * shrinkAdj / 2.0;
                    double yShrink2 = orig.getLengthY() * shrinkAdj / 2.0;
                    double zShrink2 = orig.getLengthZ() * shrinkAdj / 2.0;
                    double x1p = (double)pos.getX() + box.minX + xShrink2;
                    double y1p = (double)pos.getY() + box.minY + yShrink2;
                    double z1p = (double)pos.getZ() + box.minZ + zShrink2;
                    double x2p = (double)pos.getX() + box.maxX + xShrink2;
                    double y2p = (double)pos.getY() + box.maxY + yShrink2;
                    double z2p = (double)pos.getZ() + box.maxZ + zShrink2;
                    int sideA = (int)Math.round((double)SilentMine.this.pulseSideColor.get().a * (0.6 + 0.4 * Math.sin(phase)));
                    int lineA = (int)Math.round((double)SilentMine.this.pulseLineColor.get().a * (0.6 + 0.4 * Math.sin(phase)));
                    Color sidePulse = SilentMine.this.pulseSideColor.get().copy().a(Math.max(0, Math.min(255, sideA)));
                    Color linePulse = SilentMine.this.pulseLineColor.get().copy().a(Math.max(0, Math.min(255, lineA)));
                    event.renderer.box(x1p, y1p, z1p, x2p, y2p, z2p, sidePulse, linePulse, ShapeMode.Both, 0);
                }
            }
        }

        private static /* synthetic */ boolean lambda$findBestToolFor$0(ItemStack target, ItemStack s) {
            return s == target;
        }
    }
}

