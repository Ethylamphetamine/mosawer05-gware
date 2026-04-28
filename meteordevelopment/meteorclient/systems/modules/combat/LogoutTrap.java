/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.PlayerJoinLeaveEvent;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.LogoutSpots;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LogoutTrap
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> trapBlocks;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> placementColor;
    private SilentMine silentMine;
    private final Map<UUID, PlayerEntity> playerCache;
    private final Map<UUID, TrappedSpot> spots;
    private final Map<BlockPos, Long> recentlyPlaced;
    private static final long RENDER_MS = 300L;
    private static final long PLACEMENT_WINDOW_MS = 300L;
    private static final int MAX_PLACEMENTS_PER_WINDOW = 9;
    private static final double PLACE_RANGE = 4.5;
    private static final int MAX_BREAKS_PER_TICK = 2;
    private long placementWindowStart;
    private int placementsThisWindow;

    public LogoutTrap() {
        super(Categories.Combat, "logout-trap", "Traps logout spots by mining and placing blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.trapBlocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Blocks used to build the trap.")).defaultValue(Blocks.OBSIDIAN).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("Side color.")).defaultValue(new SettingColor(197, 137, 232, 10)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("Line color.")).defaultValue(new SettingColor(197, 137, 232)).build());
        this.placementColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("placement-color")).description("Placement color.")).defaultValue(new SettingColor(0, 200, 255, 35)).build());
        this.silentMine = null;
        this.playerCache = new ConcurrentHashMap<UUID, PlayerEntity>();
        this.spots = new ConcurrentHashMap<UUID, TrappedSpot>();
        this.recentlyPlaced = new ConcurrentHashMap<BlockPos, Long>();
        this.placementWindowStart = 0L;
        this.placementsThisWindow = 0;
    }

    @Override
    public void onActivate() {
        super.onActivate();
        this.silentMine = Modules.get().get(SilentMine.class);
        this.spots.clear();
        this.playerCache.clear();
        this.recentlyPlaced.clear();
        this.placementWindowStart = 0L;
        this.placementsThisWindow = 0;
        this.syncFromLogoutSpots();
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (this.mc.world == null || this.mc.player == null || this.silentMine == null) {
            return;
        }
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (player == null || player.equals((Object)this.mc.player)) continue;
            this.playerCache.put(player.getUuid(), player);
        }
        this.syncFromLogoutSpots();
        long now = System.currentTimeMillis();
        if (now - this.placementWindowStart >= 300L) {
            this.placementWindowStart = now;
            this.placementsThisWindow = 0;
        }
        int miningCapacity = 0;
        if (!this.silentMine.hasDelayedDestroy()) {
            ++miningCapacity;
        }
        if (!this.silentMine.hasRebreakBlock() || this.silentMine.canRebreakRebreakBlock()) {
            ++miningCapacity;
        }
        Iterator<Map.Entry<UUID, TrappedSpot>> it = this.spots.entrySet().iterator();
        while (it.hasNext()) {
            TrappedSpot spot = it.next().getValue();
            if (!spot.breakQueue.isEmpty()) {
                int breakCount = 0;
                while (breakCount < 2 && !spot.breakQueue.isEmpty()) {
                    BlockPos next = spot.breakQueue.peekFirst();
                    if (next == null) {
                        spot.breakQueue.removeFirst();
                        continue;
                    }
                    if (this.silentMine.alreadyBreaking(next)) {
                        spot.breakQueue.removeFirst();
                        if (spot.currentlyBreaking.contains(next)) continue;
                        spot.currentlyBreaking.add(next);
                        continue;
                    }
                    BlockState st = this.mc.world.getBlockState(next);
                    if (st.isAir()) {
                        spot.breakQueue.removeFirst();
                        continue;
                    }
                    if (spot.trapShellPositions.contains(next) && (this.trapBlocks.get().contains(st.getBlock()) || st.isOf(Blocks.BEDROCK))) {
                        spot.breakQueue.removeFirst();
                        continue;
                    }
                    if (!BlockUtils.canBreak(next, st)) {
                        spot.breakQueue.removeFirst();
                        continue;
                    }
                    if (!this.silentMine.inBreakRange(next) || miningCapacity <= 0) break;
                    this.silentMine.silentBreakBlock(next, 20.0);
                    spot.currentlyBreaking.add(next);
                    spot.breakQueue.removeFirst();
                    --miningCapacity;
                    ++breakCount;
                }
            }
            if (!spot.placeQueue.isEmpty()) {
                BlockPos place = spot.placeQueue.peekFirst();
                if (place == null) {
                    spot.placeQueue.removeFirst();
                    continue;
                }
                BlockState cur = this.mc.world.getBlockState(place);
                if (!cur.isAir()) {
                    spot.placeQueue.removeFirst();
                    if (this.trapBlocks.get().contains(cur.getBlock()) || cur.isOf(Blocks.BEDROCK)) {
                        spot.placed.add(place);
                        continue;
                    }
                    if (!BlockUtils.canBreak(place, cur)) continue;
                    spot.breakQueue.addLast(place);
                    continue;
                }
                double d = this.mc.player.getEyePos().distanceTo(Vec3d.ofCenter((Vec3i)place));
                if (d <= 4.5) {
                    if (this.placementsThisWindow < 9) {
                        boolean ok = this.placeExactFromWhitelist(this.trapBlocks.get(), place);
                        if (ok) {
                            spot.placed.add(place);
                            this.recentlyPlaced.put(place, System.currentTimeMillis());
                            spot.placeQueue.removeFirst();
                            ++this.placementsThisWindow;
                        } else if (!this.hasAnyBlockInInventory(this.trapBlocks.get())) {
                            spot.placeQueue.clear();
                        } else {
                            BlockPos failedPos = spot.placeQueue.removeFirst();
                            spot.placeQueue.addLast(failedPos);
                        }
                    }
                } else {
                    BlockPos outOfRangePos = spot.placeQueue.removeFirst();
                    spot.placeQueue.addLast(outOfRangePos);
                }
            }
            if (!spot.breakQueue.isEmpty() || !spot.placeQueue.isEmpty() || !spot.currentlyBreaking.isEmpty()) continue;
            spot.buildPlan();
        }
        long now2 = System.currentTimeMillis();
        this.recentlyPlaced.entrySet().removeIf(e -> now2 - (Long)e.getValue() > 300L);
    }

    @EventHandler
    private void onSilentMineFinished(SilentMineFinishedEvent.Pre event) {
        BlockPos pos = event.getBlockPos();
        if (pos == null) {
            return;
        }
        for (TrappedSpot spot : this.spots.values()) {
            if (!spot.currentlyBreaking.remove(pos)) continue;
            this.recentlyPlaced.remove(pos);
            if (!spot.trapShellPositions.contains(pos) && !spot.trapCorePositions.contains(pos)) break;
            spot.buildPlan();
            break;
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerJoinLeaveEvent.Leave event) {
        if (event.getEntry().getProfile() == null) {
            return;
        }
        UUID leaveId = event.getEntry().getProfile().getId();
        if (!this.playerCache.containsKey(leaveId)) {
            return;
        }
        PlayerEntity player = this.playerCache.get(leaveId);
        if (player == null) {
            return;
        }
        if (!Friends.get().shouldAttack(player)) {
            return;
        }
        if (player instanceof FakePlayerEntity) {
            return;
        }
        if (!this.spots.containsKey(leaveId)) {
            BlockPos feet = player.getBlockPos();
            TrappedSpot spot = new TrappedSpot(leaveId, player.getName().getString(), feet, player);
            spot.buildPlan();
            this.spots.put(leaveId, spot);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        long now = System.currentTimeMillis();
        for (Map.Entry<BlockPos, Long> e : this.recentlyPlaced.entrySet()) {
            BlockPos p = e.getKey();
            if (now - e.getValue() > 300L) continue;
            event.renderer.box(p, (Color)this.placementColor.get(), (Color)this.placementColor.get(), this.shapeMode.get(), 0);
        }
        for (TrappedSpot spot : this.spots.values()) {
            for (BlockPos p : spot.placed) {
                Long t = this.recentlyPlaced.get(p);
                if (t != null && now - t <= 300L) continue;
                event.renderer.box(p, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
            }
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.spots.size());
    }

    private boolean placeExactFromWhitelist(List<Block> blocks, BlockPos pos) {
        Item item;
        for (Block block : blocks) {
            item = block.asItem();
            FindItemResult hot = InvUtils.findInHotbar(item);
            if (!hot.found()) continue;
            if (!MeteorClient.BLOCK.beginPlacement(List.of(pos), item)) {
                return false;
            }
            boolean ok = MeteorClient.BLOCK.placeBlock(item, pos);
            MeteorClient.BLOCK.endPlacement();
            return ok;
        }
        for (Block block : blocks) {
            item = block.asItem();
            FindItemResult inv = InvUtils.find(item);
            if (!inv.found()) continue;
            int invSlot = inv.slot();
            int hotbarSlot = this.mc.player.getInventory().selectedSlot;
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
            boolean ok = false;
            if (MeteorClient.BLOCK.beginPlacement(List.of(pos), item)) {
                ok = MeteorClient.BLOCK.placeBlock(item, pos);
                MeteorClient.BLOCK.endPlacement();
            }
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, invSlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
            return ok;
        }
        return false;
    }

    private boolean hasAnyBlockInInventory(List<Block> blocks) {
        for (Block b : blocks) {
            if (!InvUtils.findInHotbar(b.asItem()).found() && !InvUtils.find(b.asItem()).found()) continue;
            return true;
        }
        return false;
    }

    private void syncFromLogoutSpots() {
        try {
            LogoutSpots ls = Modules.get().get(LogoutSpots.class);
            if (ls == null || !ls.isActive()) {
                this.spots.clear();
                return;
            }
            Field f = LogoutSpots.class.getDeclaredField("loggedPlayers");
            f.setAccessible(true);
            Object mapObj = f.get(ls);
            if (!(mapObj instanceof Map)) {
                return;
            }
            Map logged = (Map)mapObj;
            this.spots.keySet().removeIf(uuid -> !logged.containsKey(uuid));
            Field posField = null;
            Field nameField = null;
            Field playerEntityField = null;
            for (Map.Entry entry : logged.entrySet()) {
                Object val;
                UUID key;
                if (!(entry.getKey() instanceof UUID) || this.spots.containsKey(key = (UUID)entry.getKey()) || (val = entry.getValue()) == null) continue;
                Class<?> cls = val.getClass();
                try {
                    PlayerEntity pEntity = null;
                    try {
                        if (playerEntityField == null) {
                            playerEntityField = cls.getDeclaredField("playerEntity");
                        }
                        playerEntityField.setAccessible(true);
                        pEntity = (PlayerEntity)playerEntityField.get(val);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (pEntity != null && !Friends.get().shouldAttack(pEntity)) continue;
                    if (posField == null) {
                        posField = cls.getDeclaredField("pos");
                    }
                    posField.setAccessible(true);
                    Vec3d pos = (Vec3d)posField.get(val);
                    if (nameField == null) {
                        nameField = cls.getDeclaredField("name");
                    }
                    nameField.setAccessible(true);
                    String name = (String)nameField.get(val);
                    if (pos == null) continue;
                    BlockPos bp = BlockPos.ofFloored((Position)pos);
                    TrappedSpot spot = new TrappedSpot(key, name != null ? name : "unknown", bp, pEntity);
                    spot.buildPlan();
                    this.spots.put(key, spot);
                }
                catch (IllegalAccessException | NoSuchFieldException reflectiveOperationException) {}
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private class TrappedSpot {
        public final UUID uuid;
        public final String name;
        public final BlockPos pos;
        public final long logoutTime;
        private final Deque<BlockPos> breakQueue = new LinkedList<BlockPos>();
        private final Deque<BlockPos> placeQueue = new LinkedList<BlockPos>();
        public final Deque<BlockPos> placed = new LinkedList<BlockPos>();
        public final List<BlockPos> currentlyBreaking = new LinkedList<BlockPos>();
        public long lastMineScheduled = 0L;
        private final int boxHeight;
        public final LinkedHashSet<BlockPos> trapShellPositions = new LinkedHashSet();
        public final LinkedHashSet<BlockPos> trapCorePositions = new LinkedHashSet();

        TrappedSpot(UUID uuid, String name, BlockPos pos, PlayerEntity playerAtLogout) {
            this.uuid = uuid;
            this.name = name;
            this.pos = pos;
            this.logoutTime = System.currentTimeMillis();
            double playerHeight = playerAtLogout != null ? (double)playerAtLogout.getHeight() : 1.8;
            this.boxHeight = playerHeight >= 2.5 ? 3 : 2;
        }

        public void buildPlan() {
            int dz;
            int dx;
            this.breakQueue.clear();
            this.placeQueue.clear();
            this.placed.clear();
            this.trapShellPositions.clear();
            this.trapCorePositions.clear();
            if (((LogoutTrap)LogoutTrap.this).mc.world == null) {
                return;
            }
            int cx = this.pos.getX();
            int cy = this.pos.getY();
            int cz = this.pos.getZ();
            for (int dx2 = -1; dx2 <= 1; ++dx2) {
                for (int dz2 = -1; dz2 <= 1; ++dz2) {
                    this.trapShellPositions.add(new BlockPos(cx + dx2, cy - 1, cz + dz2));
                }
            }
            for (int dy = 0; dy < this.boxHeight; ++dy) {
                for (dx = -2; dx <= 2; ++dx) {
                    for (dz = -2; dz <= 2; ++dz) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != 2 || Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                        this.trapShellPositions.add(new BlockPos(cx + dx, cy + dy, cz + dz));
                    }
                }
            }
            int topY = cy + this.boxHeight;
            for (dx = -1; dx <= 1; ++dx) {
                for (dz = -1; dz <= 1; ++dz) {
                    this.trapShellPositions.add(new BlockPos(cx + dx, topY, cz + dz));
                }
            }
            for (int dy = 0; dy < this.boxHeight; ++dy) {
                for (int dx3 = -1; dx3 <= 1; ++dx3) {
                    for (int dz3 = -1; dz3 <= 1; ++dz3) {
                        this.trapCorePositions.add(new BlockPos(cx + dx3, cy + dy, cz + dz3));
                    }
                }
            }
            for (BlockPos p : this.trapShellPositions) {
                BlockState st = ((LogoutTrap)LogoutTrap.this).mc.world.getBlockState(p);
                if (st.isAir()) {
                    this.placeQueue.addLast(p);
                    continue;
                }
                if (LogoutTrap.this.trapBlocks.get().contains(st.getBlock()) || st.isOf(Blocks.BEDROCK)) {
                    this.placed.add(p);
                    continue;
                }
                if (!BlockUtils.canBreak(p, st)) continue;
                this.breakQueue.addLast(p);
            }
            for (BlockPos p : this.trapCorePositions) {
                BlockState st = ((LogoutTrap)LogoutTrap.this).mc.world.getBlockState(p);
                if (st.isAir() || !BlockUtils.canBreak(p, st)) continue;
                this.breakQueue.addLast(p);
            }
        }
    }
}

