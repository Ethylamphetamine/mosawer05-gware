/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.command.CommandSource
 *  net.minecraft.component.ComponentMap
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.LodestoneTrackerComponent
 *  net.minecraft.component.type.MapDecorationsComponent
 *  net.minecraft.component.type.MapDecorationsComponent$Decoration
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.EyeOfEnderEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.map.MapDecorationType
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.GlobalPos
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.World
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.commands.commands;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LocateCommand
extends Command {
    private Vec3d firstStart;
    private Vec3d firstEnd;
    private Vec3d secondStart;
    private Vec3d secondEnd;
    private final List<Block> netherFortressBlocks = List.of(Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_WART);
    private final List<Block> monumentBlocks = List.of(Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE);
    private final List<Block> strongholdBlocks = List.of(Blocks.END_PORTAL_FRAME);
    private final List<Block> endCityBlocks = List.of(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.PURPUR_SLAB, Blocks.PURPUR_STAIRS, Blocks.END_STONE_BRICKS, Blocks.END_ROD);

    public LocateCommand() {
        super("locate", "Locates structures", "loc");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(LocateCommand.literal("buried_treasure").executes(s -> {
            ItemStack stack = LocateCommand.mc.player.getInventory().getMainHandStack();
            if (stack.getItem() != Items.FILLED_MAP || stack.get(DataComponentTypes.ITEM_NAME) == null || !((Text)stack.get(DataComponentTypes.ITEM_NAME)).getString().equals(Text.translatable((String)"filled_map.buried_treasure").getString())) {
                this.error("You need to hold a (highlight)buried treasure map(default)!", new Object[0]);
                return 1;
            }
            MapDecorationsComponent mapDecorationsComponent = (MapDecorationsComponent)stack.get(DataComponentTypes.MAP_DECORATIONS);
            if (mapDecorationsComponent == null) {
                this.error("Couldn't locate the map icons!", new Object[0]);
                return 1;
            }
            for (MapDecorationsComponent.Decoration decoration : mapDecorationsComponent.comp_2404().values()) {
                if (!((MapDecorationType)decoration.comp_2405().comp_349()).comp_2514().toString().equals("minecraft:red_x")) continue;
                Vec3d coords = new Vec3d(decoration.comp_2406(), 62.0, decoration.comp_2407());
                MutableText text = Text.literal((String)"Buried Treasure located at ");
                text.append((Text)ChatUtils.formatCoords(coords));
                text.append(".");
                this.info((Text)text);
                return 1;
            }
            this.error("Couldn't locate the buried treasure!", new Object[0]);
            return 1;
        }));
        builder.then(LocateCommand.literal("mansion").executes(s -> {
            ItemStack stack = LocateCommand.mc.player.getInventory().getMainHandStack();
            if (stack.getItem() != Items.FILLED_MAP || stack.get(DataComponentTypes.ITEM_NAME) == null || !((Text)stack.get(DataComponentTypes.ITEM_NAME)).getString().equals(Text.translatable((String)"filled_map.mansion").getString())) {
                this.error("You need to hold a (highlight)woodland explorer map(default)!", new Object[0]);
                return 1;
            }
            MapDecorationsComponent mapDecorationsComponent = (MapDecorationsComponent)stack.get(DataComponentTypes.MAP_DECORATIONS);
            if (mapDecorationsComponent == null) {
                this.error("Couldn't locate the map icons!", new Object[0]);
                return 1;
            }
            for (MapDecorationsComponent.Decoration decoration : mapDecorationsComponent.comp_2404().values()) {
                if (!((MapDecorationType)decoration.comp_2405().comp_349()).comp_2514().toString().equals("minecraft:woodland_mansion")) continue;
                Vec3d coords = new Vec3d(decoration.comp_2406(), 62.0, decoration.comp_2407());
                MutableText text = Text.literal((String)"Mansion located at ");
                text.append((Text)ChatUtils.formatCoords(coords));
                text.append(".");
                this.info((Text)text);
                return 1;
            }
            this.error("Couldn't locate the mansion!", new Object[0]);
            return 1;
        }));
        builder.then(LocateCommand.literal("monument").executes(s -> {
            ItemStack stack = LocateCommand.mc.player.getInventory().getMainHandStack();
            if (stack.getItem() == Items.FILLED_MAP && stack.get(DataComponentTypes.ITEM_NAME) != null && ((Text)stack.get(DataComponentTypes.ITEM_NAME)).getString().equals(Text.translatable((String)"filled_map.monument").getString())) {
                MapDecorationsComponent mapDecorationsComponent = (MapDecorationsComponent)stack.get(DataComponentTypes.MAP_DECORATIONS);
                if (mapDecorationsComponent == null) {
                    this.error("Couldn't locate the map icons!", new Object[0]);
                    return 1;
                }
                for (MapDecorationsComponent.Decoration decoration : mapDecorationsComponent.comp_2404().values()) {
                    if (!((MapDecorationType)decoration.comp_2405().comp_349()).comp_2514().toString().equals("minecraft:ocean_monument")) continue;
                    Vec3d coords = new Vec3d(decoration.comp_2406(), 62.0, decoration.comp_2407());
                    MutableText text = Text.literal((String)"Monument located at ");
                    text.append((Text)ChatUtils.formatCoords(coords));
                    text.append(".");
                    this.info((Text)text);
                    return 1;
                }
                this.error("Couldn't locate the monument!", new Object[0]);
                return 1;
            }
            if (BaritoneUtils.IS_AVAILABLE) {
                Vec3d coords = this.findByBlockList(this.monumentBlocks);
                if (coords == null) {
                    this.error("No monument found. Try using an (highlight)ocean explorer map(default) for more success.", new Object[0]);
                    return 1;
                }
                MutableText text = Text.literal((String)"Monument located at ");
                text.append((Text)ChatUtils.formatCoords(coords));
                text.append(".");
                this.info((Text)text);
                return 1;
            }
            this.error("Locating this structure without an (highlight)ocean explorer map(default) requires Baritone.", new Object[0]);
            return 1;
        }));
        builder.then(LocateCommand.literal("stronghold").executes(s -> {
            boolean foundEye = InvUtils.testInHotbar(Items.ENDER_EYE);
            if (foundEye) {
                if (BaritoneUtils.IS_AVAILABLE) {
                    PathManagers.get().follow(EyeOfEnderEntity.class::isInstance);
                }
                this.firstStart = null;
                this.firstEnd = null;
                this.secondStart = null;
                this.secondEnd = null;
                MeteorClient.EVENT_BUS.subscribe(this);
                this.info("Please throw the first Eye of Ender", new Object[0]);
            } else if (BaritoneUtils.IS_AVAILABLE) {
                Vec3d coords = this.findByBlockList(this.strongholdBlocks);
                if (coords == null) {
                    this.error("No stronghold found nearby. You can use (highlight)Ender Eyes(default) for more success.", new Object[0]);
                    return 1;
                }
                MutableText text = Text.literal((String)"Stronghold located at ");
                text.append((Text)ChatUtils.formatCoords(coords));
                text.append(".");
                this.info((Text)text);
            } else {
                this.error("No Eyes of Ender found in hotbar.", new Object[0]);
            }
            return 1;
        }));
        builder.then(LocateCommand.literal("nether_fortress").executes(s -> {
            if (LocateCommand.mc.world.getRegistryKey() != World.NETHER) {
                this.error("You need to be in the nether to locate a nether fortress.", new Object[0]);
                return 1;
            }
            if (!BaritoneUtils.IS_AVAILABLE) {
                this.error("Locating this structure requires Baritone.", new Object[0]);
                return 1;
            }
            Vec3d coords = this.findByBlockList(this.netherFortressBlocks);
            if (coords == null) {
                this.error("No nether fortress found.", new Object[0]);
                return 1;
            }
            MutableText text = Text.literal((String)"Fortress located at ");
            text.append((Text)ChatUtils.formatCoords(coords));
            text.append(".");
            this.info((Text)text);
            return 1;
        }));
        builder.then(LocateCommand.literal("end_city").executes(s -> {
            if (LocateCommand.mc.world.getRegistryKey() != World.END) {
                this.error("You need to be in the end to locate an end city.", new Object[0]);
                return 1;
            }
            if (!BaritoneUtils.IS_AVAILABLE) {
                this.error("Locating this structure requires Baritone.", new Object[0]);
                return 1;
            }
            Vec3d coords = this.findByBlockList(this.endCityBlocks);
            if (coords == null) {
                this.error("No end city found.", new Object[0]);
                return 1;
            }
            MutableText text = Text.literal((String)"End city located at ");
            text.append((Text)ChatUtils.formatCoords(coords));
            text.append(".");
            this.info((Text)text);
            return 1;
        }));
        builder.then(LocateCommand.literal("lodestone").executes(s -> {
            ItemStack stack = LocateCommand.mc.player.getInventory().getMainHandStack();
            if (stack.getItem() != Items.COMPASS) {
                this.error("You need to hold a (highlight)lodestone(default) compass!", new Object[0]);
                return 1;
            }
            ComponentMap components = stack.getComponents();
            if (components == null) {
                this.error("Couldn't get the components data. Are you holding a (highlight)lodestone(default) compass?", new Object[0]);
                return 1;
            }
            LodestoneTrackerComponent lodestoneTrackerComponent = (LodestoneTrackerComponent)components.get(DataComponentTypes.LODESTONE_TRACKER);
            if (lodestoneTrackerComponent == null) {
                this.error("Couldn't get the components data. Are you holding a (highlight)lodestone(default) compass?", new Object[0]);
                return 1;
            }
            if (lodestoneTrackerComponent.comp_2402().isEmpty()) {
                this.error("Couldn't get the lodestone's target!", new Object[0]);
                return 1;
            }
            Vec3d coords = Vec3d.of((Vec3i)((GlobalPos)lodestoneTrackerComponent.comp_2402().get()).comp_2208());
            MutableText text = Text.literal((String)"Lodestone located at ");
            text.append((Text)ChatUtils.formatCoords(coords));
            text.append(".");
            this.info((Text)text);
            return 1;
        }));
        builder.then(LocateCommand.literal("cancel").executes(s -> {
            this.cancel();
            return 1;
        }));
    }

    private void cancel() {
        this.warning("Locate canceled", new Object[0]);
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    @Nullable
    private Vec3d findByBlockList(List<Block> blockList) {
        List posList = BaritoneAPI.getProvider().getWorldScanner().scanChunkRadius(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext(), blockList, 64, 10, 32);
        if (posList.isEmpty()) {
            return null;
        }
        if (posList.size() < 3) {
            this.warning("Only %d block(s) found. This search might be a false positive.", posList.size());
        }
        return new Vec3d((double)((BlockPos)posList.getFirst()).getX(), (double)((BlockPos)posList.getFirst()).getY(), (double)((BlockPos)posList.getFirst()).getZ());
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        EntitySpawnS2CPacket packet;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof EntitySpawnS2CPacket && (packet = (EntitySpawnS2CPacket)packet2).getEntityType() == EntityType.EYE_OF_ENDER) {
            this.firstPosition(packet.getX(), packet.getY(), packet.getZ());
        }
        if ((packet2 = event.packet) instanceof PlaySoundS2CPacket && (packet = (PlaySoundS2CPacket)packet2).getSound().comp_349() == SoundEvents.ENTITY_ENDER_EYE_DEATH) {
            this.lastPosition(packet.getX(), packet.getY(), packet.getZ());
        }
    }

    private void firstPosition(double x, double y, double z) {
        Vec3d pos = new Vec3d(x, y, z);
        if (this.firstStart == null) {
            this.firstStart = pos;
        } else {
            this.secondStart = pos;
        }
    }

    private void lastPosition(double x, double y, double z) {
        this.info("%s Eye of Ender's trajectory saved.", this.firstEnd == null ? "First" : "Second");
        Vec3d pos = new Vec3d(x, y, z);
        if (this.firstEnd == null) {
            this.firstEnd = pos;
            this.info("Please throw the second Eye Of Ender from a different location.", new Object[0]);
        } else {
            this.secondEnd = pos;
            this.findStronghold();
        }
    }

    private void findStronghold() {
        PathManagers.get().stop();
        if (this.firstStart == null || this.firstEnd == null || this.secondStart == null || this.secondEnd == null) {
            this.error("Missing position data", new Object[0]);
            this.cancel();
            return;
        }
        double[] start = new double[]{this.secondStart.x, this.secondStart.z, this.secondEnd.x, this.secondEnd.z};
        double[] end = new double[]{this.firstStart.x, this.firstStart.z, this.firstEnd.x, this.firstEnd.z};
        double[] intersection = this.calcIntersection(start, end);
        if (Double.isNaN(intersection[0]) || Double.isNaN(intersection[1]) || Double.isInfinite(intersection[0]) || Double.isInfinite(intersection[1])) {
            this.error("Unable to calculate intersection.", new Object[0]);
            this.cancel();
            return;
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
        Vec3d coords = new Vec3d(intersection[0], 0.0, intersection[1]);
        MutableText text = Text.literal((String)"Stronghold roughly located at ");
        text.append((Text)ChatUtils.formatCoords(coords));
        text.append(".");
        this.info((Text)text);
    }

    private double[] calcIntersection(double[] line, double[] line2) {
        double a1 = line[3] - line[1];
        double b1 = line[0] - line[2];
        double c1 = a1 * line[0] + b1 * line[1];
        double a2 = line2[3] - line2[1];
        double b2 = line2[0] - line2[2];
        double c2 = a2 * line2[0] + b2 * line2[1];
        double delta = a1 * b2 - a2 * b1;
        return new double[]{(b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta};
    }
}

