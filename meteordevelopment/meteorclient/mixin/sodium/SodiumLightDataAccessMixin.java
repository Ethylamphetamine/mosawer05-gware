/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess
 *  net.minecraft.block.BlockState
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.world.BlockRenderView
 *  net.minecraft.world.LightType
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin.sodium;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Fullbright;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LightDataAccess.class}, remap=false)
public abstract class SodiumLightDataAccessMixin {
    @Unique
    private static final int FULL_LIGHT = 4095;
    @Shadow
    protected BlockRenderView level;
    @Shadow
    @Final
    private BlockPos.Mutable pos;
    @Unique
    private Xray xray;
    @Unique
    private Fullbright fb;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        this.xray = Modules.get().get(Xray.class);
        this.fb = Modules.get().get(Fullbright.class);
    }

    @ModifyVariable(method={"compute"}, at=@At(value="TAIL"), name={"bl"})
    private int compute_modifyBL(int light) {
        BlockState state;
        if (this.xray.isActive() && !this.xray.isBlocked((state = this.level.getBlockState((BlockPos)this.pos)).getBlock(), (BlockPos)this.pos)) {
            return 4095;
        }
        return light;
    }

    @ModifyVariable(method={"compute"}, at=@At(value="STORE"), name={"sl"})
    private int compute_assignSL(int sl) {
        return Math.max(this.fb.getLuminance(LightType.SKY), sl);
    }

    @ModifyVariable(method={"compute"}, at=@At(value="STORE"), name={"bl"})
    private int compute_assignBL(int bl) {
        return Math.max(this.fb.getLuminance(LightType.BLOCK), bl);
    }
}

