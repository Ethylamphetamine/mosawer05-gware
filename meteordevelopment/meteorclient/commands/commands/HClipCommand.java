/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class HClipCommand
extends Command {
    public HClipCommand() {
        super("hclip", "Lets you clip through blocks horizontally.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(HClipCommand.argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            double blocks = (Double)context.getArgument("blocks", Double.class);
            Vec3d forward = Vec3d.fromPolar((float)0.0f, (float)HClipCommand.mc.player.getYaw()).normalize();
            if (HClipCommand.mc.player.hasVehicle()) {
                Entity vehicle = HClipCommand.mc.player.getVehicle();
                vehicle.setPosition(vehicle.getX() + forward.x * blocks, vehicle.getY(), vehicle.getZ() + forward.z * blocks);
            }
            HClipCommand.mc.player.setPosition(HClipCommand.mc.player.getX() + forward.x * blocks, HClipCommand.mc.player.getY(), HClipCommand.mc.player.getZ() + forward.z * blocks);
            return 1;
        }));
    }
}

