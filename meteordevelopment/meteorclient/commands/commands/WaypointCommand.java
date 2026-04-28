/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.command.CommandSource
 *  net.minecraft.command.argument.PosArgument
 *  net.minecraft.command.argument.Vec3ArgumentType
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.WaypointArgumentType;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class WaypointCommand
extends Command {
    public WaypointCommand() {
        super("waypoint", "Manages waypoints.", "wp");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(WaypointCommand.literal("list").executes(context -> {
            if (Waypoints.get().isEmpty()) {
                this.error("No created waypoints.", new Object[0]);
            } else {
                this.info(String.valueOf(Formatting.WHITE) + "Created Waypoints:", new Object[0]);
                for (Waypoint waypoint : Waypoints.get()) {
                    this.info("Name: (highlight)'%s'(default), Dimension: (highlight)%s(default), Pos: (highlight)%s(default)", new Object[]{waypoint.name.get(), waypoint.dimension.get(), this.waypointPos(waypoint)});
                }
            }
            return 1;
        }));
        builder.then(WaypointCommand.literal("get").then(WaypointCommand.argument("waypoint", WaypointArgumentType.create()).executes(context -> {
            Waypoint waypoint = WaypointArgumentType.get(context);
            this.info("Name: " + String.valueOf(Formatting.WHITE) + waypoint.name.get(), new Object[0]);
            this.info("Actual Dimension: " + String.valueOf(Formatting.WHITE) + String.valueOf((Object)waypoint.dimension.get()), new Object[0]);
            this.info("Position: " + String.valueOf(Formatting.WHITE) + this.waypointFullPos(waypoint), new Object[0]);
            this.info("Visible: " + (waypoint.visible.get() != false ? String.valueOf(Formatting.GREEN) + "True" : String.valueOf(Formatting.RED) + "False"), new Object[0]);
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)WaypointCommand.literal("add").then(WaypointCommand.argument("pos", Vec3ArgumentType.vec3()).then(WaypointCommand.argument("waypoint", StringArgumentType.greedyString()).executes(context -> this.addWaypoint((CommandContext<CommandSource>)context, true))))).then(WaypointCommand.argument("waypoint", StringArgumentType.greedyString()).executes(context -> this.addWaypoint((CommandContext<CommandSource>)context, false))));
        builder.then(WaypointCommand.literal("delete").then(WaypointCommand.argument("waypoint", WaypointArgumentType.create()).executes(context -> {
            Waypoint waypoint = WaypointArgumentType.get(context);
            this.info("The waypoint (highlight)'%s'(default) has been deleted.", waypoint.name.get());
            Waypoints.get().remove(waypoint);
            return 1;
        })));
        builder.then(WaypointCommand.literal("toggle").then(WaypointCommand.argument("waypoint", WaypointArgumentType.create()).executes(context -> {
            Waypoint waypoint = WaypointArgumentType.get(context);
            waypoint.visible.set(waypoint.visible.get() == false);
            Waypoints.get().save();
            return 1;
        })));
    }

    private String waypointPos(Waypoint waypoint) {
        return "X: " + waypoint.pos.get().getX() + " Z: " + waypoint.pos.get().getZ();
    }

    private String waypointFullPos(Waypoint waypoint) {
        return "X: " + waypoint.pos.get().getX() + ", Y: " + waypoint.pos.get().getY() + ", Z: " + waypoint.pos.get().getZ();
    }

    private int addWaypoint(CommandContext<CommandSource> context, boolean withCoords) {
        if (WaypointCommand.mc.player == null) {
            return -1;
        }
        BlockPos pos = withCoords ? ((PosArgument)context.getArgument("pos", PosArgument.class)).toAbsoluteBlockPos(WaypointCommand.mc.player.getCommandSource()) : WaypointCommand.mc.player.getBlockPos().up(2);
        Waypoint waypoint = new Waypoint.Builder().name(StringArgumentType.getString(context, (String)"waypoint")).pos(pos).dimension(PlayerUtils.getDimension()).build();
        Waypoints.get().add(waypoint);
        this.info("Created waypoint with name: (highlight)%s(default)", waypoint.name.get());
        return 1;
    }
}

