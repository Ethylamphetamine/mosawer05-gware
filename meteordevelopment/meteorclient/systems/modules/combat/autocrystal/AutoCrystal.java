/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.PacketByteBuf
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$Handler
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractTypeHandler
 *  net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.PlayerInteractEntityC2SPacketInvoker;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.managers.PacketPriority;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoMine;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystalRenderer;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystalUtil;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AutoCrystal
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgFacePlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgRotate;
    private final SettingGroup sgSwing;
    private final SettingGroup sgRange;
    private final SettingGroup sgPrediction;
    private final AutoCrystalRenderer renderer;
    private FacePlaceMode currentFacePlaceMode;
    private boolean facePlaceKeyDown;
    private final Setting<Boolean> placeCrystals;
    private final Setting<Boolean> manualOverride;
    private final Setting<Boolean> pauseEatPlace;
    private final Setting<Boolean> breakCrystals;
    private final Setting<Boolean> pauseEatBreak;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<Boolean> setPlayerDead;
    private final Setting<Boolean> terrainIgnoreCalc;
    private final Setting<Double> placeSpeedLimit;
    private final Setting<Double> minPlace;
    private final Setting<Double> maxPlace;
    private final Setting<Boolean> basePlaceEnabled;
    private final Setting<Boolean> antiSurroundPlace;
    private final Setting<Double> silentMineProgressThreshold;
    private final Setting<Double> placeDelay;
    private final Setting<Boolean> ignoreItem;
    private final Setting<Boolean> grimPlace;
    private final Setting<GrimMode> grimMode;
    private final Setting<Boolean> facePlaceMissingArmor;
    private final Setting<Keybind> facePlaceKeybind;
    private final Setting<Boolean> slowPlace;
    private final Setting<Double> slowPlaceMinDamage;
    private final Setting<Double> slowPlaceMaxDamage;
    private final Setting<Double> slowPlaceSpeed;
    private final Setting<Double> breakSpeedLimit;
    private final Setting<PacketBreakMode> packetBreakMode;
    private final Setting<Boolean> terrainIgnoreBreak;
    private final Setting<Double> minBreak;
    private final Setting<Double> maxBreak;
    private final Setting<Double> breakDelay;
    private final Setting<Boolean> rotatePlace;
    private final Setting<Boolean> rotateBreak;
    private final Setting<SwingMode> breakSwingMode;
    private final Setting<SwingMode> placeSwingMode;
    private final Setting<Double> placeRange;
    private final Setting<Double> breakRange;
    private final Setting<Boolean> prediction;
    private final Setting<Integer> predictionTicks;
    private final Setting<Integer> elytraPredictionTicks;
    private final Setting<Integer> crystalSlot;
    public final List<Entity> forceBreakCrystals;
    private final Pool<PlacePosition> placePositionPool;
    private final List<PlacePosition> _placePositions;
    private final BlockPos.Mutable mutablePos;
    private final IntSet explodedCrystals;
    private final Map<Integer, Long> crystalBreakDelays;
    private final Map<BlockPos, Long> crystalPlaceDelays;
    public final List<Boolean> cachedValidSpots;
    private long lastPlaceTimeMS;
    private long lastBreakTimeMS;
    private Integer pendingPacketBreakId;
    private Vec3d pendingPacketBreakPos;
    private BlockPos pendingPacketBreakBasePos;
    private long pendingPacketBreakTimeMS;
    private BlockPos pendingBasePlacePos;
    private long pendingBasePlaceTimeMS;
    private static final long BASE_PLACE_COOLDOWN_MS = 500L;
    private double lastBasePlaceDamage;
    private boolean canBasePlace;
    private AutoMine autoMine;
    private Set<BlockPos> _calcIgnoreSet;
    private int grimRotationIndex;
    private final Map<UUID, Integer> boostingTicks;
    private static final int BOOST_DURATION_TICKS = 40;
    private static final double TERMINAL_VELOCITY = -3.92;
    private static final double DRAG_XZ_FLY = 0.99;
    private static final double DRAG_Y_FLY = 0.98;
    private static final double ALIGN_D = 1.5;
    private static final double ALIGN_E = 0.01;
    private static final double LOOK_PUSH = 0.1;
    private static final double ELYTRA_GRAVITY = -0.04;
    private long lastBasePlaceAttemptMS;
    private static final long BASE_PLACE_RETRY_MS = 500L;
    boolean doGrimSwap;
    private ItemStack renderStack;
    private int renderTimer;

    public AutoCrystal() {
        super(Categories.Combat, "auto-crystal", "Automatically places and attacks crystals.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgFacePlace = this.settings.createGroup("Face Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgRotate = this.settings.createGroup("Rotate");
        this.sgSwing = this.settings.createGroup("Swing");
        this.sgRange = this.settings.createGroup("Range");
        this.sgPrediction = this.settings.createGroup("Prediction");
        this.renderer = new AutoCrystalRenderer(this);
        this.currentFacePlaceMode = FacePlaceMode.Off;
        this.facePlaceKeyDown = false;
        this.placeCrystals = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place")).description("Places crystals.")).defaultValue(true)).build());
        this.manualOverride = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("manual-override")).description("Places crystals at your crosshair when holding Right Click with a crystal.")).defaultValue(true)).build());
        this.pauseEatPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat-place")).description("Pauses placing when eating")).defaultValue(true)).build());
        this.breakCrystals = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("break")).description("Breaks crystals.")).defaultValue(true)).build());
        this.pauseEatBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat-break")).description("Pauses placing when breaking")).defaultValue(false)).build());
        this.ignoreNakeds = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Ignore players with no items.")).defaultValue(true)).build());
        this.setPlayerDead = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("set-player-dead-instantly")).description("Tries to not blow up loot by instantly killing the player in the packet they die.")).defaultValue(true)).build());
        this.terrainIgnoreCalc = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("calc-ignore-terrain")).description("Treats explodable blocks (terrain) as air for Place/Base damage calculations.")).defaultValue(false)).build());
        this.placeSpeedLimit = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-speed-limit")).description("Maximum number of crystals to place every second.")).defaultValue(40.0).min(0.0).sliderRange(0.0, 40.0).build());
        this.minPlace = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-place")).description("Minimum enemy damage to place.")).defaultValue(8.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.maxPlace = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-place")).description("Max self damage to place.")).defaultValue(20.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.basePlaceEnabled = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("base-place")).description("Places obsidian before placing crystals if needed.")).defaultValue(true)).build());
        this.antiSurroundPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-surround")).description("Ignores auto-mine blocks from calculations to place outside of their surround.")).defaultValue(true)).build());
        this.silentMineProgressThreshold = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("silent-mine-progress-threshold")).description("Minimum SilentMine progress (percent) before treating the block as mined and ignoring it for placement. 0 -> ignore directly")).defaultValue(80.0).min(0.0).sliderRange(0.0, 100.0).visible(this.antiSurroundPlace::get)).build());
        this.placeDelay = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-delay")).description("The number of seconds to wait to retry placing a crystal at a position.")).defaultValue(0.05).min(0.0).sliderMax(0.6).build());
        this.ignoreItem = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-item")).description("Tries to ignore items when placing")).defaultValue(false)).build());
        this.grimPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-place")).description("Test")).defaultValue(false)).build());
        this.grimMode = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("grim-mode")).description("The sequence for Grim Place.")).defaultValue(GrimMode.OneToOne)).visible(this.grimPlace::get)).build());
        this.facePlaceMissingArmor = this.sgFacePlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("face-place-missing-armor")).description("Face places on missing armor")).defaultValue(true)).build());
        this.facePlaceKeybind = this.sgFacePlace.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("face-place-toggle")).description("Cycles: Fast Faceplace -> Off -> Slow Faceplace.")).defaultValue(Keybind.none())).build());
        this.slowPlace = this.sgFacePlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("slow-place")).description("Slowly places crystals at lower damages.")).defaultValue(true)).build());
        this.slowPlaceMinDamage = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slow-place-min-place")).description("Minimum damage to slow place.")).defaultValue(4.0).min(0.0).sliderRange(0.0, 20.0).visible(this.slowPlace::get)).build());
        this.slowPlaceMaxDamage = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slow-place-max-place")).description("Maximum damage to slow place.")).defaultValue(8.0).min(0.0).sliderRange(0.0, 20.0).visible(this.slowPlace::get)).build());
        this.slowPlaceSpeed = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slow-place-speed")).description("Speed at which to slow place.")).defaultValue(2.0).min(0.0).sliderRange(0.0, 20.0).visible(this.slowPlace::get)).build());
        this.breakSpeedLimit = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-speed-limit")).description("Maximum number of crystals to break every second.")).defaultValue(60.0).min(0.0).sliderRange(0.0, 60.0).build());
        this.packetBreakMode = this.sgBreak.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("packet-break-mode")).description("How to break crystals on packet arrival. EntityBased = break on EntityAddedEvent, IdBased = break on EntitySpawnS2CPacket by ID, Both = use both methods, None = disabled.")).defaultValue(PacketBreakMode.IdBased)).build());
        this.terrainIgnoreBreak = this.sgBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-terrain-blocks")).description("Treats explodable blocks like air during damage calculations for breaking.")).defaultValue(false)).build());
        this.minBreak = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-break")).description("Minimum enemy damage to break.")).defaultValue(3.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.maxBreak = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-break")).description("Max self damage to break.")).defaultValue(20.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.breakDelay = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-delay")).description("The number of seconds to wait to retry breaking a crystal.")).defaultValue(0.05).min(0.0).sliderMax(0.6).build());
        this.rotatePlace = this.sgRotate.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate-place")).description("Rotates server-side towards the crystals when placed.")).defaultValue(false)).build());
        this.rotateBreak = this.sgRotate.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate-break")).description("Rotates server-side towards the crystals when broken.")).defaultValue(true)).build());
        this.breakSwingMode = this.sgSwing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("break-swing-mode")).description("Mode for swinging your hand when breaking")).defaultValue(SwingMode.None)).build());
        this.placeSwingMode = this.sgSwing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("place-swing-mode")).description("Mode for swinging your hand when placing")).defaultValue(SwingMode.None)).build());
        this.placeRange = this.sgRange.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("Maximum distance to place crystals for")).defaultValue(4.0).build());
        this.breakRange = this.sgRange.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-range")).description("Maximum distance to break crystals for")).defaultValue(4.0).build());
        this.prediction = this.sgPrediction.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("prediction")).description("Predicts player movement for placement calculations.")).defaultValue(true)).build());
        this.predictionTicks = this.sgPrediction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("ground-ticks")).description("How many ticks to predict for walking/running players.")).defaultValue(2)).min(0).sliderMax(20).visible(this.prediction::get)).build());
        this.elytraPredictionTicks = this.sgPrediction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("elytra-ticks")).description("How many ticks to predict for players using Elytra.")).defaultValue(4)).min(0).sliderMax(30).visible(this.prediction::get)).build());
        this.crystalSlot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("crystal-slot")).description("The hotbar slot to move Crystals to if not found in hotbar.")).defaultValue(9)).range(1, 9).sliderMin(1).sliderMax(9).build());
        this.forceBreakCrystals = new ArrayList<Entity>();
        this.placePositionPool = new Pool<PlacePosition>(() -> new PlacePosition(this));
        this._placePositions = new ArrayList<PlacePosition>();
        this.mutablePos = new BlockPos.Mutable();
        this.explodedCrystals = new IntOpenHashSet();
        this.crystalBreakDelays = new HashMap<Integer, Long>();
        this.crystalPlaceDelays = new HashMap<BlockPos, Long>();
        this.cachedValidSpots = new ArrayList<Boolean>();
        this.lastPlaceTimeMS = 0L;
        this.lastBreakTimeMS = 0L;
        this.pendingPacketBreakId = null;
        this.pendingPacketBreakPos = null;
        this.pendingPacketBreakBasePos = null;
        this.pendingPacketBreakTimeMS = 0L;
        this.pendingBasePlacePos = null;
        this.pendingBasePlaceTimeMS = 0L;
        this.lastBasePlaceDamage = 0.0;
        this.canBasePlace = false;
        this._calcIgnoreSet = new HashSet<BlockPos>();
        this.grimRotationIndex = 0;
        this.boostingTicks = new HashMap<UUID, Integer>();
        this.lastBasePlaceAttemptMS = 0L;
        this.doGrimSwap = false;
        this.renderStack = ItemStack.EMPTY;
        this.renderTimer = 0;
    }

    public ItemStack getRenderStack() {
        return this.renderStack;
    }

    @Override
    public void onActivate() {
        if (this.autoMine == null) {
            this.autoMine = Modules.get().get(AutoMine.class);
        }
        this.explodedCrystals.clear();
        this.crystalBreakDelays.clear();
        this.crystalPlaceDelays.clear();
        this.boostingTicks.clear();
        this.pendingPacketBreakId = null;
        this.pendingPacketBreakPos = null;
        this.pendingPacketBreakBasePos = null;
        this.pendingPacketBreakTimeMS = 0L;
        this.pendingBasePlacePos = null;
        this.pendingBasePlaceTimeMS = 0L;
        this.lastBasePlaceDamage = 0.0;
        this.canBasePlace = false;
        this.facePlaceKeyDown = false;
        this.grimRotationIndex = 0;
        this.renderStack = ItemStack.EMPTY;
        this.renderTimer = 0;
        this.renderer.onActivate();
    }

    private boolean isManual() {
        if (!this.manualOverride.get().booleanValue()) {
            return false;
        }
        if (this.mc.currentScreen != null) {
            return false;
        }
        if (!this.mc.options.useKey.isPressed()) {
            return false;
        }
        ItemStack main = this.mc.player.getMainHandStack();
        ItemStack off = this.mc.player.getOffHandStack();
        return main.getItem() == Items.END_CRYSTAL || off.getItem() == Items.END_CRYSTAL;
    }

    private PlacePosition getManualPlacePos() {
        HitResult hit = this.mc.crosshairTarget;
        if (hit instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)hit;
            if (hit.getType() == HitResult.Type.BLOCK) {
                Box box;
                BlockState state;
                BlockPos obsPos = bhr.getBlockPos();
                BlockPos placePos = obsPos.up();
                if (!this.inPlaceRange(placePos)) {
                    return null;
                }
                if (this.mc.world.isAir(placePos) && ((state = this.mc.world.getBlockState(obsPos)).getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK) && !this.intersectsWithEntities(box = new Box((double)placePos.getX(), (double)placePos.getY(), (double)placePos.getZ(), (double)(placePos.getX() + 1), (double)(placePos.getY() + 2), (double)(placePos.getZ() + 1)))) {
                    PlacePosition p = this.placePositionPool.get();
                    p.blockPos = placePos;
                    p.isSlowPlace = false;
                    this._placePositions.add(p);
                    return p;
                }
            }
        }
        return null;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        Vec3d soundPos;
        PlaySoundS2CPacket packet;
        if (this.mc.world == null || this.mc.world.getPlayers().isEmpty()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof PlaySoundS2CPacket && (packet = (PlaySoundS2CPacket)packet2).getSound().comp_349() == SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH) {
            soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (!(player.getPos().squaredDistanceTo(soundPos) < 9.0)) continue;
                this.boostingTicks.put(player.getUuid(), 40);
                break;
            }
        }
        if ((soundPos = event.packet) instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket spawnPacket = (EntitySpawnS2CPacket)soundPos;
            if (!this.breakCrystals.get().booleanValue()) {
                return;
            }
            PacketBreakMode mode = this.packetBreakMode.get();
            if (mode != PacketBreakMode.IdBased && mode != PacketBreakMode.Both) {
                return;
            }
            if (spawnPacket.getEntityType() != EntityType.END_CRYSTAL) {
                return;
            }
            if (this.mc.player == null) {
                return;
            }
            Vec3d crystalPos = new Vec3d(spawnPacket.getX(), spawnPacket.getY(), spawnPacket.getZ());
            if (!this.inBreakRange(crystalPos)) {
                return;
            }
            if (!this.shouldBreakCrystalAtPos(crystalPos)) {
                return;
            }
            int entityId = spawnPacket.getEntityId();
            this.doBreakCrystalById(entityId, crystalPos);
        }
    }

    private boolean shouldBreakCrystalAtPos(Vec3d crystalPos) {
        double selfDamage;
        SilentMine silentMine;
        if (this.isManual()) {
            return true;
        }
        HashSet<BlockPos> ignore = new HashSet<BlockPos>();
        BlockPos crystalBlockPos = BlockPos.ofFloored((Position)crystalPos);
        if (this.terrainIgnoreBreak.get().booleanValue()) {
            this.addExplodableBlocks(crystalBlockPos, ignore);
        }
        if (this.antiSurroundPlace.get().booleanValue() && (silentMine = Modules.get().get(SilentMine.class)) != null && silentMine.isActive()) {
            BlockPos rebreak;
            double required = this.silentMineProgressThreshold.get() / 100.0;
            BlockPos delayed = silentMine.getDelayedDestroyBlockPos();
            if (delayed != null && silentMine.getDelayedDestroyProgress() >= required) {
                ignore.add(delayed);
            }
            if ((rebreak = silentMine.getRebreakBlockPos()) != null && silentMine.canRebreakRebreakBlock()) {
                ignore.add(rebreak);
            }
        }
        if ((selfDamage = DamageUtils.newCrystalDamage((PlayerEntity)this.mc.player, this.mc.player.getBoundingBox(), crystalPos, ignore)) > this.maxBreak.get()) {
            return false;
        }
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            double targetDamage;
            if (player == this.mc.player || player.isDead() || Friends.get().isFriend(player) || player.squaredDistanceTo(this.mc.player.getEyePos()) > 196.0 || !((targetDamage = DamageUtils.newCrystalDamage(player, player.getBoundingBox(), crystalPos, ignore)) >= this.minBreak.get())) continue;
            return true;
        }
        return false;
    }

    private boolean doBreakCrystalById(int entityId, Vec3d crystalPos) {
        if (this.mc.player == null) {
            return false;
        }
        BlockPos crystalBase = BlockPos.ofFloored((Position)crystalPos).down();
        if (!this.breakSpeedCheck()) {
            return false;
        }
        boolean sneaking = this.mc.player.isSneaking();
        PlayerInteractEntityC2SPacket.InteractTypeHandler attackHandler = new PlayerInteractEntityC2SPacket.InteractTypeHandler(this){

            public PlayerInteractEntityC2SPacket.InteractType getType() {
                return PlayerInteractEntityC2SPacket.InteractType.ATTACK;
            }

            public void handle(PlayerInteractEntityC2SPacket.Handler handler) {
                handler.attack();
            }

            public void write(PacketByteBuf buf) {
            }
        };
        PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacketInvoker.invokeInit(entityId, sneaking, attackHandler);
        this.mc.getNetworkHandler().sendPacket((Packet)attackPacket);
        PacketManager.INSTANCE.incrementInteract();
        if (this.breakSwingMode.get() == SwingMode.Client) {
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (this.breakSwingMode.get() == SwingMode.Packet) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            PacketManager.INSTANCE.incrementInteract();
        }
        this.explodedCrystals.add(entityId);
        this.lastBreakTimeMS = System.currentTimeMillis();
        this.crystalBreakDelays.put(entityId, System.currentTimeMillis());
        this.pendingPacketBreakId = entityId;
        this.pendingPacketBreakPos = crystalPos;
        this.pendingPacketBreakBasePos = crystalBase;
        this.pendingPacketBreakTimeMS = System.currentTimeMillis();
        return true;
    }

    private Vec3d getPredictedPos(PlayerEntity player) {
        int ticks;
        if (!this.prediction.get().booleanValue()) {
            return player.getPos();
        }
        if (this.boostingTicks.containsKey(player.getUuid())) {
            ticks = this.boostingTicks.get(player.getUuid());
            if (ticks > 0) {
                this.boostingTicks.put(player.getUuid(), ticks - 1);
            } else {
                this.boostingTicks.remove(player.getUuid());
            }
        }
        if (player.isFallFlying()) {
            ticks = this.elytraPredictionTicks.get();
            if (this.boostingTicks.containsKey(player.getUuid())) {
                return this.simulateBoostedElytraFuturePos(player, ticks);
            }
            return this.simulateElytraFuturePos(player, ticks);
        }
        return this.predictPositionOnGround(player);
    }

    private Vec3d predictPositionOnGround(PlayerEntity player) {
        if (player == null) {
            return player.getPos();
        }
        Vec3d pos = player.getPos();
        double vX = player.getX() - player.prevX;
        double vY = player.getY() - player.prevY;
        double vZ = player.getZ() - player.prevZ;
        Vec3d velocity = new Vec3d(vX, vY, vZ);
        boolean onGround = player.isOnGround();
        if (onGround && vY <= 0.05) {
            velocity = new Vec3d(velocity.x, 0.0, velocity.z);
        }
        int ticks = this.predictionTicks.get();
        for (int i = 0; i < ticks; ++i) {
            Vec3d rayEnd;
            if (!onGround && velocity.y != 0.0) {
                velocity = velocity.add(0.0, -0.08, 0.0);
                velocity = velocity.multiply(0.98, 0.98, 0.98);
            }
            Vec3d nextPos = pos.add(velocity);
            Vec3d rayStart = pos.add(0.0, 0.5, 0.0);
            BlockHitResult result = this.mc.world.raycast(new RaycastContext(rayStart, rayEnd = nextPos.add(0.0, 0.5, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)player));
            if (result.getType() == HitResult.Type.BLOCK) {
                if (velocity.y != 0.0 && result.getSide() == Direction.UP) {
                    return new Vec3d(nextPos.x, (double)(result.getBlockPos().getY() + 1), nextPos.z);
                }
                return result.getPos();
            }
            if (velocity.y != 0.0 && this.mc.world.getBlockState(BlockPos.ofFloored((Position)nextPos)).isSolid()) {
                return new Vec3d(nextPos.x, Math.floor(nextPos.y) + 1.0, nextPos.z);
            }
            pos = nextPos;
            if (velocity.lengthSquared() < 0.001) break;
        }
        return pos;
    }

    private Vec3d simulateElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        float pitchRad = (float)Math.toRadians(player.getPitch());
        Vec3d look = player.getRotationVector();
        double cos = Math.cos(pitchRad);
        for (int i = 0; i < ticks; ++i) {
            Vec3d vNext;
            pos = pos.add(vel);
            double horizSpeed = Math.hypot(vel.x, vel.z);
            double len = vel.length();
            double liftFactor = cos * cos * Math.min(1.0, len / 0.4);
            double vy = vel.y + (-0.04 + liftFactor * 0.06);
            if (vel.y < 0.0 && horizSpeed > 0.0) {
                vy += -0.1 * vel.y * liftFactor;
            }
            Vec3d vAfterLift = new Vec3d(vel.x, vy, vel.z);
            Vec3d align = new Vec3d(look.x * 0.1 + (look.x * 1.5 - vAfterLift.x) * 0.01, look.y * 0.1 + (look.y * 1.5 - vAfterLift.y) * 0.01, look.z * 0.1 + (look.z * 1.5 - vAfterLift.z) * 0.01);
            Vec3d vAligned = vAfterLift.add(align);
            vel = vNext = new Vec3d(vAligned.x * 0.99, vAligned.y * 0.98, vAligned.z * 0.99);
        }
        return pos;
    }

    private Vec3d simulateBoostedElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        double BOOST_DRAG = 0.991;
        for (int i = 0; i < ticks; ++i) {
            pos = pos.add(vel);
            double vy = vel.y;
            if (vy < -3.92) {
                vy = -3.92;
            }
            vel = new Vec3d(vel.x * 0.991, vel.y, vel.z * 0.991);
        }
        return pos;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult invCrystals;
        this.canBasePlace = true;
        this.crystalBreakDelays.values().removeIf(time -> System.currentTimeMillis() - time > 1000L);
        if (this.renderTimer > 0) {
            --this.renderTimer;
        } else {
            this.renderStack = ItemStack.EMPTY;
        }
        if (!this.isActive() || this.mc.player == null || this.mc.world == null) {
            return;
        }
        FindItemResult hotbarCrystals = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!hotbarCrystals.found() && (invCrystals = InvUtils.find(Items.END_CRYSTAL)).found()) {
            InvUtils.move().from(invCrystals.slot()).toHotbar(this.crystalSlot.get() - 1);
        }
        if (this.mc.currentScreen == null && this.facePlaceKeybind.get().isPressed()) {
            if (!this.facePlaceKeyDown) {
                switch (this.currentFacePlaceMode.ordinal()) {
                    case 1: {
                        this.currentFacePlaceMode = FacePlaceMode.Fast;
                        this.sendChatFeedback("started");
                        break;
                    }
                    case 0: {
                        this.currentFacePlaceMode = FacePlaceMode.Off;
                        this.sendChatFeedback("stopped");
                    }
                }
                this.facePlaceKeyDown = true;
            }
        } else {
            this.facePlaceKeyDown = false;
        }
        if (this.autoMine == null) {
            this.autoMine = Modules.get().get(AutoMine.class);
        }
        if (!this.basePlaceEnabled.get().booleanValue()) {
            return;
        }
        if (this.mc.world.getPlayers().isEmpty()) {
            return;
        }
        if (this.pauseEatPlace.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        this.cachedValidPlaceSpots();
        PlacePosition tickBestPlacePos = null;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            PlacePosition testPos;
            if (player == this.mc.player || Friends.get().isFriend(player) || player.isDead() || this.ignoreNakeds.get().booleanValue() && this.isNaked(player) || player.squaredDistanceTo(this.mc.player.getEyePos()) > 144.0 || (testPos = this.findBestPlacePosition(player)) == null || tickBestPlacePos != null && !(testPos.damage > tickBestPlacePos.damage)) continue;
            tickBestPlacePos = testPos;
        }
        double currentPlaceDamage = tickBestPlacePos != null ? tickBestPlacePos.damage : 0.0;
        BlockPos bestBasePos = this.findBestBasePlacePosition(tickBestPlacePos);
        double bestBaseDamage = bestBasePos != null ? this.lastBasePlaceDamage : 0.0;
        boolean shouldBasePlace = false;
        if (bestBasePos != null) {
            double refDamage;
            shouldBasePlace = tickBestPlacePos != null ? bestBaseDamage >= currentPlaceDamage + 10.0 : ((refDamage = this.computeBestPlaceDamageIgnoringCrystals()) > 0.0 ? bestBaseDamage >= refDamage + 10.0 : true);
        }
        if (shouldBasePlace) {
            if (this.pendingBasePlacePos != null) {
                Block block = this.mc.world.getBlockState(this.pendingBasePlacePos).getBlock();
                if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                    this.pendingBasePlacePos = null;
                } else if (System.currentTimeMillis() - this.pendingBasePlaceTimeMS >= 500L) {
                    this.doBasePlace(bestBasePos);
                }
            } else {
                this.doBasePlace(bestBasePos);
            }
        }
        for (PlacePosition p : this._placePositions) {
            this.placePositionPool.free(p);
        }
        this._placePositions.clear();
    }

    private void update() {
        long now;
        if (this.mc.player == null || this.mc.world == null || this.mc.world.getPlayers().isEmpty()) {
            return;
        }
        if (this.autoMine == null) {
            this.autoMine = Modules.get().get(AutoMine.class);
        }
        if (this.pendingPacketBreakBasePos != null && (double)((now = System.currentTimeMillis()) - this.pendingPacketBreakTimeMS) / 1000.0 >= 0.05) {
            EndCrystalEntity target = null;
            for (Entity entity : this.mc.world.getEntities()) {
                EndCrystalEntity crystal;
                if (!(entity instanceof EndCrystalEntity) || !(crystal = (EndCrystalEntity)entity).getBlockPos().down().equals((Object)this.pendingPacketBreakBasePos)) continue;
                target = crystal;
                break;
            }
            if (target != null && !target.isRemoved() && this.shouldBreakCrystal((Entity)target) && this.inBreakRange(target.getPos())) {
                this.breakCrystal((Entity)target);
                this.pendingPacketBreakId = null;
                this.pendingPacketBreakPos = null;
                this.pendingPacketBreakBasePos = null;
            } else if (now - this.pendingPacketBreakTimeMS > 200L) {
                this.pendingPacketBreakId = null;
                this.pendingPacketBreakPos = null;
                this.pendingPacketBreakBasePos = null;
            } else {
                this.pendingPacketBreakTimeMS = now;
            }
        }
        for (PlacePosition p : this._placePositions) {
            this.placePositionPool.free(p);
        }
        this._placePositions.clear();
        boolean eating = this.mc.player.isUsingItem();
        PlacePosition bestPlacePos = null;
        if (this.isManual()) {
            bestPlacePos = this.getManualPlacePos();
        } else if (!(!this.placeCrystals.get().booleanValue() || this.pauseEatPlace.get().booleanValue() && eating)) {
            this.cachedValidPlaceSpots();
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                PlacePosition testPos;
                if (player == this.mc.player || Friends.get().isFriend(player) || player.isDead() || this.ignoreNakeds.get().booleanValue() && ((ItemStack)player.getInventory().armor.get(0)).isEmpty() && ((ItemStack)player.getInventory().armor.get(1)).isEmpty() && ((ItemStack)player.getInventory().armor.get(2)).isEmpty() && ((ItemStack)player.getInventory().armor.get(3)).isEmpty() || player.squaredDistanceTo(this.mc.player.getEyePos()) > 144.0 || (testPos = this.findBestPlacePosition(player)) == null || bestPlacePos != null && !(testPos.damage > bestPlacePos.damage)) continue;
                bestPlacePos = testPos;
            }
            if (bestPlacePos != null && this.placeSpeedCheck(bestPlacePos.isSlowPlace)) {
                this.pendingBasePlacePos = null;
                if (this.placeCrystal(bestPlacePos.blockPos.down(), bestPlacePos.damage)) {
                    this.lastPlaceTimeMS = System.currentTimeMillis();
                }
            }
        }
        if (!(!this.breakCrystals.get().booleanValue() || this.pauseEatBreak.get().booleanValue() && this.mc.player.isUsingItem())) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity) || !this.inBreakRange(entity.getPos()) || !this.shouldBreakCrystal(entity)) continue;
                if (!this.breakSpeedCheck()) break;
                BlockPos crystalBase = entity.getBlockPos().down();
                if (this.breakCrystal(entity) || !this.rotateBreak.get().booleanValue() || MeteorClient.ROTATION.lookingAt(entity.getBoundingBox())) continue;
                break;
            }
        }
    }

    private double computeBestPlaceDamageIgnoringCrystals() {
        if (this.mc.player == null || this.mc.world == null) {
            return 0.0;
        }
        double bestDamage = 0.0;
        int r = (int)Math.floor(this.placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        this.updateCalcIgnoreSet();
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    double selfDamage;
                    Box crystalBox;
                    BlockPos downPos;
                    BlockState downState;
                    Block downBlock;
                    BlockPos pos = new BlockPos(ex + x, ey + y, ez + z);
                    if (!this.mc.world.isAir(pos) || (downBlock = (downState = this.mc.world.getBlockState(downPos = pos.down())).getBlock()) != Blocks.OBSIDIAN && downBlock != Blocks.BEDROCK || !this.inPlaceRange(downPos) || this.intersectsWithNonCrystalEntities(crystalBox = new Box((double)downPos.getX(), (double)(downPos.getY() + 1), (double)downPos.getZ(), (double)(downPos.getX() + 1), (double)(downPos.getY() + 3), (double)(downPos.getZ() + 1))) || (selfDamage = DamageUtils.newCrystalDamage((PlayerEntity)this.mc.player, this.mc.player.getBoundingBox(), new Vec3d((double)downPos.getX() + 0.5, (double)(downPos.getY() + 1), (double)downPos.getZ() + 0.5), this._calcIgnoreSet)) > this.maxPlace.get()) continue;
                    for (PlayerEntity player : this.mc.world.getPlayers()) {
                        double targetDamage;
                        Box targetBox;
                        if (player == this.mc.player || player.isDead() || Friends.get().isFriend(player) || this.ignoreNakeds.get().booleanValue() && this.isNaked(player) || player.squaredDistanceTo(this.mc.player.getEyePos()) > 144.0 || (targetBox = player.getBoundingBox()).intersects(crystalBox) || !((targetDamage = DamageUtils.newCrystalDamage(player, targetBox, new Vec3d((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5), this._calcIgnoreSet)) > bestDamage)) continue;
                        bestDamage = targetDamage;
                    }
                }
            }
        }
        return bestDamage;
    }

    private boolean intersectsWithNonCrystalEntities(Box box) {
        return EntityUtils.intersectsWithEntity(box, entity -> {
            if (entity.isRemoved()) {
                return false;
            }
            if (entity instanceof EndCrystalEntity) {
                return false;
            }
            if (this.ignoreItem.get().booleanValue() && entity instanceof ItemEntity) {
                return false;
            }
            if (entity instanceof ItemEntity) {
                ItemEntity item = (ItemEntity)entity;
                if (item.age < 10) {
                    return false;
                }
            }
            return true;
        });
    }

    private BlockPos findBestBasePlacePosition(PlacePosition currentBestPlace) {
        if (this.mc.player == null || this.mc.world == null || this.mc.world.getPlayers().isEmpty()) {
            return null;
        }
        int r = (int)Math.floor(this.placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        this.updateCalcIgnoreSet();
        BlockPos bestOpenPos = null;
        double bestOpenDamage = 0.0;
        BlockPos bestBlockedPos = null;
        double bestBlockedDamage = 0.0;
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    double selfDamage;
                    BlockPos basePos;
                    BlockState baseState;
                    Block crystalBlock;
                    boolean crystalSpaceOpen;
                    BlockPos crystalPos = new BlockPos(ex + x, ey + y, ez + z);
                    boolean bl = crystalSpaceOpen = this.mc.world.isAir(crystalPos) || this._calcIgnoreSet.contains(crystalPos);
                    if (!crystalSpaceOpen && ((crystalBlock = this.mc.world.getBlockState(crystalPos).getBlock()) == Blocks.WATER || crystalBlock == Blocks.LAVA) || (baseState = this.mc.world.getBlockState(basePos = crystalPos.down())).getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK || !baseState.isAir() && !baseState.isReplaceable() || !this.inPlaceRange(basePos)) continue;
                    Vec3d explosionPos = new Vec3d((double)basePos.getX() + 0.5, (double)(basePos.getY() + 1), (double)basePos.getZ() + 0.5);
                    HashMap<BlockPos, BlockState> overrides = new HashMap<BlockPos, BlockState>();
                    overrides.put(basePos.toImmutable(), Blocks.OBSIDIAN.getDefaultState());
                    if (!crystalSpaceOpen) {
                        overrides.put(crystalPos.toImmutable(), Blocks.AIR.getDefaultState());
                    }
                    if ((selfDamage = DamageUtils.newCrystalDamage((PlayerEntity)this.mc.player, this.mc.player.getBoundingBox(), explosionPos, this._calcIgnoreSet, overrides)) > this.maxPlace.get()) continue;
                    for (PlayerEntity player : this.mc.world.getPlayers()) {
                        double targetDamage;
                        Box crystalBox;
                        Box targetBox;
                        if (player == this.mc.player || player.isDead() || Friends.get().isFriend(player) || this.ignoreNakeds.get().booleanValue() && this.isNaked(player) || player.squaredDistanceTo(this.mc.player.getEyePos()) > 144.0 || (targetBox = player.getBoundingBox()).intersects(crystalBox = new Box((double)basePos.getX(), (double)(basePos.getY() + 1), (double)basePos.getZ(), (double)(basePos.getX() + 1), (double)(basePos.getY() + 3), (double)(basePos.getZ() + 1))) || !((targetDamage = DamageUtils.newCrystalDamage(player, targetBox, explosionPos, this._calcIgnoreSet, overrides)) >= this.minPlace.get())) continue;
                        if (crystalSpaceOpen) {
                            if (!(targetDamage > bestOpenDamage)) continue;
                            bestOpenDamage = targetDamage;
                            bestOpenPos = basePos.toImmutable();
                            continue;
                        }
                        if (!(targetDamage > bestBlockedDamage)) continue;
                        bestBlockedDamage = targetDamage;
                        bestBlockedPos = basePos.toImmutable();
                    }
                }
            }
        }
        if (bestOpenPos != null) {
            this.lastBasePlaceDamage = bestOpenDamage;
            return bestOpenPos;
        }
        if (bestBlockedPos != null && currentBestPlace == null) {
            this.lastBasePlaceDamage = bestBlockedDamage;
            return bestBlockedPos;
        }
        return null;
    }

    private void doBasePlace(BlockPos pos) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastBasePlaceAttemptMS < 500L) {
            return;
        }
        this.lastBasePlaceAttemptMS = now;
        Block current = this.mc.world.getBlockState(pos).getBlock();
        if (current == Blocks.OBSIDIAN || current == Blocks.BEDROCK) {
            return;
        }
        Item item = Blocks.OBSIDIAN.asItem();
        FindItemResult result = InvUtils.findInHotbar(item);
        if (!result.found()) {
            return;
        }
        ArrayList<BlockPos> toPlace = new ArrayList<BlockPos>();
        toPlace.add(pos);
        if (MeteorClient.BLOCK.beginPlacement(toPlace, item)) {
            for (BlockPos p : toPlace) {
                MeteorClient.BLOCK.placeBlock(item, p);
            }
            MeteorClient.BLOCK.endPlacement();
            this.pendingBasePlacePos = pos.toImmutable();
            this.pendingBasePlaceTimeMS = now;
        }
    }

    private boolean isNaked(PlayerEntity player) {
        return ((ItemStack)player.getInventory().armor.get(0)).isEmpty() && ((ItemStack)player.getInventory().armor.get(1)).isEmpty() && ((ItemStack)player.getInventory().armor.get(2)).isEmpty() && ((ItemStack)player.getInventory().armor.get(3)).isEmpty();
    }

    public boolean placeCrystal(BlockPos blockPos, double damage) {
        if (blockPos == null || this.mc.player == null) {
            return false;
        }
        BlockPos crystaBlockPos = blockPos.up();
        Box box = new Box((double)crystaBlockPos.getX(), (double)crystaBlockPos.getY(), (double)crystaBlockPos.getZ(), (double)(crystaBlockPos.getX() + 1), (double)(crystaBlockPos.getY() + 2), (double)(crystaBlockPos.getZ() + 1));
        if (this.intersectsWithEntities(box)) {
            return false;
        }
        FindItemResult result = InvUtils.find(Items.END_CRYSTAL);
        if (!result.found()) {
            return false;
        }
        if (this.rotatePlace.get().booleanValue()) {
            MeteorClient.ROTATION.requestRotation(blockPos.toCenterPos(), 10.0);
            if (!this.doGrimSwap && !MeteorClient.ROTATION.lookingAt(new Box(blockPos))) {
                return false;
            }
        }
        long currentTime = System.currentTimeMillis();
        if (this.crystalPlaceDelays.containsKey(blockPos) && (double)(currentTime - this.crystalPlaceDelays.get(blockPos)) / 1000.0 < this.placeDelay.get()) {
            return false;
        }
        if (this.renderer.renderPlace.get().booleanValue()) {
            this.renderStack = new ItemStack((ItemConvertible)Items.END_CRYSTAL);
            this.renderTimer = 3;
        }
        if (!MeteorClient.SWAP.beginSwap(result, true)) {
            return false;
        }
        PacketManager.INSTANCE.incrementGlobal();
        this.crystalPlaceDelays.put(blockPos, currentTime);
        this.renderer.onPlaceCrystal(blockPos, damage);
        BlockHitResult calculatedHitResult = AutoCrystalUtil.getPlaceBlockHitResult(blockPos);
        Hand hand = Hand.MAIN_HAND;
        if (this.grimPlace.get().booleanValue()) {
            if (this.grimMode.get() == GrimMode.Full) {
                this.doGrimSwap = true;
            } else if (this.grimMode.get() == GrimMode.OneToOne) {
                this.doGrimSwap = this.grimRotationIndex >= 1;
                ++this.grimRotationIndex;
                if (this.grimRotationIndex > 1) {
                    this.grimRotationIndex = 0;
                }
            } else {
                this.doGrimSwap = this.grimRotationIndex >= 2;
                ++this.grimRotationIndex;
                if (this.grimRotationIndex > 2) {
                    this.grimRotationIndex = 0;
                }
            }
        } else {
            this.doGrimSwap = false;
            this.grimRotationIndex = 0;
        }
        if (this.doGrimSwap) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
            hand = Hand.OFF_HAND;
        }
        int s = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractBlockC2SPacket(hand, calculatedHitResult, s));
        PacketManager.INSTANCE.incrementPlace();
        if (this.doGrimSwap) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
        }
        if (this.placeSwingMode.get() == SwingMode.Client) {
            this.mc.player.swingHand(hand);
        }
        if (this.placeSwingMode.get() == SwingMode.Packet) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(hand));
            PacketManager.INSTANCE.incrementInteract();
        }
        MeteorClient.SWAP.endSwap(true);
        PacketManager.INSTANCE.incrementGlobal();
        if (this.pendingBasePlacePos != null && this.pendingBasePlacePos.equals((Object)blockPos)) {
            this.pendingBasePlacePos = null;
        }
        return true;
    }

    private void attackCrystal(Entity entity) {
        this.renderer.onBreakCrystal(entity);
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack((Entity)entity, (boolean)this.mc.player.isSneaking());
        this.mc.getNetworkHandler().sendPacket((Packet)packet);
        PacketManager.INSTANCE.incrementInteract();
        if (this.breakSwingMode.get() == SwingMode.Client) {
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (this.breakSwingMode.get() == SwingMode.Packet) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            PacketManager.INSTANCE.incrementInteract();
        }
        this.lastBreakTimeMS = System.currentTimeMillis();
    }

    public boolean breakCrystal(Entity entity) {
        if (this.mc.player == null) {
            return false;
        }
        if (this.rotateBreak.get().booleanValue()) {
            MeteorClient.ROTATION.requestRotation(entity.getPos(), 10.0);
            if (!MeteorClient.ROTATION.lookingAt(entity.getBoundingBox())) {
                return false;
            }
        }
        long currentTime = System.currentTimeMillis();
        if (this.crystalBreakDelays.containsKey(entity.getId()) && (double)(currentTime - this.crystalBreakDelays.get(entity.getId())) / 1000.0 < this.breakDelay.get()) {
            return false;
        }
        this.crystalBreakDelays.put(entity.getId(), currentTime);
        this.renderer.onBreakCrystal(entity);
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack((Entity)entity, (boolean)this.mc.player.isSneaking());
        this.mc.getNetworkHandler().sendPacket((Packet)packet);
        PacketManager.INSTANCE.incrementInteract();
        if (this.breakSwingMode.get() == SwingMode.Client) {
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (this.breakSwingMode.get() == SwingMode.Packet) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            PacketManager.INSTANCE.incrementInteract();
        }
        this.explodedCrystals.add(entity.getId());
        this.lastBreakTimeMS = System.currentTimeMillis();
        return true;
    }

    private void updateCalcIgnoreSet() {
        double prog;
        this._calcIgnoreSet.clear();
        if (!this.antiSurroundPlace.get().booleanValue()) {
            return;
        }
        SilentMine silentMine = Modules.get().get(SilentMine.class);
        if (!silentMine.isActive()) {
            return;
        }
        double required = this.silentMineProgressThreshold.get() / 100.0;
        BlockPos delayed = silentMine.getDelayedDestroyBlockPos();
        if (delayed != null && (prog = silentMine.getDelayedDestroyProgress()) >= required) {
            this._calcIgnoreSet.add(delayed);
        }
        BlockPos rebreak = silentMine.getRebreakBlockPos();
        boolean isOpen = silentMine.canRebreakRebreakBlock();
        if (rebreak != null && isOpen) {
            this._calcIgnoreSet.add(rebreak);
        }
    }

    private PlacePosition findBestPlacePosition(PlayerEntity target) {
        PlacePosition bestClean = this.placePositionPool.get();
        PlacePosition bestDirty = this.placePositionPool.get();
        this._placePositions.add(bestClean);
        this._placePositions.add(bestDirty);
        bestClean.damage = 0.0;
        bestClean.blockPos = null;
        bestClean.isSlowPlace = false;
        bestDirty.damage = 0.0;
        bestDirty.blockPos = null;
        bestDirty.isSlowPlace = false;
        int r = (int)Math.floor(this.placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        this.updateCalcIgnoreSet();
        if (this.terrainIgnoreCalc.get().booleanValue()) {
            this.addExplodableBlocks(target.getBlockPos(), this._calcIgnoreSet);
        }
        boolean shouldFacePlace = false;
        if (this.facePlaceMissingArmor.get().booleanValue() && (((ItemStack)target.getInventory().armor.get(0)).isEmpty() || ((ItemStack)target.getInventory().armor.get(1)).isEmpty() || ((ItemStack)target.getInventory().armor.get(2)).isEmpty() || ((ItemStack)target.getInventory().armor.get(3)).isEmpty())) {
            shouldFacePlace = true;
        }
        if (this.currentFacePlaceMode != FacePlaceMode.Off) {
            shouldFacePlace = true;
        }
        Vec3d predictedPos = this.getPredictedPos(target);
        Box predictedBox = target.getBoundingBox().offset(predictedPos.subtract(target.getPos()));
        int dim = 2 * r + 1;
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    BlockPos.Mutable pos;
                    Box crystalBox;
                    int idx = (x + r) * dim * dim + (y + r) * dim + (z + r);
                    if (idx < 0 || idx >= this.cachedValidSpots.size() || !this.cachedValidSpots.get(idx).booleanValue() || predictedBox.intersects(crystalBox = new Box((double)(pos = this.mutablePos.set(ex + x, ey + y, ez + z)).getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1)))) continue;
                    boolean isCleanSpot = !this.intersectsWithEntities(crystalBox, false);
                    boolean isDirtySpot = false;
                    if (!isCleanSpot && this.ignoreItem.get().booleanValue()) {
                        boolean bl = isDirtySpot = !this.intersectsWithEntities(crystalBox, true);
                    }
                    if (!isCleanSpot && !isDirtySpot) continue;
                    double targetDamage = DamageUtils.newCrystalDamage(target, predictedBox, new Vec3d((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5), this._calcIgnoreSet);
                    if (targetDamage < (shouldFacePlace ? 1.0 : this.minPlace.get())) continue;
                    boolean isSlowPlace = false;
                    if (this.slowPlace.get().booleanValue() && targetDamage <= this.slowPlaceMaxDamage.get() && targetDamage >= this.slowPlaceMinDamage.get()) {
                        isSlowPlace = true;
                    }
                    if (isCleanSpot) {
                        if (!(targetDamage > bestClean.damage)) continue;
                        bestClean.blockPos = pos.toImmutable();
                        bestClean.damage = targetDamage;
                        bestClean.isSlowPlace = isSlowPlace;
                        continue;
                    }
                    if (!(targetDamage > bestDirty.damage)) continue;
                    bestDirty.blockPos = pos.toImmutable();
                    bestDirty.damage = targetDamage;
                    bestDirty.isSlowPlace = isSlowPlace;
                }
            }
        }
        if (bestClean.blockPos != null) {
            return bestClean;
        }
        if (bestDirty.blockPos != null) {
            return bestDirty;
        }
        return null;
    }

    private void cachedValidPlaceSpots() {
        int r = (int)Math.floor(this.placeRange.get());
        int dim = 2 * r + 1;
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        Box box = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.updateCalcIgnoreSet();
        this.cachedValidSpots.clear();
        for (int i = 0; i < dim * dim * dim; ++i) {
            this.cachedValidSpots.add(false);
        }
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    double selfDamage;
                    if (!this.mc.world.isAir((BlockPos)this.mutablePos.set(ex + x, ey + y, ez + z))) continue;
                    BlockPos.Mutable downPos = this.mutablePos.set(ex + x, ey + y - 1, ez + z);
                    BlockState downState = this.mc.world.getBlockState((BlockPos)downPos);
                    Block downBlock = downState.getBlock();
                    if (downState.isAir() || downBlock != Blocks.OBSIDIAN && downBlock != Blocks.BEDROCK || !this.inPlaceRange((BlockPos)downPos)) continue;
                    ((IBox)box).set(downPos.getX(), downPos.getY() + 1, downPos.getZ(), downPos.getX() + 1, downPos.getY() + 3, downPos.getZ() + 1);
                    if (this.intersectsWithEntities(box) || (selfDamage = DamageUtils.newCrystalDamage((PlayerEntity)this.mc.player, this.mc.player.getBoundingBox(), new Vec3d((double)downPos.getX() + 0.5, (double)(downPos.getY() + 1), (double)downPos.getZ() + 0.5), this._calcIgnoreSet)) > this.maxPlace.get()) continue;
                    int idx = (x + r) * dim * dim + (y + r) * dim + (z + r);
                    this.cachedValidSpots.set(idx, true);
                }
            }
        }
    }

    public void preplaceCrystal(BlockPos crystalBlockPos, boolean snapAt) {
        BlockPos blockPos = crystalBlockPos.down();
        this.crystalPlaceDelays.remove(blockPos);
        Box box = new Box((double)crystalBlockPos.getX(), (double)crystalBlockPos.getY(), (double)crystalBlockPos.getZ(), (double)(crystalBlockPos.getX() + 1), (double)(crystalBlockPos.getY() + 2), (double)(crystalBlockPos.getZ() + 1));
        if (this.intersectsWithEntities(box)) {
            return;
        }
        if (this.rotatePlace.get().booleanValue() && snapAt && !MeteorClient.ROTATION.lookingAt(new Box(blockPos))) {
            MeteorClient.ROTATION.snapAt(blockPos.toCenterPos());
        }
        this.placeCrystal(blockPos, 0.0);
    }

    public boolean inPlaceRange(BlockPos blockPos) {
        Vec3d from = this.mc.player.getEyePos();
        return blockPos.toCenterPos().distanceTo(from) <= this.placeRange.get();
    }

    public boolean inBreakRange(Vec3d pos) {
        Vec3d from = this.mc.player.getEyePos();
        return pos.distanceTo(from) <= this.breakRange.get();
    }

    public boolean shouldBreakCrystal(Entity entity) {
        double selfDamage;
        SilentMine silentMine;
        if (this.isManual()) {
            return this.inBreakRange(entity.getPos());
        }
        boolean damageCheck = false;
        HashSet<BlockPos> ignore = new HashSet<BlockPos>();
        if (this.terrainIgnoreBreak.get().booleanValue()) {
            this.addExplodableBlocks(entity.getBlockPos(), ignore);
        }
        if (this.antiSurroundPlace.get().booleanValue() && (silentMine = Modules.get().get(SilentMine.class)) != null && silentMine.isActive()) {
            BlockPos rebreak;
            double required = this.silentMineProgressThreshold.get() / 100.0;
            BlockPos delayed = silentMine.getDelayedDestroyBlockPos();
            if (delayed != null && silentMine.getDelayedDestroyProgress() >= required) {
                ignore.add(delayed);
            }
            if ((rebreak = silentMine.getRebreakBlockPos()) != null && silentMine.canRebreakRebreakBlock()) {
                ignore.add(rebreak);
            }
        }
        if ((selfDamage = DamageUtils.newCrystalDamage((PlayerEntity)this.mc.player, this.mc.player.getBoundingBox(), entity.getPos(), ignore)) > this.maxBreak.get()) {
            return false;
        }
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            double targetDamage;
            if (player == this.mc.player || player.isDead() || Friends.get().isFriend(player) || player.squaredDistanceTo(this.mc.player.getEyePos()) > 196.0 || !((targetDamage = DamageUtils.newCrystalDamage(player, player.getBoundingBox(), entity.getPos(), ignore)) >= this.minBreak.get())) continue;
            damageCheck = true;
            break;
        }
        return damageCheck;
    }

    private void addExplodableBlocks(BlockPos center, Set<BlockPos> set) {
        if (center == null || this.mc.world == null) {
            return;
        }
        int r = 8;
        BlockPos.Mutable mut = new BlockPos.Mutable();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    mut.set(cx + x, cy + y, cz + z);
                    BlockState state = this.mc.world.getBlockState((BlockPos)mut);
                    if (state.isAir() || !(state.getBlock().getBlastResistance() < 600.0f)) continue;
                    set.add(mut.toImmutable());
                }
            }
        }
    }

    @EventHandler(priority=200)
    private void onEntity(EntityAddedEvent event) {
        Entity entity = event.entity;
        if (!(entity instanceof EndCrystalEntity)) {
            return;
        }
        BlockPos blockPos = entity.getBlockPos().down();
        boolean isOwnCrystal = this.crystalPlaceDelays.containsKey(blockPos);
        if (isOwnCrystal) {
            this.crystalPlaceDelays.remove(blockPos);
        }
        if (this.pendingBasePlacePos != null && this.pendingBasePlacePos.equals((Object)blockPos)) {
            this.pendingBasePlacePos = null;
        }
        PacketBreakMode mode = this.packetBreakMode.get();
        if (this.breakCrystals.get().booleanValue() && (mode == PacketBreakMode.EntityBased || mode == PacketBreakMode.Both)) {
            if (!this.inBreakRange(entity.getPos())) {
                return;
            }
            if (!this.shouldBreakCrystal(entity)) {
                return;
            }
            if (!this.breakSpeedCheck()) {
                return;
            }
            this.breakCrystal(entity);
        }
    }

    public double getDamageForPos(BlockPos blockPos) {
        if (this.mc.world == null || this.mc.player == null) {
            return 0.0;
        }
        Vec3d crystalVec = new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
        double maxDmg = 0.0;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            double dmg;
            if (player == this.mc.player || Friends.get().isFriend(player) || player.isDead() || this.ignoreNakeds.get().booleanValue() && ((ItemStack)player.getInventory().armor.get(0)).isEmpty() && ((ItemStack)player.getInventory().armor.get(1)).isEmpty() && ((ItemStack)player.getInventory().armor.get(2)).isEmpty() && ((ItemStack)player.getInventory().armor.get(3)).isEmpty() || player.squaredDistanceTo(this.mc.player.getEyePos()) > 144.0) continue;
            this.updateCalcIgnoreSet();
            if (this.terrainIgnoreCalc.get().booleanValue()) {
                this.addExplodableBlocks(player.getBlockPos(), this._calcIgnoreSet);
            }
            if (!((dmg = DamageUtils.newCrystalDamage(player, player.getBoundingBox(), crystalVec, this._calcIgnoreSet)) > maxDmg)) continue;
            maxDmg = dmg;
        }
        return maxDmg;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        this.renderer.onRender2D(event);
    }

    @EventHandler(priority=201)
    private void onRender3D(Render3DEvent event) {
        if (!this.isActive()) {
            return;
        }
        this.update();
        this.renderer.onRender3D(event);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent.Death event) {
        if (event.getPlayer() == null || event.getPlayer() == this.mc.player) {
            return;
        }
        if (this.setPlayerDead.get().booleanValue()) {
            event.getPlayer().setHealth(0.0f);
        }
    }

    private boolean intersectsWithEntities(Box box) {
        return this.intersectsWithEntities(box, this.ignoreItem.get());
    }

    private boolean intersectsWithEntities(Box box, boolean ignoreItems) {
        return EntityUtils.intersectsWithEntity(box, entity -> {
            if (entity.isRemoved()) {
                return false;
            }
            if (this.explodedCrystals.contains(entity.getId())) {
                return false;
            }
            if (ignoreItems && entity instanceof ItemEntity) {
                return false;
            }
            if (entity instanceof ItemEntity) {
                ItemEntity item = (ItemEntity)entity;
                if (item.age < 10) {
                    return false;
                }
            }
            return true;
        });
    }

    private boolean breakSpeedCheck() {
        long currentTime = System.currentTimeMillis();
        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.MEDIUM)) {
            return (double)(currentTime - this.lastBreakTimeMS) / 1000.0 > 0.09090909090909091;
        }
        return this.breakSpeedLimit.get() == 0.0 || (double)(currentTime - this.lastBreakTimeMS) / 1000.0 > 1.0 / this.breakSpeedLimit.get();
    }

    private boolean placeSpeedCheck(boolean slowPlace) {
        long currentTime = System.currentTimeMillis();
        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.MEDIUM)) {
            return (double)(currentTime - this.lastPlaceTimeMS) / 1000.0 > 0.09090909090909091;
        }
        double placeSpeed = slowPlace ? this.slowPlaceSpeed.get() : this.placeSpeedLimit.get();
        return placeSpeed == 0.0 || (double)(currentTime - this.lastPlaceTimeMS) / 1000.0 > 1.0 / placeSpeed;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(this.getCPS());
    }

    public long getCPS() {
        long currentTime = System.currentTimeMillis();
        long count = this.crystalBreakDelays.values().stream().filter(x -> currentTime - x <= 600L).count();
        return Math.round((double)count * 1.666666);
    }

    private void sendChatFeedback(String state) {
        MutableText body;
        if (this.mc.player == null) {
            return;
        }
        MutableText prefix = Text.literal((String)"[AutoCrystal] ").formatted(Formatting.GOLD);
        switch (state) {
            case "started": {
                body = Text.literal((String)"FacePlace enabled.").formatted(Formatting.GREEN);
                break;
            }
            case "stopped": {
                body = Text.literal((String)"FacePlace disabled.").formatted(Formatting.RED);
                break;
            }
            default: {
                return;
            }
        }
        this.mc.player.sendMessage((Text)prefix.append((Text)body), true);
    }

    public static enum FacePlaceMode {
        Fast,
        Off;

    }

    public static enum GrimMode {
        OneToOne,
        TwoToOne,
        Full;

    }

    public static enum PacketBreakMode {
        None,
        EntityBased,
        IdBased,
        Both;

    }

    public static enum SwingMode {
        Packet,
        Client,
        None;

    }

    private class PlacePosition {
        public BlockPos blockPos;
        public double damage = 0.0;
        public boolean isSlowPlace = false;

        private PlacePosition(AutoCrystal autoCrystal) {
        }
    }
}

