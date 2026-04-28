/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.input.Input
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.EmptyBlockView
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.misc.MBlockPos;
import meteordevelopment.meteorclient.utils.player.CustomPlayerInput;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.input.Input;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import org.jetbrains.annotations.NotNull;

public class HighwayBuilder
extends Module {
    private static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDigging;
    private final SettingGroup sgPaving;
    private final SettingGroup sgInventory;
    private final SettingGroup sgRenderDigging;
    private final SettingGroup sgRenderPaving;
    private final Setting<Integer> width;
    private final Setting<Integer> height;
    private final Setting<Floor> floor;
    private final Setting<Boolean> railings;
    private final Setting<Boolean> mineAboveRailings;
    private final Setting<Rotation> rotation;
    private final Setting<Boolean> disconnectOnToggle;
    private final Setting<Boolean> pauseOnLag;
    private final Setting<Boolean> dontBreakTools;
    private final Setting<Integer> savePickaxes;
    private final Setting<Integer> breakDelay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<List<Block>> blocksToPlace;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> placementsPerTick;
    private final Setting<List<Item>> trashItems;
    private final Setting<Boolean> mineEnderChests;
    private final Setting<Integer> saveEchests;
    private final Setting<Boolean> rebreakEchests;
    private final Setting<Integer> rebreakTimer;
    private final Setting<Boolean> renderMine;
    private final Setting<ShapeMode> renderMineShape;
    private final Setting<SettingColor> renderMineSideColor;
    private final Setting<SettingColor> renderMineLineColor;
    private final Setting<Boolean> renderPlace;
    private final Setting<ShapeMode> renderPlaceShape;
    private final Setting<SettingColor> renderPlaceSideColor;
    private final Setting<SettingColor> renderPlaceLineColor;
    private HorizontalDirection dir;
    private HorizontalDirection leftDir;
    private HorizontalDirection rightDir;
    private Input prevInput;
    private CustomPlayerInput input;
    private State state;
    private State lastState;
    private IBlockPosProvider blockPosProvider;
    public Vec3d start;
    public int blocksBroken;
    public int blocksPlaced;
    private final MBlockPos lastBreakingPos;
    private boolean displayInfo;
    private int placeTimer;
    private int breakTimer;
    private int count;
    private final MBlockPos posRender2;
    private final MBlockPos posRender3;

    public HighwayBuilder() {
        super(Categories.World, "highway-builder", "Automatically builds highways.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDigging = this.settings.createGroup("Digging");
        this.sgPaving = this.settings.createGroup("Paving");
        this.sgInventory = this.settings.createGroup("Inventory");
        this.sgRenderDigging = this.settings.createGroup("Render Digging");
        this.sgRenderPaving = this.settings.createGroup("Render Paving");
        this.width = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("width")).description("Width of the highway.")).defaultValue(4)).range(1, 5).sliderRange(1, 5).build());
        this.height = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("height")).description("Height of the highway.")).defaultValue(3)).range(2, 5).sliderRange(2, 5).build());
        this.floor = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("floor")).description("What floor placement mode to use.")).defaultValue(Floor.Replace)).build());
        this.railings = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("railings")).description("Builds railings next to the highway.")).defaultValue(true)).build());
        this.mineAboveRailings = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mine-above-railings")).description("Mines blocks above railings.")).visible(this.railings::get)).defaultValue(true)).build());
        this.rotation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("rotation")).description("Mode of rotation.")).defaultValue(Rotation.Both)).build());
        this.disconnectOnToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disconnect-on-toggle")).description("Automatically disconnects when the module is turned off, for example for not having enough blocks.")).defaultValue(false)).build());
        this.pauseOnLag = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-lag")).description("Pauses the current process while the server stops responding.")).defaultValue(true)).build());
        this.dontBreakTools = this.sgDigging.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dont-break-tools")).description("Don't break tools.")).defaultValue(false)).build());
        this.savePickaxes = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("save-pickaxes")).description("How many pickaxes to ensure are saved.")).defaultValue(0)).range(0, 36).sliderRange(0, 36).visible(() -> this.dontBreakTools.get() == false)).build());
        this.breakDelay = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-delay")).description("The delay between breaking blocks.")).defaultValue(0)).min(0).build());
        this.blocksPerTick = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("The maximum amount of blocks that can be mined in a tick. Only applies to blocks instantly breakable.")).defaultValue(1)).range(1, 100).sliderRange(1, 25).build());
        this.blocksToPlace = this.sgPaving.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks-to-place")).description("Blocks it is allowed to place.")).defaultValue(Blocks.OBSIDIAN).filter(block -> Block.isShapeFullCube((VoxelShape)block.getDefaultState().getCollisionShape((BlockView)EmptyBlockView.INSTANCE, ZERO))).build());
        this.placeDelay = this.sgPaving.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The delay between placing blocks.")).defaultValue(0)).min(0).build());
        this.placementsPerTick = this.sgPaving.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("placements-per-tick")).description("The maximum amount of blocks that can be placed in a tick.")).defaultValue(1)).min(1).build());
        this.trashItems = this.sgInventory.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("trash-items")).description("Items that are considered trash and can be thrown out.")).defaultValue(Items.NETHERRACK, Items.QUARTZ, Items.GOLD_NUGGET, Items.GOLDEN_SWORD, Items.GLOWSTONE_DUST, Items.GLOWSTONE, Items.BLACKSTONE, Items.BASALT, Items.GHAST_TEAR, Items.SOUL_SAND, Items.SOUL_SOIL, Items.ROTTEN_FLESH).build());
        this.mineEnderChests = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mine-ender-chests")).description("Mines ender chests for obsidian.")).defaultValue(true)).build());
        this.saveEchests = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("save-ender-chests")).description("How many ender chests to ensure are saved.")).defaultValue(1)).range(0, 64).sliderRange(0, 64).visible(this.mineEnderChests::get)).build());
        this.rebreakEchests = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instantly-rebreak-echests")).description("Whether or not to use the instant rebreak exploit to break echests.")).defaultValue(false)).visible(this.mineEnderChests::get)).build());
        this.rebreakTimer = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rebreak-delay")).description("Delay between rebreak attempts.")).defaultValue(0)).sliderMax(20).visible(() -> this.mineEnderChests.get() != false && this.rebreakEchests.get() != false)).build());
        this.renderMine = this.sgRenderDigging.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-blocks-to-mine")).description("Render blocks to be mined.")).defaultValue(true)).build());
        this.renderMineShape = this.sgRenderDigging.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-to-mine-shape-mode")).description("How the blocks to be mined are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.renderMineSideColor = this.sgRenderDigging.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-mine-side-color")).description("Color of blocks to be mined.")).defaultValue(new SettingColor(225, 25, 25, 25)).build());
        this.renderMineLineColor = this.sgRenderDigging.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-mine-line-color")).description("Color of blocks to be mined.")).defaultValue(new SettingColor(225, 25, 25)).build());
        this.renderPlace = this.sgRenderPaving.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-blocks-to-place")).description("Render blocks to be placed.")).defaultValue(true)).build());
        this.renderPlaceShape = this.sgRenderPaving.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-to-place-shape-mode")).description("How the blocks to be placed are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.renderPlaceSideColor = this.sgRenderPaving.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-place-side-color")).description("Color of blocks to be placed.")).defaultValue(new SettingColor(25, 25, 225, 25)).build());
        this.renderPlaceLineColor = this.sgRenderPaving.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-place-line-color")).description("Color of blocks to be placed.")).defaultValue(new SettingColor(25, 25, 225)).build());
        this.lastBreakingPos = new MBlockPos();
        this.posRender2 = new MBlockPos();
        this.posRender3 = new MBlockPos();
    }

    @Override
    public void onActivate() {
        this.dir = HorizontalDirection.get(this.mc.player.getYaw());
        this.leftDir = this.dir.rotateLeftSkipOne();
        this.rightDir = this.leftDir.opposite();
        this.prevInput = this.mc.player.input;
        this.input = new CustomPlayerInput();
        this.mc.player.input = this.input;
        this.state = State.Forward;
        this.setState(State.Center);
        this.blockPosProvider = this.dir.diagonal ? new DiagonalBlockPosProvider() : new StraightBlockPosProvider();
        this.start = this.mc.player.getPos();
        this.blocksPlaced = 0;
        this.blocksBroken = 0;
        this.lastBreakingPos.set(0, 0, 0);
        this.displayInfo = true;
        this.placeTimer = 0;
        this.breakTimer = 0;
        this.count = 0;
        if (this.blocksPerTick.get() > 1 && this.rotation.get().mine) {
            this.warning("With rotations enabled, you can break at most 1 block per tick.", new Object[0]);
        }
        if (this.placementsPerTick.get() > 1 && this.rotation.get().place) {
            this.warning("With rotations enabled, you can place at most 1 block per tick.", new Object[0]);
        }
        if (Modules.get().get(InstantRebreak.class).isActive()) {
            this.warning("It's recommended to disable the Instant Rebreak module and instead use the 'instantly-rebreak-echests' setting to avoid errors.", new Object[0]);
        }
    }

    @Override
    public void onDeactivate() {
        this.mc.player.input = this.prevInput;
        this.mc.player.setYaw(this.dir.yaw);
        if (this.displayInfo) {
            this.info("Distance: (highlight)%.0f", PlayerUtils.distanceTo(this.start));
            this.info("Blocks broken: (highlight)%d", this.blocksBroken);
            this.info("Blocks placed: (highlight)%d", this.blocksPlaced);
        }
    }

    @Override
    public void error(String message, Object ... args) {
        super.error(message, args);
        this.toggle();
        if (this.disconnectOnToggle.get().booleanValue()) {
            this.disconnect(message, args);
        }
    }

    private void errorEarly(String message, Object ... args) {
        super.error(message, args);
        this.displayInfo = false;
        this.toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.width.get() < 3 && this.dir.diagonal) {
            this.errorEarly("Diagonal highways with width less than 3 are not supported.", new Object[0]);
            return;
        }
        if (Modules.get().get(AutoEat.class).eating) {
            return;
        }
        if (Modules.get().get(AutoGap.class).isEating()) {
            return;
        }
        if (Modules.get().get(KillAura.class).attacking) {
            return;
        }
        if (this.pauseOnLag.get().booleanValue() && TickRate.INSTANCE.getTimeSinceLastTick() >= 2.0f) {
            return;
        }
        this.count = 0;
        this.state.tick(this);
        if (this.breakTimer > 0) {
            --this.breakTimer;
        }
        if (this.placeTimer > 0) {
            --this.placeTimer;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.renderMine.get().booleanValue()) {
            this.render(event, this.blockPosProvider.getFront(), mBlockPos -> this.canMine((MBlockPos)mBlockPos, true), true);
            if (this.floor.get() == Floor.Replace) {
                this.render(event, this.blockPosProvider.getFloor(), mBlockPos -> this.canMine((MBlockPos)mBlockPos, false), true);
            }
            if (this.railings.get().booleanValue()) {
                this.render(event, this.blockPosProvider.getRailings(true), mBlockPos -> this.canMine((MBlockPos)mBlockPos, false), true);
            }
            if (this.state == State.MineEChestBlockade) {
                this.render(event, this.blockPosProvider.getEChestBlockade(true), mBlockPos -> this.canMine((MBlockPos)mBlockPos, true), true);
            }
        }
        if (this.renderPlace.get().booleanValue()) {
            this.render(event, this.blockPosProvider.getLiquids(), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, true), false);
            if (this.railings.get().booleanValue()) {
                this.render(event, this.blockPosProvider.getRailings(false), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, false), false);
            }
            this.render(event, this.blockPosProvider.getFloor(), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, false), false);
            if (this.state == State.PlaceEChestBlockade) {
                this.render(event, this.blockPosProvider.getEChestBlockade(false), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, false), false);
            }
        }
    }

    private void render(Render3DEvent event, MBPIterator it, Predicate<MBlockPos> predicate, boolean mine) {
        Color sideColor = mine ? (Color)this.renderMineSideColor.get() : (Color)this.renderPlaceSideColor.get();
        Color lineColor = mine ? (Color)this.renderMineLineColor.get() : (Color)this.renderPlaceLineColor.get();
        ShapeMode shapeMode = mine ? this.renderMineShape.get() : this.renderPlaceShape.get();
        for (MBlockPos pos : it) {
            this.posRender2.set(pos);
            if (!predicate.test(this.posRender2)) continue;
            int excludeDir = 0;
            for (Direction side : Direction.values()) {
                this.posRender3.set(this.posRender2).add(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ());
                it.save();
                for (MBlockPos p : it) {
                    if (!p.equals(this.posRender3) || !predicate.test(p)) continue;
                    excludeDir |= Dir.get(side);
                }
                it.restore();
            }
            event.renderer.box(this.posRender2.getBlockPos(), sideColor, lineColor, shapeMode, excludeDir);
        }
    }

    private void setState(State state) {
        this.lastState = this.state;
        this.state = state;
        this.input.stop();
        state.start(this);
    }

    private int getWidthLeft() {
        return switch (this.width.get()) {
            default -> 0;
            case 2, 3 -> 1;
            case 4, 5 -> 2;
        };
    }

    private int getWidthRight() {
        return switch (this.width.get()) {
            default -> 0;
            case 3, 4 -> 1;
            case 5 -> 2;
        };
    }

    private boolean canMine(MBlockPos pos, boolean ignoreBlocksToPlace) {
        BlockState state = pos.getState();
        return BlockUtils.canBreak(pos.getBlockPos(), state) && (ignoreBlocksToPlace || !this.blocksToPlace.get().contains(state.getBlock()));
    }

    private boolean canPlace(MBlockPos pos, boolean liquids) {
        return liquids ? !pos.getState().getFluidState().isEmpty() : BlockUtils.canPlace(pos.getBlockPos());
    }

    private void disconnect(String message, Object ... args) {
        MutableText text = Text.literal((String)(String.format("%s[%s%s%s] %s", Formatting.GRAY, Formatting.BLUE, this.title, Formatting.GRAY, Formatting.RED) + String.format(message, args))).append("\n");
        text.append((Text)this.getStatsText());
        this.mc.getNetworkHandler().getConnection().disconnect((Text)text);
    }

    public MutableText getStatsText() {
        MutableText text = Text.literal((String)String.format("%sDistance: %s%.0f\n", Formatting.GRAY, Formatting.WHITE, this.mc.player == null ? 0.0 : PlayerUtils.distanceTo(this.start)));
        text.append(String.format("%sBlocks broken: %s%d\n", Formatting.GRAY, Formatting.WHITE, this.blocksBroken));
        text.append(String.format("%sBlocks placed: %s%d", Formatting.GRAY, Formatting.WHITE, this.blocksPlaced));
        return text;
    }

    public static enum Floor {
        Replace,
        PlaceMissing;

    }

    public static enum Rotation {
        None(false, false),
        Mine(true, false),
        Place(false, true),
        Both(true, true);

        public final boolean mine;
        public final boolean place;

        private Rotation(boolean mine, boolean place) {
            this.mine = mine;
            this.place = place;
        }
    }

    private static enum State {
        Center{

            @Override
            protected void tick(HighwayBuilder b) {
                boolean isZ;
                double x = Math.abs(((HighwayBuilder)b).mc.player.getX() - (double)((int)((HighwayBuilder)b).mc.player.getX())) - 0.5;
                double z = Math.abs(((HighwayBuilder)b).mc.player.getZ() - (double)((int)((HighwayBuilder)b).mc.player.getZ())) - 0.5;
                boolean isX = Math.abs(x) <= 0.1;
                boolean bl = isZ = Math.abs(z) <= 0.1;
                if (isX && isZ) {
                    b.input.stop();
                    ((HighwayBuilder)b).mc.player.setVelocity(0.0, 0.0, 0.0);
                    ((HighwayBuilder)b).mc.player.setPosition((double)((int)((HighwayBuilder)b).mc.player.getX()) + (((HighwayBuilder)b).mc.player.getX() < 0.0 ? -0.5 : 0.5), ((HighwayBuilder)b).mc.player.getY(), (double)((int)((HighwayBuilder)b).mc.player.getZ()) + (((HighwayBuilder)b).mc.player.getZ() < 0.0 ? -0.5 : 0.5));
                    b.setState(b.lastState);
                } else {
                    ((HighwayBuilder)b).mc.player.setYaw(0.0f);
                    if (!isZ) {
                        b.input.pressingForward = z < 0.0;
                        boolean bl2 = b.input.pressingBack = z > 0.0;
                        if (((HighwayBuilder)b).mc.player.getZ() < 0.0) {
                            boolean forward = b.input.pressingForward;
                            b.input.pressingForward = b.input.pressingBack;
                            b.input.pressingBack = forward;
                        }
                    }
                    if (!isX) {
                        b.input.pressingRight = x > 0.0;
                        boolean bl3 = b.input.pressingLeft = x < 0.0;
                        if (((HighwayBuilder)b).mc.player.getX() < 0.0) {
                            boolean right = b.input.pressingRight;
                            b.input.pressingRight = b.input.pressingLeft;
                            b.input.pressingLeft = right;
                        }
                    }
                    b.input.sneaking = true;
                }
            }
        }
        ,
        Forward{

            @Override
            protected void start(HighwayBuilder b) {
                ((HighwayBuilder)b).mc.player.setYaw(b.dir.yaw);
                this.checkTasks(b);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.checkTasks(b);
                if (b.state == Forward) {
                    b.input.pressingForward = true;
                }
            }

            private void checkTasks(HighwayBuilder b) {
                if (this.needsToPlace(b, b.blockPosProvider.getLiquids(), true)) {
                    b.setState(FillLiquids);
                } else if (this.needsToMine(b, b.blockPosProvider.getFront(), true)) {
                    b.setState(MineFront);
                } else if (b.floor.get() == Floor.Replace && this.needsToMine(b, b.blockPosProvider.getFloor(), false)) {
                    b.setState(MineFloor);
                } else if (b.railings.get().booleanValue() && this.needsToMine(b, b.blockPosProvider.getRailings(true), false)) {
                    b.setState(MineRailings);
                } else if (b.railings.get().booleanValue() && this.needsToPlace(b, b.blockPosProvider.getRailings(false), false)) {
                    b.setState(PlaceRailings);
                } else if (this.needsToPlace(b, b.blockPosProvider.getFloor(), false)) {
                    b.setState(PlaceFloor);
                }
            }

            private boolean needsToMine(HighwayBuilder b, MBPIterator it, boolean ignoreBlocksToPlace) {
                for (MBlockPos pos : it) {
                    if (!b.canMine(pos, ignoreBlocksToPlace)) continue;
                    return true;
                }
                return false;
            }

            private boolean needsToPlace(HighwayBuilder b, MBPIterator it, boolean liquids) {
                for (MBlockPos pos : it) {
                    if (!b.canPlace(pos, liquids)) continue;
                    return true;
                }
                return false;
            }
        }
        ,
        FillLiquids{

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, new MBPIteratorFilter(b.blockPosProvider.getLiquids(), pos -> !pos.getState().getFluidState().isEmpty()), slot, Forward);
            }
        }
        ,
        MineFront{

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFront(), true, MineFloor, this);
            }
        }
        ,
        MineFloor{

            @Override
            protected void start(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFloor(), false, MineRailings, this);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFloor(), false, MineRailings, this);
            }
        }
        ,
        MineRailings{

            @Override
            protected void start(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getRailings(true), false, PlaceRailings, this);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getRailings(true), false, PlaceRailings, this);
            }
        }
        ,
        PlaceRailings{

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getRailings(false), slot, Forward);
            }
        }
        ,
        PlaceFloor{

            @Override
            protected void start(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getFloor(), slot, Forward);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getFloor(), slot, Forward);
            }
        }
        ,
        ThrowOutTrash{
            private int skipSlot;
            private boolean timerEnabled;
            private boolean firstTick;
            private int timer;

            @Override
            protected void start(HighwayBuilder b) {
                int biggestCount = 0;
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                    ItemStack itemStack = ((HighwayBuilder)b).mc.player.getInventory().getStack(i);
                    if (!(itemStack.getItem() instanceof BlockItem) || !b.trashItems.get().contains(itemStack.getItem()) || itemStack.getCount() <= biggestCount) continue;
                    biggestCount = itemStack.getCount();
                    this.skipSlot = i;
                    if (biggestCount >= 64) break;
                }
                if (biggestCount == 0) {
                    this.skipSlot = -1;
                }
                this.timerEnabled = false;
                this.firstTick = true;
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (this.timerEnabled) {
                    if (this.timer > 0) {
                        --this.timer;
                    } else {
                        b.setState(b.lastState);
                    }
                    return;
                }
                ((HighwayBuilder)b).mc.player.setYaw(b.dir.opposite().yaw);
                ((HighwayBuilder)b).mc.player.setPitch(-25.0f);
                if (this.firstTick) {
                    this.firstTick = false;
                    return;
                }
                if (!((HighwayBuilder)b).mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    InvUtils.dropHand();
                    return;
                }
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                    if (i == this.skipSlot) continue;
                    ItemStack itemStack = ((HighwayBuilder)b).mc.player.getInventory().getStack(i);
                    if (!b.trashItems.get().contains(itemStack.getItem())) continue;
                    InvUtils.drop().slot(i);
                    return;
                }
                this.timerEnabled = true;
                this.timer = 10;
            }
        }
        ,
        PlaceEChestBlockade{

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getEChestBlockade(false), slot, MineEnderChests);
            }
        }
        ,
        MineEChestBlockade{

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getEChestBlockade(true), true, Center, Forward);
            }
        }
        ,
        MineEnderChests{
            private static final MBlockPos pos = new MBlockPos();
            private int minimumObsidian;
            private boolean first;
            private boolean primed;
            private boolean stopTimerEnabled;
            private int stopTimer;
            private int moveTimer;
            private int rebreakTimer;

            @Override
            protected void start(HighwayBuilder b) {
                if (b.lastState != Center && b.lastState != ThrowOutTrash && b.lastState != PlaceEChestBlockade) {
                    b.setState(Center);
                    return;
                }
                if (b.lastState == Center) {
                    b.setState(ThrowOutTrash);
                    return;
                }
                if (b.lastState == ThrowOutTrash) {
                    b.setState(PlaceEChestBlockade);
                    return;
                }
                int emptySlots = 0;
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                    if (!((HighwayBuilder)b).mc.player.getInventory().getStack(i).isEmpty()) continue;
                    ++emptySlots;
                }
                if (emptySlots == 0) {
                    b.error("No empty slots.", new Object[0]);
                    return;
                }
                int minimumSlots = Math.max(emptySlots - 4, 1);
                this.minimumObsidian = minimumSlots * 64;
                this.first = true;
                this.moveTimer = 0;
                this.stopTimerEnabled = false;
                this.primed = false;
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (this.stopTimerEnabled) {
                    if (this.stopTimer > 0) {
                        --this.stopTimer;
                    } else {
                        b.setState(MineEChestBlockade);
                    }
                    return;
                }
                HorizontalDirection dir = b.dir.diagonal ? b.dir.rotateLeft().rotateLeftSkipOne() : b.dir.opposite();
                pos.set((Entity)((HighwayBuilder)b).mc.player).offset(dir);
                if (this.moveTimer > 0) {
                    ((HighwayBuilder)b).mc.player.setYaw(dir.yaw);
                    b.input.pressingForward = this.moveTimer > 2;
                    --this.moveTimer;
                    return;
                }
                int obsidianCount = 0;
                for (Entity entity : ((HighwayBuilder)b).mc.world.getOtherEntities((Entity)((HighwayBuilder)b).mc.player, new Box((double)12.pos.x, (double)12.pos.y, (double)12.pos.z, (double)(12.pos.x + 1), (double)(12.pos.y + 2), (double)(12.pos.z + 1)))) {
                    ItemEntity itemEntity;
                    if (!(entity instanceof ItemEntity) || (itemEntity = (ItemEntity)entity).getStack().getItem() != Items.OBSIDIAN) continue;
                    obsidianCount += itemEntity.getStack().getCount();
                }
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                    ItemStack itemStack2 = ((HighwayBuilder)b).mc.player.getInventory().getStack(i);
                    if (itemStack2.getItem() != Items.OBSIDIAN) continue;
                    obsidianCount += itemStack2.getCount();
                }
                if (obsidianCount >= this.minimumObsidian) {
                    this.stopTimerEnabled = true;
                    this.stopTimer = 8;
                    return;
                }
                BlockPos bp = pos.getBlockPos();
                BlockState blockState = ((HighwayBuilder)b).mc.world.getBlockState(bp);
                if (blockState.getBlock() == Blocks.ENDER_CHEST) {
                    if (this.first) {
                        this.moveTimer = 8;
                        this.first = false;
                        return;
                    }
                    int slot = this.findAndMoveBestToolToHotbar(b, blockState, true);
                    if (slot == -1) {
                        b.error("Cannot find pickaxe without silk touch to mine ender chests.", new Object[0]);
                        return;
                    }
                    InvUtils.swap(slot, false);
                    if (b.rebreakEchests.get().booleanValue() && this.primed) {
                        if (this.rebreakTimer > 0) {
                            --this.rebreakTimer;
                            return;
                        }
                        PlayerActionC2SPacket p = new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp, BlockUtils.getDirection(bp));
                        this.rebreakTimer = b.rebreakTimer.get();
                        if (b.rotation.get().mine) {
                            Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> b.mc.getNetworkHandler().sendPacket((Packet)p));
                        } else {
                            b.mc.getNetworkHandler().sendPacket((Packet)p);
                        }
                    } else if (b.rotation.get().mine) {
                        Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> BlockUtils.breakBlock(bp, true));
                    } else {
                        BlockUtils.breakBlock(bp, true);
                    }
                } else {
                    int slot = this.findAndMoveToHotbar(b, itemStack -> itemStack.getItem() == Items.ENDER_CHEST, false);
                    if (slot == -1 || this.countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) <= b.saveEchests.get()) {
                        this.stopTimerEnabled = true;
                        this.stopTimer = 4;
                        return;
                    }
                    if (!this.first) {
                        this.primed = true;
                    }
                    BlockUtils.place(bp, Hand.MAIN_HAND, slot, b.rotation.get().place, 0, true, true, false);
                }
            }
        };


        protected void start(HighwayBuilder b) {
        }

        protected abstract void tick(HighwayBuilder var1);

        protected void mine(HighwayBuilder b, MBPIterator it, boolean ignoreBlocksToPlace, State nextState, State lastState) {
            boolean breaking = false;
            boolean finishedBreaking = false;
            for (MBlockPos pos : it) {
                if (b.count >= b.blocksPerTick.get()) {
                    return;
                }
                if (b.breakTimer > 0) {
                    return;
                }
                BlockState state = pos.getState();
                if (state.isAir() || !ignoreBlocksToPlace && b.blocksToPlace.get().contains(state.getBlock())) continue;
                int slot = this.findAndMoveBestToolToHotbar(b, state, false);
                if (slot == -1) {
                    return;
                }
                InvUtils.swap(slot, false);
                BlockPos mcPos = pos.getBlockPos();
                if (BlockUtils.canBreak(mcPos)) {
                    if (b.rotation.get().mine) {
                        Rotations.rotate(Rotations.getYaw(mcPos), Rotations.getPitch(mcPos), () -> BlockUtils.breakBlock(mcPos, true));
                    } else {
                        BlockUtils.breakBlock(mcPos, true);
                    }
                    breaking = true;
                    b.breakTimer = b.breakDelay.get();
                    if (!b.lastBreakingPos.equals(pos)) {
                        b.lastBreakingPos.set(pos);
                        ++b.blocksBroken;
                    }
                    ++b.count;
                    if (b.blocksPerTick.get() == 1 || !BlockUtils.canInstaBreak(mcPos) || b.rotation.get().mine) break;
                }
                if (it.hasNext() || !BlockUtils.canInstaBreak(mcPos)) continue;
                finishedBreaking = true;
            }
            if (finishedBreaking || !breaking) {
                b.setState(nextState);
                b.lastState = lastState;
            }
        }

        protected void place(HighwayBuilder b, MBPIterator it, int slot, State nextState) {
            boolean placed = false;
            boolean finishedPlacing = false;
            for (MBlockPos pos : it) {
                if (b.count >= b.placementsPerTick.get()) {
                    return;
                }
                if (b.placeTimer > 0) {
                    return;
                }
                if (BlockUtils.place(pos.getBlockPos(), Hand.MAIN_HAND, slot, b.rotation.get().place, 0, true, true, true)) {
                    placed = true;
                    ++b.blocksPlaced;
                    b.placeTimer = b.placeDelay.get();
                    ++b.count;
                    if (b.placementsPerTick.get() == 1) break;
                }
                if (it.hasNext()) continue;
                finishedPlacing = true;
            }
            if (finishedPlacing || !placed) {
                b.setState(nextState);
            }
        }

        private int findSlot(HighwayBuilder b, Predicate<ItemStack> predicate, boolean hotbar) {
            for (int i = hotbar ? 0 : 9; i < (hotbar ? 9 : ((HighwayBuilder)b).mc.player.getInventory().main.size()); ++i) {
                if (!predicate.test(((HighwayBuilder)b).mc.player.getInventory().getStack(i))) continue;
                return i;
            }
            return -1;
        }

        private int findHotbarSlot(HighwayBuilder b, boolean replaceTools) {
            int thrashSlot = -1;
            int slotsWithBlocks = 0;
            int slotWithLeastBlocks = -1;
            int slotWithLeastBlocksCount = Integer.MAX_VALUE;
            for (int i = 0; i < 9; ++i) {
                Item item;
                ItemStack itemStack = ((HighwayBuilder)b).mc.player.getInventory().getStack(i);
                if (itemStack.isEmpty()) {
                    return i;
                }
                if (replaceTools && AutoTool.isTool(itemStack)) {
                    return i;
                }
                if (b.trashItems.get().contains(itemStack.getItem())) {
                    thrashSlot = i;
                }
                if (!((item = itemStack.getItem()) instanceof BlockItem)) continue;
                BlockItem blockItem = (BlockItem)item;
                if (!b.blocksToPlace.get().contains(blockItem.getBlock())) continue;
                ++slotsWithBlocks;
                if (itemStack.getCount() >= slotWithLeastBlocksCount) continue;
                slotWithLeastBlocksCount = itemStack.getCount();
                slotWithLeastBlocks = i;
            }
            if (thrashSlot != -1) {
                return thrashSlot;
            }
            if (slotsWithBlocks > 1) {
                return slotWithLeastBlocks;
            }
            b.error("No empty space in hotbar.", new Object[0]);
            return -1;
        }

        private boolean hasItem(HighwayBuilder b, Item item) {
            for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                if (((HighwayBuilder)b).mc.player.getInventory().getStack(i).getItem() != item) continue;
                return true;
            }
            return false;
        }

        protected int countItem(HighwayBuilder b, Predicate<ItemStack> predicate) {
            int count = 0;
            for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                ItemStack stack = ((HighwayBuilder)b).mc.player.getInventory().getStack(i);
                if (!predicate.test(stack)) continue;
                count += stack.getCount();
            }
            return count;
        }

        protected int findAndMoveToHotbar(HighwayBuilder b, Predicate<ItemStack> predicate, boolean required) {
            int slot = this.findSlot(b, predicate, true);
            if (slot != -1) {
                return slot;
            }
            int hotbarSlot = this.findHotbarSlot(b, false);
            if (hotbarSlot == -1) {
                return -1;
            }
            slot = this.findSlot(b, predicate, false);
            if (slot == -1) {
                if (required) {
                    b.error("Out of items.", new Object[0]);
                }
                return -1;
            }
            InvUtils.move().from(slot).toHotbar(hotbarSlot);
            InvUtils.dropHand();
            return hotbarSlot;
        }

        protected int findAndMoveBestToolToHotbar(HighwayBuilder b, BlockState blockState, boolean noSilkTouch) {
            int count;
            if (((HighwayBuilder)b).mc.player.isCreative()) {
                return ((HighwayBuilder)b).mc.player.getInventory().selectedSlot;
            }
            double bestScore = -1.0;
            int bestSlot = -1;
            for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().main.size(); ++i) {
                double score = AutoTool.getScore(((HighwayBuilder)b).mc.player.getInventory().getStack(i), blockState, false, false, AutoTool.EnchantPreference.None, itemStack -> {
                    if (noSilkTouch && Utils.hasEnchantment(itemStack, (RegistryKey<Enchantment>)Enchantments.SILK_TOUCH)) {
                        return false;
                    }
                    return b.dontBreakTools.get() == false || itemStack.getMaxDamage() - itemStack.getDamage() > 1;
                });
                if (!(score > bestScore)) continue;
                bestScore = score;
                bestSlot = i;
            }
            if (bestSlot == -1) {
                return ((HighwayBuilder)b).mc.player.getInventory().selectedSlot;
            }
            if (((HighwayBuilder)b).mc.player.getInventory().getStack(bestSlot).getItem() instanceof PickaxeItem && (count = this.countItem(b, stack -> stack.getItem() instanceof PickaxeItem)) <= b.savePickaxes.get()) {
                b.error("Found less than the selected amount of pickaxes required: " + count + "/" + (b.savePickaxes.get() + 1), new Object[0]);
                return -1;
            }
            if (bestSlot < 9) {
                return bestSlot;
            }
            int hotbarSlot = this.findHotbarSlot(b, true);
            if (hotbarSlot == -1) {
                return -1;
            }
            InvUtils.move().from(bestSlot).toHotbar(hotbarSlot);
            InvUtils.dropHand();
            return hotbarSlot;
        }

        protected int findBlocksToPlace(HighwayBuilder b) {
            int slot = this.findAndMoveToHotbar(b, itemStack -> {
                Item patt0$temp = itemStack.getItem();
                if (!(patt0$temp instanceof BlockItem)) return false;
                BlockItem blockItem = (BlockItem)patt0$temp;
                if (!b.blocksToPlace.get().contains(blockItem.getBlock())) return false;
                return true;
            }, false);
            if (slot == -1) {
                if (!b.mineEnderChests.get().booleanValue() || !this.hasItem(b, Items.ENDER_CHEST) || this.countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) <= b.saveEchests.get()) {
                    b.error("Out of blocks to place.", new Object[0]);
                } else {
                    b.setState(MineEnderChests);
                }
                return -1;
            }
            return slot;
        }

        protected int findBlocksToPlacePrioritizeTrash(HighwayBuilder b) {
            int slot = this.findAndMoveToHotbar(b, itemStack -> {
                if (!(itemStack.getItem() instanceof BlockItem)) {
                    return false;
                }
                return b.trashItems.get().contains(itemStack.getItem());
            }, false);
            return slot != -1 ? slot : this.findBlocksToPlace(b);
        }
    }

    private class DiagonalBlockPosProvider
    implements IBlockPosProvider {
        private final MBlockPos pos = new MBlockPos();
        private final MBlockPos pos2 = new MBlockPos();

        private DiagonalBlockPosProvider() {
        }

        @Override
        public MBPIterator getFront() {
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir.rotateLeft()).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft() - 1);
            return new MBPIterator(){
                private int i;
                private int w;
                private int y;
                private int pi;
                private int pw;
                private int py;

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.w < HighwayBuilder.this.width.get() && this.y < HighwayBuilder.this.height.get();
                }

                @Override
                public MBlockPos next() {
                    DiagonalBlockPosProvider.this.pos2.set(DiagonalBlockPosProvider.this.pos).offset(HighwayBuilder.this.rightDir, this.w).add(0, this.y++, 0);
                    if (this.y >= HighwayBuilder.this.height.get()) {
                        this.y = 0;
                        ++this.w;
                        if (this.w >= (this.i == 0 ? HighwayBuilder.this.width.get() - 1 : HighwayBuilder.this.width.get())) {
                            this.w = 0;
                            ++this.i;
                            DiagonalBlockPosProvider.this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
                        }
                    }
                    return DiagonalBlockPosProvider.this.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        DiagonalBlockPosProvider.this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir.rotateLeft()).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft() - 1);
                    } else {
                        DiagonalBlockPosProvider.this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.w = this.pw;
                    this.y = this.py;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getFloor() {
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).add(0, -1, 0).offset(HighwayBuilder.this.dir.rotateLeft()).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft() - 1);
            return new MBPIterator(){
                private int i;
                private int w;
                private int pi;
                private int pw;

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.w < HighwayBuilder.this.width.get();
                }

                @Override
                public MBlockPos next() {
                    DiagonalBlockPosProvider.this.pos2.set(DiagonalBlockPosProvider.this.pos).offset(HighwayBuilder.this.rightDir, this.w++);
                    if (this.w >= (this.i == 0 ? HighwayBuilder.this.width.get() - 1 : HighwayBuilder.this.width.get())) {
                        this.w = 0;
                        ++this.i;
                        DiagonalBlockPosProvider.this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).add(0, -1, 0).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
                    }
                    return DiagonalBlockPosProvider.this.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        DiagonalBlockPosProvider.this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).add(0, -1, 0).offset(HighwayBuilder.this.dir.rotateLeft()).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft() - 1);
                    } else {
                        DiagonalBlockPosProvider.this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).add(0, -1, 0).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.pw = this.w;
                    this.w = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.w = this.pw;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getRailings(boolean mine) {
            final boolean mineAll = mine && HighwayBuilder.this.mineAboveRailings.get() != false;
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir.rotateLeft()).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    this.this$1 = this$1;
                }

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.y < (mineAll ? this.this$1.HighwayBuilder.this.height.get() : 1);
                }

                @Override
                public MBlockPos next() {
                    this.this$1.pos2.set(this.this$1.pos).add(0, this.y++, 0);
                    if (this.y >= (mineAll ? this.this$1.HighwayBuilder.this.height.get() : 1)) {
                        this.y = 0;
                        ++this.i;
                        this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir.rotateRight()).offset(this.this$1.HighwayBuilder.this.rightDir, this.this$1.HighwayBuilder.this.getWidthRight());
                    }
                    return this.this$1.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir.rotateLeft()).offset(this.this$1.HighwayBuilder.this.leftDir, this.this$1.HighwayBuilder.this.getWidthLeft());
                    } else {
                        this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir.rotateRight()).offset(this.this$1.HighwayBuilder.this.rightDir, this.this$1.HighwayBuilder.this.getWidthRight());
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.y = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getLiquids() {
            final boolean m = HighwayBuilder.this.railings.get() != false && HighwayBuilder.this.mineAboveRailings.get() != false;
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.dir.rotateLeft()).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
            return new MBPIterator(){
                private int i;
                private int w;
                private int y;
                private int pi;
                private int pw;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    this.this$1 = this$1;
                }

                private int getWidth() {
                    return this.this$1.HighwayBuilder.this.width.get() + (this.i == 0 ? 1 : 0) + (m && this.i == 1 ? 2 : 0);
                }

                @Override
                public boolean hasNext() {
                    if (m && this.i == 1 && this.y == this.this$1.HighwayBuilder.this.height.get() && this.w == this.getWidth() - 1) {
                        return false;
                    }
                    return this.i < 2 && this.w < this.getWidth() && this.y < this.this$1.HighwayBuilder.this.height.get() + 1;
                }

                private void updateW() {
                    ++this.w;
                    if (this.w >= this.getWidth()) {
                        this.w = 0;
                        ++this.i;
                        this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir, 2).offset(this.this$1.HighwayBuilder.this.leftDir, this.this$1.HighwayBuilder.this.getWidthLeft() + (m ? 1 : 0));
                    }
                }

                @Override
                public MBlockPos next() {
                    if (this.i == (m ? 1 : 0) && this.y == this.this$1.HighwayBuilder.this.height.get() && (this.w == 0 || this.w == this.getWidth() - 1)) {
                        this.y = 0;
                        this.updateW();
                    }
                    this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.HighwayBuilder.this.rightDir, this.w).add(0, this.y++, 0);
                    if (this.y >= this.this$1.HighwayBuilder.this.height.get() + 1) {
                        this.y = 0;
                        this.updateW();
                    }
                    return this.this$1.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir).offset(this.this$1.HighwayBuilder.this.dir.rotateLeft()).offset(this.this$1.HighwayBuilder.this.leftDir, this.this$1.HighwayBuilder.this.getWidthLeft());
                    } else {
                        this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir, 2).offset(this.this$1.HighwayBuilder.this.leftDir, this.this$1.HighwayBuilder.this.getWidthLeft() + (m ? 1 : 0));
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.w = this.pw;
                    this.y = this.py;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getEChestBlockade(final boolean mine) {
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    this.this$1 = this$1;
                    this.i = mine ? -1 : 0;
                }

                private MBlockPos get(int i) {
                    HorizontalDirection dir2 = this.this$1.HighwayBuilder.this.dir.rotateLeft().rotateLeftSkipOne();
                    this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(dir2);
                    return switch (i) {
                        case -1 -> this.this$1.pos;
                        default -> this.this$1.pos.offset(dir2);
                        case 1 -> this.this$1.pos.offset(dir2.rotateLeftSkipOne());
                        case 2 -> this.this$1.pos.offset(dir2.rotateLeftSkipOne().opposite());
                        case 3 -> this.this$1.pos.offset(dir2.opposite(), 2);
                    };
                }

                @Override
                public boolean hasNext() {
                    return this.i < 4 && this.y < 2;
                }

                @Override
                public MBlockPos next() {
                    MBlockPos pos = this.get(this.i).add(0, this.y, 0);
                    ++this.y;
                    if (this.y > 1) {
                        this.y = 0;
                        ++this.i;
                    }
                    return pos;
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.y = 0;
                    this.i = 0;
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                }
            };
        }
    }

    private class StraightBlockPosProvider
    implements IBlockPosProvider {
        private final MBlockPos pos = new MBlockPos();
        private final MBlockPos pos2 = new MBlockPos();

        private StraightBlockPosProvider() {
        }

        @Override
        public MBPIterator getFront() {
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft());
            return new MBPIterator(){
                private int w;
                private int y;
                private int pw;
                private int py;

                @Override
                public boolean hasNext() {
                    return this.w < HighwayBuilder.this.width.get() && this.y < HighwayBuilder.this.height.get();
                }

                @Override
                public MBlockPos next() {
                    StraightBlockPosProvider.this.pos2.set(StraightBlockPosProvider.this.pos).offset(HighwayBuilder.this.rightDir, this.w).add(0, this.y, 0);
                    ++this.w;
                    if (this.w >= HighwayBuilder.this.width.get()) {
                        this.w = 0;
                        ++this.y;
                    }
                    return StraightBlockPosProvider.this.pos2;
                }

                @Override
                public void save() {
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                }

                @Override
                public void restore() {
                    this.w = this.pw;
                    this.y = this.py;
                }
            };
        }

        @Override
        public MBPIterator getFloor() {
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft()).add(0, -1, 0);
            return new MBPIterator(){
                private int w;
                private int pw;

                @Override
                public boolean hasNext() {
                    return this.w < HighwayBuilder.this.width.get();
                }

                @Override
                public MBlockPos next() {
                    return StraightBlockPosProvider.this.pos2.set(StraightBlockPosProvider.this.pos).offset(HighwayBuilder.this.rightDir, this.w++);
                }

                @Override
                public void save() {
                    this.pw = this.w;
                    this.w = 0;
                }

                @Override
                public void restore() {
                    this.w = this.pw;
                }
            };
        }

        @Override
        public MBPIterator getRailings(boolean mine) {
            final boolean mineAll = mine && HighwayBuilder.this.mineAboveRailings.get() != false;
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir);
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    this.this$1 = this$1;
                }

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.y < (mineAll ? this.this$1.HighwayBuilder.this.height.get() : 1);
                }

                @Override
                public MBlockPos next() {
                    if (this.i == 0) {
                        this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.HighwayBuilder.this.leftDir, this.this$1.HighwayBuilder.this.getWidthLeft() + 1).add(0, this.y, 0);
                    } else {
                        this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.HighwayBuilder.this.rightDir, this.this$1.HighwayBuilder.this.getWidthRight() + 1).add(0, this.y, 0);
                    }
                    ++this.y;
                    if (this.y >= (mineAll ? this.this$1.HighwayBuilder.this.height.get() : 1)) {
                        this.y = 0;
                        ++this.i;
                    }
                    return this.this$1.pos2;
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.y = 0;
                    this.i = 0;
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                }
            };
        }

        @Override
        public MBPIterator getLiquids() {
            this.pos.set((Entity)((HighwayBuilder)HighwayBuilder.this).mc.player).offset(HighwayBuilder.this.dir, 2).offset(HighwayBuilder.this.leftDir, HighwayBuilder.this.getWidthLeft() + (HighwayBuilder.this.railings.get() != false && HighwayBuilder.this.mineAboveRailings.get() != false ? 2 : 1));
            return new MBPIterator(){
                private int w;
                private int y;
                private int pw;
                private int py;

                private int getWidth() {
                    return HighwayBuilder.this.width.get() + (HighwayBuilder.this.railings.get() != false && HighwayBuilder.this.mineAboveRailings.get() != false ? 2 : 0);
                }

                @Override
                public boolean hasNext() {
                    return this.w < this.getWidth() + 2 && this.y < HighwayBuilder.this.height.get() + 1;
                }

                @Override
                public MBlockPos next() {
                    StraightBlockPosProvider.this.pos2.set(StraightBlockPosProvider.this.pos).offset(HighwayBuilder.this.rightDir, this.w).add(0, this.y, 0);
                    ++this.w;
                    if (this.w >= this.getWidth() + 2) {
                        this.w = 0;
                        ++this.y;
                    }
                    return StraightBlockPosProvider.this.pos2;
                }

                @Override
                public void save() {
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                }

                @Override
                public void restore() {
                    this.w = this.pw;
                    this.y = this.py;
                }
            };
        }

        @Override
        public MBPIterator getEChestBlockade(final boolean mine) {
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    this.this$1 = this$1;
                    this.i = mine ? -1 : 0;
                }

                private MBlockPos get(int i) {
                    this.this$1.pos.set((Entity)((HighwayBuilder)this.this$1.HighwayBuilder.this).mc.player).offset(this.this$1.HighwayBuilder.this.dir.opposite());
                    return switch (i) {
                        case -1 -> this.this$1.pos;
                        default -> this.this$1.pos.offset(this.this$1.HighwayBuilder.this.dir.opposite());
                        case 1 -> this.this$1.pos.offset(this.this$1.HighwayBuilder.this.leftDir);
                        case 2 -> this.this$1.pos.offset(this.this$1.HighwayBuilder.this.rightDir);
                        case 3 -> this.this$1.pos.offset(this.this$1.HighwayBuilder.this.dir, 2);
                    };
                }

                @Override
                public boolean hasNext() {
                    return this.i < 4 && this.y < 2;
                }

                @Override
                public MBlockPos next() {
                    if (this.this$1.HighwayBuilder.this.width.get() == 1 && this.this$1.HighwayBuilder.this.railings.get().booleanValue() && this.i > 0 && this.y == 0) {
                        ++this.y;
                    }
                    MBlockPos pos = this.get(this.i).add(0, this.y, 0);
                    ++this.y;
                    if (this.y > 1) {
                        this.y = 0;
                        ++this.i;
                    }
                    return pos;
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.y = 0;
                    this.i = 0;
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                }
            };
        }
    }

    private static interface IBlockPosProvider {
        public MBPIterator getFront();

        public MBPIterator getFloor();

        public MBPIterator getRailings(boolean var1);

        public MBPIterator getLiquids();

        public MBPIterator getEChestBlockade(boolean var1);
    }

    private static interface MBPIterator
    extends Iterator<MBlockPos>,
    Iterable<MBlockPos> {
        public void save();

        public void restore();

        @Override
        @NotNull
        default public Iterator<MBlockPos> iterator() {
            return this;
        }
    }

    private static class MBPIteratorFilter
    implements MBPIterator {
        private final MBPIterator it;
        private final Predicate<MBlockPos> predicate;
        private MBlockPos pos;
        private boolean isOld = true;
        private boolean pisOld = true;

        public MBPIteratorFilter(MBPIterator it, Predicate<MBlockPos> predicate) {
            this.it = it;
            this.predicate = predicate;
        }

        @Override
        public void save() {
            this.it.save();
            this.pisOld = this.isOld;
            this.isOld = true;
        }

        @Override
        public void restore() {
            this.it.restore();
            this.isOld = this.pisOld;
        }

        @Override
        public boolean hasNext() {
            if (this.isOld) {
                this.isOld = false;
                this.pos = null;
                while (this.it.hasNext()) {
                    this.pos = (MBlockPos)this.it.next();
                    if (this.predicate.test(this.pos)) {
                        return true;
                    }
                    this.pos = null;
                }
            }
            return this.pos != null && this.predicate.test(this.pos);
        }

        @Override
        public MBlockPos next() {
            this.isOld = true;
            return this.pos;
        }
    }
}

