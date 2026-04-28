/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

public class VClipCommand
extends Command {
    public VClipCommand() {
        super("vclip", "Lets you clip through blocks vertically.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(VClipCommand.argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            double blocks = (Double)context.getArgument("blocks", Double.class);
            int packetsRequired = (int)Math.ceil(Math.abs(blocks / 10.0));
            if (packetsRequired > 20) {
                packetsRequired = 1;
            }
            if (VClipCommand.mc.player.hasVehicle()) {
                for (int packetNumber = 0; packetNumber < packetsRequired - 1; ++packetNumber) {
                    VClipCommand.mc.player.networkHandler.sendPacket((Packet)new VehicleMoveC2SPacket(VClipCommand.mc.player.getVehicle()));
                }
                VClipCommand.mc.player.getVehicle().setPosition(VClipCommand.mc.player.getVehicle().getX(), VClipCommand.mc.player.getVehicle().getY() + blocks, VClipCommand.mc.player.getVehicle().getZ());
                VClipCommand.mc.player.networkHandler.sendPacket((Packet)new VehicleMoveC2SPacket(VClipCommand.mc.player.getVehicle()));
            } else {
                for (int packetNumber = 0; packetNumber < packetsRequired - 1; ++packetNumber) {
                    VClipCommand.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(true));
                }
                VClipCommand.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(VClipCommand.mc.player.getX(), VClipCommand.mc.player.getY() + blocks, VClipCommand.mc.player.getZ(), true));
                VClipCommand.mc.player.setPosition(VClipCommand.mc.player.getX(), VClipCommand.mc.player.getY() + blocks, VClipCommand.mc.player.getZ());
            }
            return 1;
        }));
    }
}

