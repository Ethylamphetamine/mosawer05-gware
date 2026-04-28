/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.command.CommandSource
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.player.AntiHunger;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class DamageCommand
extends Command {
    private static final SimpleCommandExceptionType INVULNERABLE = new SimpleCommandExceptionType((Message)Text.literal((String)"You are invulnerable."));

    public DamageCommand() {
        super("damage", "Damages self", "dmg");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(DamageCommand.argument("damage", IntegerArgumentType.integer((int)1, (int)7)).executes(context -> {
            int amount = IntegerArgumentType.getInteger((CommandContext)context, (String)"damage");
            if (DamageCommand.mc.player.getAbilities().invulnerable) {
                throw INVULNERABLE.create();
            }
            this.damagePlayer(amount);
            return 1;
        }));
    }

    private void damagePlayer(int amount) {
        boolean antiHunger;
        boolean noFall = Modules.get().isActive(NoFall.class);
        if (noFall) {
            Modules.get().get(NoFall.class).toggle();
        }
        if (antiHunger = Modules.get().isActive(AntiHunger.class)) {
            Modules.get().get(AntiHunger.class).toggle();
        }
        Vec3d pos = DamageCommand.mc.player.getPos();
        for (int i = 0; i < 80; ++i) {
            this.sendPositionPacket(pos.x, pos.y + (double)amount + 2.1, pos.z, false);
            this.sendPositionPacket(pos.x, pos.y + 0.05, pos.z, false);
        }
        this.sendPositionPacket(pos.x, pos.y, pos.z, true);
        if (noFall) {
            Modules.get().get(NoFall.class).toggle();
        }
        if (antiHunger) {
            Modules.get().get(AntiHunger.class).toggle();
        }
    }

    private void sendPositionPacket(double x, double y, double z, boolean onGround) {
        DamageCommand.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));
    }
}

