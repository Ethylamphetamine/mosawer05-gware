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
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
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
package meteordevelopment.meteorclient.systems.modules.combat.newAutocrystal;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.PlayerDeathEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
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
import meteordevelopment.meteorclient.systems.modules.combat.newAutocrystal.NewAutoCrystalRenderer;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
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

public class NewAutoCrystal
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgFacePlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgSwing;
    private final SettingGroup sgRange;
    private final Setting<Boolean> placeCrystals;
    private final Setting<Boolean> manualOverride;
    private final Setting<Boolean> pauseEatPlace;
    private final Setting<Boolean> breakCrystals;
    private final Setting<Boolean> pauseEatBreak;
    private final Setting<Boolean> pauseWhileFlying;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<Boolean> setPlayerDead;
    private final Setting<Boolean> terrainIgnoreCalc;
    private final Setting<Integer> crystalSlot;
    private final Setting<Boolean> debugMessage;
    private final Setting<Double> placeSpeedLimit;
    private final Setting<Double> minPlace;
    private final Setting<Double> maxPlace;
    private final Setting<Boolean> basePlaceEnabled;
    private final Setting<Boolean> antiSurroundPlace;
    private final Setting<Double> silentMineProgressThreshold;
    private final Setting<Double> placeDelay;
    private final Setting<Boolean> ignoreItem;
    private final Setting<Boolean> fastPlace;
    private final Setting<Boolean> grimPlace;
    private final Setting<Boolean> facePlaceMissingArmor;
    private final Setting<Keybind> facePlaceKeybind;
    private final Setting<Boolean> slowPlace;
    private final Setting<Double> slowPlaceMinDamage;
    private final Setting<Double> slowPlaceMaxDamage;
    private final Setting<Double> slowPlaceSpeed;
    private final Setting<Double> breakSpeedLimit;
    private final Setting<Boolean> terrainIgnoreBreak;
    private final Setting<Double> minBreak;
    private final Setting<Double> maxBreak;
    private final Setting<Double> breakDelay;
    private final Setting<PacketBreakMode> packetBreakMode;
    private final Setting<SwingMode> breakSwingMode;
    private final Setting<SwingMode> placeSwingMode;
    private final Setting<Double> placeRange;
    private final Setting<Double> breakRange;
    private final Pool<PlacePosition> placePositionPool;
    private final List<PlacePosition> _placePositions;
    private final BlockPos.Mutable mutablePos;
    private final IntSet explodedCrystals;
    private final Map<Integer, Long> crystalBreakDelays;
    private final Map<BlockPos, Long> crystalPlaceDelays;
    private final List<Boolean> cachedValidSpots;
    private long lastPlaceTimeMS;
    private long lastBreakTimeMS;
    private double lastBasePlaceDamage;
    private BlockPos lastPacketBreakPos;
    private long lastPacketBreakTimeMS;
    private Integer pendingPacketBreakId;
    private Vec3d pendingPacketBreakPos;
    private BlockPos pendingPacketBreakBasePos;
    private long pendingPacketBreakTimeMS;
    private AutoMine autoMine;
    private Set<BlockPos> _calcIgnoreSet;
    private FacePlaceMode currentFacePlaceMode;
    private boolean facePlaceKeyDown;
    private BlockPos pendingBasePlacePos;
    private long pendingBasePlaceTimeMS;
    private static final long BASE_PLACE_COOLDOWN_MS = 500L;
    private final Map<BlockPos, Long> placeSentTimestamps;
    private final Map<BlockPos, Long> placeToBreakTimestamps;
    private int debugCrystalCount;
    private final List<Long> crystalLifetimes;
    private final List<Long> placeToSpawnDelays;
    private final List<Long> spawnToBreakDelays;
    private final List<Long> breakToPlaceDelays;
    private long lastBreakSentTimeMS;
    private static final int DIAG_MAX_SAMPLES = 50;
    private boolean awaitingPlaceConfirmation;
    private BlockPos awaitingPlacePos;
    private long awaitingPlaceTimeMS;
    private static final long PLACE_CONFIRMATION_TIMEOUT_MS = 100L;
    private boolean placedThisCycle;
    private float[] pendingFlyingRotation;
    private final NewAutoCrystalRenderer renderer;

    public NewAutoCrystal() {
        super(Categories.Combat, "new-auto-crystal", "Tick-based auto crystal with silent rotations.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgFacePlace = this.settings.createGroup("Face Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgSwing = this.settings.createGroup("Swing");
        this.sgRange = this.settings.createGroup("Range");
        this.placeCrystals = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place")).description("Places crystals.")).defaultValue(true)).build());
        this.manualOverride = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("manual-override")).description("Places crystals at your crosshair when holding Right Click with a crystal.")).defaultValue(true)).build());
        this.pauseEatPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat-place")).description("Pauses placing when eating")).defaultValue(true)).build());
        this.breakCrystals = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("break")).description("Breaks crystals.")).defaultValue(true)).build());
        this.pauseEatBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat-break")).description("Pauses breaking when eating")).defaultValue(false)).build());
        this.pauseWhileFlying = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-while-flying")).description("Pauses all AutoCrystal actions while flying.")).defaultValue(true)).build());
        this.ignoreNakeds = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Ignore players with no items.")).defaultValue(true)).build());
        this.setPlayerDead = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("set-player-dead-instantly")).description("Tries to not blow up loot by instantly killing the player in the packet they die.")).defaultValue(true)).build());
        this.terrainIgnoreCalc = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("calc-ignore-terrain")).description("Treats explodable blocks (terrain) as air for damage calculations.")).defaultValue(true)).build());
        this.crystalSlot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("crystal-slot")).description("The hotbar slot to move Crystals to if not found in hotbar.")).defaultValue(9)).range(1, 9).sliderMin(1).sliderMax(9).build());
        this.debugMessage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("debug-message")).description("Gives out a debug message")).defaultValue(false)).build());
        this.placeSpeedLimit = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-speed-limit")).description("Maximum crystals to place per second.")).defaultValue(40.0).min(0.0).sliderRange(0.0, 40.0).build());
        this.minPlace = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-place")).description("Minimum enemy damage to place.")).defaultValue(8.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.maxPlace = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-place")).description("Max self damage to place.")).defaultValue(20.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.basePlaceEnabled = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("base-place")).description("Places obsidian before placing crystals if needed.")).defaultValue(false)).build());
        this.antiSurroundPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-surround")).description("Ignores auto-mine blocks from calculations.")).defaultValue(true)).build());
        this.silentMineProgressThreshold = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("silent-mine-progress-threshold")).description("Min SilentMine progress (%) before ignoring block.")).defaultValue(80.0).min(0.0).sliderRange(0.0, 100.0).visible(this.antiSurroundPlace::get)).build());
        this.placeDelay = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-delay")).description("Seconds to wait before retrying a position.")).defaultValue(0.05).min(0.0).sliderMax(0.6).build());
        this.ignoreItem = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-item")).description("Tries to ignore items when placing.")).defaultValue(true)).build());
        this.fastPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fast-place")).description("Places crystals on the render thread with rotation bypass. It is faster but will rubberband")).defaultValue(false)).build());
        this.grimPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-place")).description("Place Faster")).defaultValue(true)).visible(this.fastPlace::get)).build());
        this.facePlaceMissingArmor = this.sgFacePlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("face-place-missing-armor")).description("Face places on missing armor.")).defaultValue(false)).build());
        this.facePlaceKeybind = this.sgFacePlace.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("face-place-toggle")).description("Toggles face place.")).defaultValue(Keybind.none())).build());
        this.slowPlace = this.sgFacePlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("slow-place")).description("Slowly places crystals at lower damages.")).defaultValue(false)).build());
        this.slowPlaceMinDamage = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slow-place-min-place")).description("Minimum damage to slow place.")).defaultValue(4.0).min(0.0).sliderRange(0.0, 20.0).visible(this.slowPlace::get)).build());
        this.slowPlaceMaxDamage = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slow-place-max-place")).description("Maximum damage to slow place.")).defaultValue(8.0).min(0.0).sliderRange(0.0, 20.0).visible(this.slowPlace::get)).build());
        this.slowPlaceSpeed = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slow-place-speed")).description("Speed at which to slow place.")).defaultValue(2.0).min(0.0).sliderRange(0.0, 20.0).visible(this.slowPlace::get)).build());
        this.breakSpeedLimit = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-speed-limit")).description("Maximum crystals to break per second.")).defaultValue(60.0).min(0.0).sliderRange(0.0, 60.0).build());
        this.terrainIgnoreBreak = this.sgBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-terrain-blocks")).description("Treats explodable blocks as air for break damage calculations.")).defaultValue(true)).build());
        this.minBreak = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-break")).description("Minimum enemy damage to break.")).defaultValue(3.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.maxBreak = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-break")).description("Max self damage to break.")).defaultValue(20.0).min(0.0).sliderRange(0.0, 20.0).build());
        this.breakDelay = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-delay")).description("Seconds to wait before retrying a crystal.")).defaultValue(0.05).min(0.0).sliderMax(0.6).build());
        this.packetBreakMode = this.sgBreak.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("packet-break-mode")).description("How packet break is handled.")).defaultValue(PacketBreakMode.Id)).build());
        this.breakSwingMode = this.sgSwing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("break-swing-mode")).description("Swing mode when breaking.")).defaultValue(SwingMode.None)).build());
        this.placeSwingMode = this.sgSwing.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("place-swing-mode")).description("Swing mode when placing.")).defaultValue(SwingMode.None)).build());
        this.placeRange = this.sgRange.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("Maximum distance to place crystals.")).defaultValue(4.0).build());
        this.breakRange = this.sgRange.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-range")).description("Maximum distance to break crystals.")).defaultValue(4.0).build());
        this.placePositionPool = new Pool<PlacePosition>(() -> new PlacePosition(this));
        this._placePositions = new ArrayList<PlacePosition>();
        this.mutablePos = new BlockPos.Mutable();
        this.explodedCrystals = new IntOpenHashSet();
        this.crystalBreakDelays = new ConcurrentHashMap<Integer, Long>();
        this.crystalPlaceDelays = new HashMap<BlockPos, Long>();
        this.cachedValidSpots = new ArrayList<Boolean>();
        this.lastPlaceTimeMS = 0L;
        this.lastBreakTimeMS = 0L;
        this.lastBasePlaceDamage = 0.0;
        this.lastPacketBreakPos = null;
        this.lastPacketBreakTimeMS = 0L;
        this.pendingPacketBreakId = null;
        this.pendingPacketBreakPos = null;
        this.pendingPacketBreakBasePos = null;
        this.pendingPacketBreakTimeMS = 0L;
        this._calcIgnoreSet = new HashSet<BlockPos>();
        this.currentFacePlaceMode = FacePlaceMode.Off;
        this.facePlaceKeyDown = false;
        this.pendingBasePlacePos = null;
        this.pendingBasePlaceTimeMS = 0L;
        this.placeSentTimestamps = new ConcurrentHashMap<BlockPos, Long>();
        this.placeToBreakTimestamps = new ConcurrentHashMap<BlockPos, Long>();
        this.debugCrystalCount = 0;
        this.crystalLifetimes = new ArrayList<Long>();
        this.placeToSpawnDelays = new ArrayList<Long>();
        this.spawnToBreakDelays = new ArrayList<Long>();
        this.breakToPlaceDelays = new ArrayList<Long>();
        this.lastBreakSentTimeMS = 0L;
        this.awaitingPlaceConfirmation = false;
        this.awaitingPlacePos = null;
        this.awaitingPlaceTimeMS = 0L;
        this.placedThisCycle = false;
        this.pendingFlyingRotation = null;
        this.renderer = new NewAutoCrystalRenderer(this);
    }

    @Override
    public void onActivate() {
        this.autoMine = Modules.get().get(AutoMine.class);
        this.explodedCrystals.clear();
        this.crystalBreakDelays.clear();
        this.crystalPlaceDelays.clear();
        this.lastPlaceTimeMS = 0L;
        this.lastBreakTimeMS = 0L;
        this.pendingBasePlacePos = null;
        this.pendingBasePlaceTimeMS = 0L;
        this.lastBasePlaceDamage = 0.0;
        this.facePlaceKeyDown = false;
        this.currentFacePlaceMode = FacePlaceMode.Off;
        this.placeSentTimestamps.clear();
        this.placeToBreakTimestamps.clear();
        this.debugCrystalCount = 0;
        this.crystalLifetimes.clear();
        this.placeToSpawnDelays.clear();
        this.spawnToBreakDelays.clear();
        this.breakToPlaceDelays.clear();
        this.lastBreakSentTimeMS = 0L;
        this.pendingFlyingRotation = null;
        this.awaitingPlaceConfirmation = false;
        this.awaitingPlacePos = null;
        this.awaitingPlaceTimeMS = 0L;
        this.placedThisCycle = false;
        this.renderer.onActivate();
    }

    @EventHandler(priority=100)
    private void onPreMovement(SendMovementPacketsEvent.Pre event) {
        Vec3d rotTarget;
        long now;
        if (!this.isActive() || this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.pauseWhileFlying.get().booleanValue() && this.mc.player.isFallFlying()) {
            return;
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
                this.doBreakCrystal((Entity)target);
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
        if ((rotTarget = this.findRotationTarget()) != null) {
            if (this.mc.player.isFallFlying()) {
                float[] angle = MeteorClient.ROTATION.getRotation(rotTarget);
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), angle[0], angle[1], this.mc.player.isOnGround()));
                PacketManager.INSTANCE.incrementGlobal();
                this.executeTick();
                this.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), this.mc.player.getYaw(), this.mc.player.getPitch(), this.mc.player.isOnGround()));
                PacketManager.INSTANCE.incrementGlobal();
            } else {
                MeteorClient.ROTATION.silentRotate(rotTarget, this::executeTick);
            }
        } else {
            this.executeTick();
        }
    }

    @EventHandler
    private void onPostMovement(SendMovementPacketsEvent.Post event) {
        if (this.fastPlace.get().booleanValue() && this.awaitingPlaceConfirmation && System.currentTimeMillis() - this.awaitingPlaceTimeMS > 100L) {
            this.awaitingPlaceConfirmation = false;
            this.awaitingPlacePos = null;
        }
        this.placedThisCycle = false;
    }

    private Vec3d findRotationTarget() {
        if (!(!this.breakCrystals.get().booleanValue() || this.pauseEatBreak.get().booleanValue() && this.mc.player.isUsingItem())) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity) || this.explodedCrystals.contains(entity.getId()) || !this.inBreakRange(entity.getPos()) || !this.shouldBreakCrystal(entity)) continue;
                return entity.getPos();
            }
        }
        if (!(!this.placeCrystals.get().booleanValue() || this.pauseEatPlace.get().booleanValue() && this.mc.player.isUsingItem())) {
            this.cachedValidPlaceSpots();
            PlacePosition best = this.findBestPlaceAcrossPlayers();
            if (best != null) {
                return best.blockPos.down().toCenterPos();
            }
        }
        return null;
    }

    private void executeTick() {
        if (this.mc.player == null || this.mc.world == null || this.mc.world.getPlayers().isEmpty()) {
            return;
        }
        if (this.pauseWhileFlying.get().booleanValue() && this.mc.player.isFallFlying()) {
            return;
        }
        boolean eating = this.mc.player.isUsingItem();
        for (PlacePosition p : this._placePositions) {
            this.placePositionPool.free(p);
        }
        this._placePositions.clear();
        if (!(!this.breakCrystals.get().booleanValue() || this.packetBreakMode.get() == PacketBreakMode.Render || this.pauseEatBreak.get().booleanValue() && eating)) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity) || this.explodedCrystals.contains(entity.getId()) || !this.inBreakRange(entity.getPos()) || !this.shouldBreakCrystal(entity)) continue;
                if (!this.breakSpeedCheck()) break;
                BlockPos crystalBase = entity.getBlockPos().down();
                if (crystalBase.equals((Object)this.lastPacketBreakPos)) {
                    this.lastPacketBreakPos = null;
                    continue;
                }
                this.doBreakCrystal(entity);
                break;
            }
            this.lastPacketBreakPos = null;
        }
        if (!(!this.placeCrystals.get().booleanValue() || this.pauseEatPlace.get().booleanValue() && eating)) {
            boolean shouldBasePlace;
            PlacePosition bestPlacePos = null;
            if (this.isManual()) {
                bestPlacePos = this.getManualPlacePos();
            } else {
                this.cachedValidPlaceSpots();
                bestPlacePos = this.findBestPlaceAcrossPlayers();
            }
            double currentPlaceDamage = bestPlacePos != null ? bestPlacePos.damage : 0.0;
            BlockPos bestBasePos = null;
            double bestBaseDamage = 0.0;
            if (this.basePlaceEnabled.get().booleanValue() && (bestBasePos = this.findBestBasePlacePosition(bestPlacePos)) != null) {
                bestBaseDamage = this.lastBasePlaceDamage;
            }
            boolean bl = shouldBasePlace = bestBasePos != null && (bestPlacePos == null || bestBaseDamage >= currentPlaceDamage + 10.0);
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
            } else if (bestPlacePos != null && this.placeSpeedCheck(bestPlacePos.isSlowPlace)) {
                this.pendingBasePlacePos = null;
                this.doPlaceCrystal(bestPlacePos.blockPos.down(), bestPlacePos.damage);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult invCrystals;
        this.crystalBreakDelays.values().removeIf(time -> System.currentTimeMillis() - time > 1000L);
        long cleanupTime = System.currentTimeMillis();
        this.explodedCrystals.removeIf(id -> {
            if (this.mc.world == null || this.mc.world.getEntityById(id) == null) {
                return true;
            }
            Long breakTime = this.crystalBreakDelays.get(id);
            return breakTime != null && cleanupTime - breakTime > 200L;
        });
        if (!this.isActive() || this.mc.player == null || this.mc.world == null) {
            return;
        }
        FindItemResult hotbarCrystals = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!hotbarCrystals.found() && (invCrystals = InvUtils.find(Items.END_CRYSTAL)).found()) {
            InvUtils.move().from(invCrystals.slot()).toHotbar(this.crystalSlot.get() - 1);
        }
        if (this.mc.currentScreen == null && this.facePlaceKeybind.get().isPressed()) {
            if (!this.facePlaceKeyDown) {
                this.currentFacePlaceMode = this.currentFacePlaceMode == FacePlaceMode.Off ? FacePlaceMode.Fast : FacePlaceMode.Off;
                this.sendChatFeedback(this.currentFacePlaceMode == FacePlaceMode.Fast ? "started" : "stopped");
                this.facePlaceKeyDown = true;
            }
        } else {
            this.facePlaceKeyDown = false;
        }
        if (this.mc.player != null && this.debugMessage.get().booleanValue()) {
            long cps = this.getCPS();
            long avgLifetime = this.avg(this.crystalLifetimes);
            long avgPlaceToSpawn = this.avg(this.placeToSpawnDelays);
            long avgSpawnToBreak = this.avg(this.spawnToBreakDelays);
            long avgBreakToPlace = this.avg(this.breakToPlaceDelays);
            this.mc.player.sendMessage((Text)Text.literal((String)String.format("\u00a76[AC] \u00a7f%d CPS \u00a77| \u00a7flife:%dms \u00a77| \u00a7fp>s:%dms \u00a77| \u00a7fs>b:%dms \u00a77| \u00a7fb>p:%dms", cps, avgLifetime, avgPlaceToSpawn, avgSpawnToBreak, avgBreakToPlace)), true);
        }
    }

    private long avg(List<Long> list) {
        if (list.isEmpty()) {
            return 0L;
        }
        long sum = 0L;
        for (long v : list) {
            sum += v;
        }
        return sum / (long)list.size();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mc.player != null && this.pauseWhileFlying.get().booleanValue() && this.mc.player.isFallFlying()) {
            this.renderer.onRender3D(event);
            return;
        }
        if (this.isActive() && this.mc.player != null && this.mc.world != null && this.packetBreakMode.get() == PacketBreakMode.Render && this.breakCrystals.get().booleanValue() && (!this.pauseEatBreak.get().booleanValue() || !this.mc.player.isUsingItem())) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity) || this.explodedCrystals.contains(entity.getId()) || !this.inBreakRange(entity.getPos()) || !this.shouldBreakCrystal(entity)) continue;
                if (!this.breakSpeedCheck()) break;
                this.doBreakCrystal(entity);
                break;
            }
        }
        if (this.fastPlace.get().booleanValue() && this.isActive() && this.mc.player != null && this.mc.world != null && this.placeCrystals.get().booleanValue() && (!this.pauseEatPlace.get().booleanValue() || !this.mc.player.isUsingItem())) {
            BlockPos crystalPos;
            BlockState state;
            if (this.awaitingPlaceConfirmation && this.awaitingPlacePos != null && !(state = this.mc.world.getBlockState(crystalPos = this.awaitingPlacePos.up())).isAir()) {
                this.awaitingPlaceConfirmation = false;
                this.awaitingPlacePos = null;
            }
            if (!this.awaitingPlaceConfirmation && !this.placedThisCycle) {
                BlockPos basePos;
                this.cachedValidPlaceSpots();
                PlacePosition best = null;
                best = this.isManual() ? this.getManualPlacePos() : this.findBestPlaceAcrossPlayers();
                if (best != null && this.placeSpeedCheck(best.isSlowPlace) && this.doRenderPlace(basePos = best.blockPos.down(), best.damage)) {
                    this.awaitingPlaceConfirmation = true;
                    this.awaitingPlacePos = basePos.toImmutable();
                    this.awaitingPlaceTimeMS = System.currentTimeMillis();
                    this.placedThisCycle = true;
                }
            }
        }
        this.renderer.onRender3D(event);
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        this.renderer.onRender2D(event);
    }

    private boolean doRenderPlace(BlockPos blockPos, double damage) {
        Hand hand;
        if (blockPos == null || this.mc.player == null || this.mc.world == null) {
            return false;
        }
        BlockPos crystalPos = blockPos.up();
        Box box = new Box((double)crystalPos.getX(), (double)crystalPos.getY(), (double)crystalPos.getZ(), (double)(crystalPos.getX() + 1), (double)(crystalPos.getY() + 2), (double)(crystalPos.getZ() + 1));
        if (this.intersectsWithEntities(box)) {
            return false;
        }
        FindItemResult result = InvUtils.find(Items.END_CRYSTAL);
        if (!result.found()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (this.crystalPlaceDelays.containsKey(blockPos) && (double)(currentTime - this.crystalPlaceDelays.get(blockPos)) / 1000.0 < this.placeDelay.get()) {
            return false;
        }
        boolean isGrim = this.grimPlace.get();
        Hand hand2 = hand = isGrim ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!MeteorClient.SWAP.beginSwap(result, true)) {
            return false;
        }
        if (isGrim) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
        }
        this.crystalPlaceDelays.put(blockPos.toImmutable(), currentTime);
        this.renderer.onPlaceCrystal(blockPos, damage);
        BlockHitResult hitResult = this.getPlaceHitResult(blockPos);
        int s = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractBlockC2SPacket(hand, hitResult, s));
        PacketManager.INSTANCE.incrementPlace();
        if (isGrim) {
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
        long now = System.currentTimeMillis();
        this.placeSentTimestamps.put(blockPos.toImmutable(), now);
        if (this.lastBreakSentTimeMS > 0L) {
            long gap = now - this.lastBreakSentTimeMS;
            this.breakToPlaceDelays.add(gap);
            if (this.breakToPlaceDelays.size() > 50) {
                this.breakToPlaceDelays.remove(0);
            }
        }
        if (this.debugMessage.get().booleanValue() && this.mc.player != null) {
            ++this.debugCrystalCount;
            long b2p = this.lastBreakSentTimeMS > 0L ? now - this.lastBreakSentTimeMS : -1L;
            this.mc.player.sendMessage((Text)Text.literal((String)String.format("\u00a76[AC] \u00a77#%d \u00a7dRND-PLACE \u00a7fb>p:%dms \u00a77pos:%s", this.debugCrystalCount, b2p, blockPos.toShortString())), false);
        }
        this.lastPlaceTimeMS = now;
        return true;
    }

    private boolean doPlaceCrystal(BlockPos blockPos, double damage) {
        if (blockPos == null || this.mc.player == null) {
            return false;
        }
        BlockPos crystalPos = blockPos.up();
        Box box = new Box((double)crystalPos.getX(), (double)crystalPos.getY(), (double)crystalPos.getZ(), (double)(crystalPos.getX() + 1), (double)(crystalPos.getY() + 2), (double)(crystalPos.getZ() + 1));
        if (this.intersectsWithEntities(box)) {
            return false;
        }
        FindItemResult result = InvUtils.find(Items.END_CRYSTAL);
        if (!result.found()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (this.crystalPlaceDelays.containsKey(blockPos) && (double)(currentTime - this.crystalPlaceDelays.get(blockPos)) / 1000.0 < this.placeDelay.get()) {
            return false;
        }
        if (!MeteorClient.SWAP.beginSwap(result, true)) {
            return false;
        }
        PacketManager.INSTANCE.incrementGlobal();
        this.crystalPlaceDelays.put(blockPos.toImmutable(), currentTime);
        this.renderer.onPlaceCrystal(blockPos, damage);
        BlockHitResult hitResult = this.getPlaceHitResult(blockPos);
        Hand hand = Hand.MAIN_HAND;
        int s = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractBlockC2SPacket(hand, hitResult, s));
        PacketManager.INSTANCE.incrementPlace();
        if (this.placeSwingMode.get() == SwingMode.Client) {
            this.mc.player.swingHand(hand);
        }
        if (this.placeSwingMode.get() == SwingMode.Packet) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(hand));
            PacketManager.INSTANCE.incrementInteract();
        }
        MeteorClient.SWAP.endSwap(true);
        PacketManager.INSTANCE.incrementGlobal();
        long now = System.currentTimeMillis();
        this.placeSentTimestamps.put(blockPos.toImmutable(), now);
        if (this.lastBreakSentTimeMS > 0L) {
            long gap = now - this.lastBreakSentTimeMS;
            this.breakToPlaceDelays.add(gap);
            if (this.breakToPlaceDelays.size() > 50) {
                this.breakToPlaceDelays.remove(0);
            }
        }
        if (this.debugMessage.get().booleanValue() && this.mc.player != null) {
            ++this.debugCrystalCount;
            long b2p = this.lastBreakSentTimeMS > 0L ? now - this.lastBreakSentTimeMS : -1L;
            this.mc.player.sendMessage((Text)Text.literal((String)String.format("\u00a76[AC] \u00a77#%d \u00a7bPLACE \u00a7fb>p:%dms \u00a77pos:%s", this.debugCrystalCount, b2p, blockPos.toShortString())), false);
        }
        this.lastPlaceTimeMS = now;
        return true;
    }

    private void doBasePlace(BlockPos pos) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
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
            this.pendingBasePlaceTimeMS = System.currentTimeMillis();
        }
    }

    private boolean doBreakCrystal(Entity entity) {
        if (this.mc.player == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (this.crystalBreakDelays.containsKey(entity.getId()) && (double)(currentTime - this.crystalBreakDelays.get(entity.getId())) / 1000.0 < this.breakDelay.get()) {
            return false;
        }
        if (entity instanceof EndCrystalEntity) {
            long aliveMs = (long)entity.age * 50L;
            this.spawnToBreakDelays.add(aliveMs);
            if (this.spawnToBreakDelays.size() > 50) {
                this.spawnToBreakDelays.remove(0);
            }
            this.crystalLifetimes.add(aliveMs);
            if (this.crystalLifetimes.size() > 50) {
                this.crystalLifetimes.remove(0);
            }
        }
        this.crystalBreakDelays.put(entity.getId(), currentTime);
        this.renderer.onBreakCrystal(entity);
        this.mc.getNetworkHandler().sendPacket((Packet)PlayerInteractEntityC2SPacket.attack((Entity)entity, (boolean)this.mc.player.isSneaking()));
        PacketManager.INSTANCE.incrementInteract();
        if (this.breakSwingMode.get() == SwingMode.Client) {
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }
        if (this.breakSwingMode.get() == SwingMode.Packet) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
            PacketManager.INSTANCE.incrementInteract();
        }
        if (this.debugMessage.get().booleanValue() && this.mc.player != null) {
            BlockPos base = entity.getBlockPos().down();
            Long spawnTime = this.placeToBreakTimestamps.remove(base);
            Long placeTime = this.placeSentTimestamps.get(base);
            ++this.debugCrystalCount;
            long s2b = spawnTime != null ? currentTime - spawnTime : -1L;
            long p2b = placeTime != null ? currentTime - placeTime : -1L;
            this.mc.player.sendMessage((Text)Text.literal((String)String.format("\u00a76[AC] \u00a77#%d \u00a7eTICK-BREAK \u00a7fp>b:%dms \u00a7fs>b:%dms \u00a77age:%dt \u00a77pos:%s", this.debugCrystalCount, p2b, s2b, entity.age, base.toShortString())), false);
        }
        this.explodedCrystals.add(entity.getId());
        if (this.pendingPacketBreakId != null && this.pendingPacketBreakId.intValue() == entity.getId()) {
            this.pendingPacketBreakId = null;
            this.pendingPacketBreakPos = null;
        }
        this.lastBreakTimeMS = System.currentTimeMillis();
        this.lastBreakSentTimeMS = System.currentTimeMillis();
        return true;
    }

    @EventHandler(priority=200)
    private void onEntity(EntityAddedEvent event) {
        if (!(event.entity instanceof EndCrystalEntity)) {
            return;
        }
        BlockPos blockPos = event.entity.getBlockPos().down();
        Long placedAt = this.placeSentTimestamps.remove(blockPos);
        if (placedAt != null) {
            long delay = System.currentTimeMillis() - placedAt;
            this.placeToSpawnDelays.add(delay);
            if (this.placeToSpawnDelays.size() > 50) {
                this.placeToSpawnDelays.remove(0);
            }
            this.placeToBreakTimestamps.put(blockPos.toImmutable(), System.currentTimeMillis());
        }
        this.crystalPlaceDelays.remove(blockPos);
        if (this.awaitingPlaceConfirmation && this.awaitingPlacePos != null && this.awaitingPlacePos.equals((Object)blockPos)) {
            this.awaitingPlaceConfirmation = false;
            this.awaitingPlacePos = null;
            this.placedThisCycle = false;
        }
    }

    @EventHandler(priority=200)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (this.packetBreakMode.get() != PacketBreakMode.Id || !this.breakCrystals.get().booleanValue()) {
            return;
        }
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.pauseWhileFlying.get().booleanValue() && this.mc.player.isFallFlying()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket packet2 = (EntitySpawnS2CPacket)packet;
            if (packet2.getEntityType() != EntityType.END_CRYSTAL) {
                return;
            }
            Vec3d crystalPos = new Vec3d(packet2.getX(), packet2.getY(), packet2.getZ());
            if (!this.inBreakRange(crystalPos)) {
                return;
            }
            if (!this.shouldBreakCrystalAtPos(crystalPos)) {
                return;
            }
            int entityId = packet2.getEntityId();
            this.doBreakCrystalById(entityId, crystalPos);
        }
    }

    private boolean doBreakCrystalById(int entityId, Vec3d crystalPos) {
        if (this.mc.player == null) {
            return false;
        }
        BlockPos crystalBase = BlockPos.ofFloored((Position)crystalPos).down();
        long currentTime = System.currentTimeMillis();
        if (crystalBase.equals((Object)this.lastPacketBreakPos) && (double)(currentTime - this.lastPacketBreakTimeMS) / 1000.0 < this.breakDelay.get()) {
            return false;
        }
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
        BlockPos base = BlockPos.ofFloored((Position)crystalPos).down();
        if (this.debugMessage.get().booleanValue() && this.mc.player != null) {
            Long spawnTime = this.placeToBreakTimestamps.remove(base);
            Long placeTime = this.placeSentTimestamps.get(base);
            ++this.debugCrystalCount;
            long now = System.currentTimeMillis();
            long s2b = spawnTime != null ? now - spawnTime : -1L;
            long p2b = placeTime != null ? now - placeTime : -1L;
            this.mc.player.sendMessage((Text)Text.literal((String)String.format("\u00a76[AC] \u00a77#%d \u00a7aPKT-BREAK \u00a7fp>b:%dms \u00a7fs>b:%dms \u00a77pos:%s", this.debugCrystalCount, p2b, s2b, base.toShortString())), false);
        }
        this.explodedCrystals.add(entityId);
        this.lastBreakTimeMS = System.currentTimeMillis();
        this.lastBreakSentTimeMS = System.currentTimeMillis();
        this.lastPacketBreakPos = base;
        this.lastPacketBreakTimeMS = System.currentTimeMillis();
        this.crystalBreakDelays.put(entityId, System.currentTimeMillis());
        this.spawnToBreakDelays.add(0L);
        if (this.spawnToBreakDelays.size() > 50) {
            this.spawnToBreakDelays.remove(0);
        }
        this.crystalLifetimes.add(0L);
        if (this.crystalLifetimes.size() > 50) {
            this.crystalLifetimes.remove(0);
        }
        this.pendingPacketBreakId = entityId;
        this.pendingPacketBreakPos = crystalPos;
        this.pendingPacketBreakBasePos = base;
        this.pendingPacketBreakTimeMS = System.currentTimeMillis();
        return true;
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

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent.Death event) {
        if (event.getPlayer() == null || event.getPlayer() == this.mc.player) {
            return;
        }
        if (this.setPlayerDead.get().booleanValue()) {
            event.getPlayer().setHealth(0.0f);
        }
    }

    private boolean isManual() {
        if (!this.manualOverride.get().booleanValue() || this.mc.currentScreen != null || !this.mc.options.useKey.isPressed()) {
            return false;
        }
        ItemStack main = this.mc.player.getMainHandStack();
        ItemStack off = this.mc.player.getOffHandStack();
        return main.getItem() == Items.END_CRYSTAL || off.getItem() == Items.END_CRYSTAL;
    }

    private PlacePosition getManualPlacePos() {
        BlockHitResult bhr;
        HitResult hitResult = this.mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult) || (bhr = (BlockHitResult)hitResult).getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos obsPos = bhr.getBlockPos();
        BlockPos placePos = obsPos.up();
        if (!this.inPlaceRange(placePos)) {
            return null;
        }
        if (!this.mc.world.isAir(placePos)) {
            return null;
        }
        BlockState state = this.mc.world.getBlockState(obsPos);
        if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) {
            return null;
        }
        Box box = new Box((double)placePos.getX(), (double)placePos.getY(), (double)placePos.getZ(), (double)(placePos.getX() + 1), (double)(placePos.getY() + 2), (double)(placePos.getZ() + 1));
        if (this.intersectsWithEntities(box)) {
            return null;
        }
        PlacePosition p = this.placePositionPool.get();
        p.blockPos = placePos;
        p.isSlowPlace = false;
        p.damage = 0.0;
        this._placePositions.add(p);
        return p;
    }

    private PlacePosition findBestPlaceAcrossPlayers() {
        PlacePosition best = null;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            PlacePosition pos;
            if (player == this.mc.player || Friends.get().isFriend(player) || player.isDead() || this.ignoreNakeds.get().booleanValue() && this.isNaked(player) || player.squaredDistanceTo(this.mc.player.getEyePos()) > 144.0 || (pos = this.findBestPlacePosition(player)) == null || best != null && !(pos.damage > best.damage)) continue;
            best = pos;
        }
        return best;
    }

    private PlacePosition findBestPlacePosition(PlayerEntity target) {
        boolean shouldFacePlace;
        if (this.cachedValidSpots.isEmpty()) {
            this.cachedValidPlaceSpots();
            if (this.cachedValidSpots.isEmpty()) {
                return null;
            }
        }
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
        boolean bl = shouldFacePlace = this.currentFacePlaceMode != FacePlaceMode.Off;
        if (this.facePlaceMissingArmor.get().booleanValue() && this.hasEmptyArmor(target)) {
            shouldFacePlace = true;
        }
        Box predictedBox = target.getBoundingBox();
        int dim = 2 * r + 1;
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    PlacePosition best;
                    boolean isDirty;
                    BlockPos.Mutable pos;
                    Box crystalBox;
                    int idx = (x + r) * dim * dim + (y + r) * dim + (z + r);
                    if (idx < 0 || idx >= this.cachedValidSpots.size() || !this.cachedValidSpots.get(idx).booleanValue() || predictedBox.intersects(crystalBox = new Box((double)(pos = this.mutablePos.set(ex + x, ey + y, ez + z)).getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1)))) continue;
                    boolean isClean = !this.intersectsWithEntities(crystalBox, false);
                    boolean bl2 = isDirty = !isClean && this.ignoreItem.get() != false && !this.intersectsWithEntities(crystalBox, true);
                    if (!isClean && !isDirty) continue;
                    double targetDamage = DamageUtils.newCrystalDamage(target, predictedBox, new Vec3d((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5), this._calcIgnoreSet);
                    if (targetDamage < (shouldFacePlace ? 1.0 : this.minPlace.get())) continue;
                    boolean isSlow = this.slowPlace.get() != false && targetDamage >= this.slowPlaceMinDamage.get() && targetDamage <= this.slowPlaceMaxDamage.get();
                    PlacePosition placePosition = best = isClean ? bestClean : bestDirty;
                    if (!(targetDamage > best.damage)) continue;
                    best.blockPos = pos.toImmutable();
                    best.damage = targetDamage;
                    best.isSlowPlace = isSlow;
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
                    BlockPos crystalPos = new BlockPos(ex + x, ey + y, ez + z);
                    boolean crystalSpaceOpen = this.mc.world.isAir(crystalPos) || this._calcIgnoreSet.contains(crystalPos);
                    BlockPos basePos = crystalPos.down();
                    BlockState baseState = this.mc.world.getBlockState(basePos);
                    if (baseState.getBlock() == Blocks.OBSIDIAN || baseState.getBlock() == Blocks.BEDROCK || !baseState.isAir() && !baseState.isReplaceable() || !this.inPlaceRange(basePos)) continue;
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

    private void cachedValidPlaceSpots() {
        int r = (int)Math.floor(this.placeRange.get());
        int dim = 2 * r + 1;
        int size = dim * dim * dim;
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        Box box = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.updateCalcIgnoreSet();
        this.cachedValidSpots.clear();
        for (int i = 0; i < size; ++i) {
            this.cachedValidSpots.add(false);
        }
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    int idx;
                    double selfDamage;
                    if (!this.mc.world.isAir((BlockPos)this.mutablePos.set(ex + x, ey + y, ez + z))) continue;
                    BlockPos.Mutable downPos = this.mutablePos.set(ex + x, ey + y - 1, ez + z);
                    BlockState downState = this.mc.world.getBlockState((BlockPos)downPos);
                    Block downBlock = downState.getBlock();
                    if (downState.isAir() || downBlock != Blocks.OBSIDIAN && downBlock != Blocks.BEDROCK || !this.inPlaceRange((BlockPos)downPos)) continue;
                    ((IBox)box).set(downPos.getX(), downPos.getY() + 1, downPos.getZ(), downPos.getX() + 1, downPos.getY() + 3, downPos.getZ() + 1);
                    if (this.intersectsWithEntities(box) || (selfDamage = DamageUtils.newCrystalDamage((PlayerEntity)this.mc.player, this.mc.player.getBoundingBox(), new Vec3d((double)downPos.getX() + 0.5, (double)(downPos.getY() + 1), (double)downPos.getZ() + 0.5), this._calcIgnoreSet)) > this.maxPlace.get() || (idx = (x + r) * dim * dim + (y + r) * dim + (z + r)) < 0 || idx >= this.cachedValidSpots.size()) continue;
                    this.cachedValidSpots.set(idx, true);
                }
            }
        }
    }

    public boolean shouldBreakCrystal(Entity entity) {
        double selfDamage;
        SilentMine silentMine;
        if (this.isManual()) {
            return this.inBreakRange(entity.getPos());
        }
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
            return true;
        }
        return false;
    }

    private BlockHitResult getPlaceHitResult(BlockPos blockPos) {
        Vec3d eyePos = this.mc.player.getEyePos();
        Direction bestDir = Direction.UP;
        double bestDist = Double.MAX_VALUE;
        double[] xzOffsets = new double[]{0.05, 0.95};
        double[] yOffsets = new double[]{0.05, 0.95, 0.99};
        for (double dx : xzOffsets) {
            for (double dy : yOffsets) {
                for (double dz : xzOffsets) {
                    double dist;
                    BlockHitResult hit;
                    Vec3d point = new Vec3d((double)blockPos.getX() + dx, (double)blockPos.getY() + dy, (double)blockPos.getZ() + dz);
                    if (point.distanceTo(eyePos) > this.placeRange.get() || (hit = this.mc.world.raycast(new RaycastContext(eyePos, point, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)this.mc.player))).getType() == HitResult.Type.MISS || !hit.getBlockPos().equals((Object)blockPos) || !((dist = hit.getPos().distanceTo(eyePos)) < bestDist)) continue;
                    bestDist = dist;
                    bestDir = hit.getSide();
                }
            }
        }
        Vec3d hitPos = blockPos.toCenterPos().offset(bestDir, 0.5);
        return new BlockHitResult(hitPos, bestDir, blockPos, false);
    }

    private void updateCalcIgnoreSet() {
        BlockPos rebreak;
        this._calcIgnoreSet.clear();
        if (!this.antiSurroundPlace.get().booleanValue()) {
            return;
        }
        SilentMine silentMine = Modules.get().get(SilentMine.class);
        if (silentMine == null || !silentMine.isActive()) {
            return;
        }
        double required = this.silentMineProgressThreshold.get() / 100.0;
        BlockPos delayed = silentMine.getDelayedDestroyBlockPos();
        if (delayed != null && silentMine.getDelayedDestroyProgress() >= required) {
            this._calcIgnoreSet.add(delayed);
        }
        if ((rebreak = silentMine.getRebreakBlockPos()) != null && silentMine.canRebreakRebreakBlock()) {
            this._calcIgnoreSet.add(rebreak);
        }
    }

    private void addExplodableBlocks(BlockPos center, Set<BlockPos> set) {
        if (center == null || this.mc.world == null) {
            return;
        }
        BlockPos.Mutable mut = new BlockPos.Mutable();
        for (int x = -8; x <= 8; ++x) {
            for (int y = -8; y <= 8; ++y) {
                for (int z = -8; z <= 8; ++z) {
                    mut.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = this.mc.world.getBlockState((BlockPos)mut);
                    if (state.isAir() || !(state.getBlock().getBlastResistance() < 600.0f)) continue;
                    set.add(mut.toImmutable());
                }
            }
        }
    }

    public boolean inPlaceRange(BlockPos blockPos) {
        return blockPos.toCenterPos().distanceTo(this.mc.player.getEyePos()) <= this.placeRange.get();
    }

    public boolean inBreakRange(Vec3d pos) {
        return pos.distanceTo(this.mc.player.getEyePos()) <= this.breakRange.get();
    }

    private boolean intersectsWithEntities(Box box) {
        return this.intersectsWithEntities(box, this.ignoreItem.get());
    }

    private boolean intersectsWithEntities(Box box, boolean ignoreItems) {
        return EntityUtils.intersectsWithEntity(box, entity -> {
            if (entity.isRemoved() || this.explodedCrystals.contains(entity.getId())) {
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

    private boolean isNaked(PlayerEntity player) {
        return ((ItemStack)player.getInventory().armor.get(0)).isEmpty() && ((ItemStack)player.getInventory().armor.get(1)).isEmpty() && ((ItemStack)player.getInventory().armor.get(2)).isEmpty() && ((ItemStack)player.getInventory().armor.get(3)).isEmpty();
    }

    private boolean hasEmptyArmor(PlayerEntity player) {
        return ((ItemStack)player.getInventory().armor.get(0)).isEmpty() || ((ItemStack)player.getInventory().armor.get(1)).isEmpty() || ((ItemStack)player.getInventory().armor.get(2)).isEmpty() || ((ItemStack)player.getInventory().armor.get(3)).isEmpty();
    }

    private boolean breakSpeedCheck() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - this.lastBreakTimeMS;
        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.MEDIUM)) {
            return (double)elapsed / 1000.0 > 0.09090909090909091;
        }
        return this.breakSpeedLimit.get() == 0.0 || (double)elapsed / 1000.0 > 1.0 / this.breakSpeedLimit.get();
    }

    private boolean placeSpeedCheck(boolean slow) {
        long currentTime = System.currentTimeMillis();
        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.MEDIUM)) {
            return (double)(currentTime - this.lastPlaceTimeMS) / 1000.0 > 0.09090909090909091;
        }
        double speed = slow ? this.slowPlaceSpeed.get() : this.placeSpeedLimit.get();
        return speed == 0.0 || (double)(currentTime - this.lastPlaceTimeMS) / 1000.0 > 1.0 / speed;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(this.getCPS());
    }

    public long getCPS() {
        long currentTime = System.currentTimeMillis();
        long count = 0L;
        try {
            for (Long time : this.crystalBreakDelays.values().toArray(new Long[0])) {
                if (currentTime - time > 600L) continue;
                ++count;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Math.round((double)count * 1.666666);
    }

    private void sendChatFeedback(String state) {
        MutableText body;
        if (this.mc.player == null) {
            return;
        }
        MutableText prefix = Text.literal((String)"[NewAutoCrystal] ").formatted(Formatting.GOLD);
        switch (state) {
            case "started": {
                MutableText mutableText = Text.literal((String)"FacePlace enabled.").formatted(Formatting.GREEN);
                break;
            }
            case "stopped": {
                MutableText mutableText = Text.literal((String)"FacePlace disabled.").formatted(Formatting.RED);
                break;
            }
            default: {
                MutableText mutableText = body = null;
            }
        }
        if (body != null) {
            this.mc.player.sendMessage((Text)prefix.append((Text)body), true);
        }
    }

    public static enum PacketBreakMode {
        Off,
        Render,
        Id;

    }

    public static enum SwingMode {
        Packet,
        Client,
        None;

    }

    public static enum FacePlaceMode {
        Fast,
        Off;

    }

    private class PlacePosition {
        public BlockPos blockPos;
        public double damage = 0.0;
        public boolean isSlowPlace = false;

        private PlacePosition(NewAutoCrystal newAutoCrystal) {
        }
    }
}

