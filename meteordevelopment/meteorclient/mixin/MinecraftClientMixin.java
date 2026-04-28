/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.Mouse
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  net.minecraft.client.option.GameOptions
 *  net.minecraft.client.util.Window
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.profiler.Profiler
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.callback.LocalCapture
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.concurrent.CompletableFuture;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.FastUse;
import meteordevelopment.meteorclient.systems.modules.player.Multitask;
import meteordevelopment.meteorclient.systems.modules.render.UnfocusedCPU;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CPSUtils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.starscript.Script;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value={MinecraftClient.class}, priority=1001)
public abstract class MinecraftClientMixin
implements IMinecraftClient {
    @Unique
    private boolean doItemUseCalled;
    @Unique
    private boolean rightClick;
    @Unique
    private long lastTime;
    @Unique
    private boolean firstFrame;
    @Shadow
    public ClientWorld world;
    @Shadow
    @Final
    public Mouse mouse;
    @Shadow
    @Final
    private Window window;
    @Shadow
    public Screen currentScreen;
    @Shadow
    @Final
    public GameOptions options;
    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    @Shadow
    private int itemUseCooldown;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    protected abstract void doItemUse();

    @Shadow
    public abstract Profiler getProfiler();

    @Shadow
    public abstract boolean isWindowFocused();

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        MeteorClient.INSTANCE.onInitializeClient();
        this.firstFrame = true;
    }

    @Inject(at={@At(value="HEAD")}, method={"tick"})
    private void onPreTick(CallbackInfo info) {
        OnlinePlayers.update();
        this.doItemUseCalled = false;
        this.getProfiler().push("meteor-client_pre_update");
        MeteorClient.EVENT_BUS.post(TickEvent.Pre.get());
        this.getProfiler().pop();
        if (this.rightClick && !this.doItemUseCalled && this.interactionManager != null) {
            this.doItemUse();
        }
        this.rightClick = false;
    }

    @Inject(at={@At(value="TAIL")}, method={"tick"})
    private void onTick(CallbackInfo info) {
        this.getProfiler().push("meteor-client_post_update");
        MeteorClient.EVENT_BUS.post(TickEvent.Post.get());
        this.getProfiler().pop();
    }

    @Inject(method={"doAttack"}, at={@At(value="HEAD")})
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        CPSUtils.onAttack();
    }

    @Inject(method={"doItemUse"}, at={@At(value="HEAD")})
    private void onDoItemUse(CallbackInfo info) {
        this.doItemUseCalled = true;
    }

    @Inject(method={"disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V"}, at={@At(value="HEAD")})
    private void onDisconnect(Screen screen, boolean transferring, CallbackInfo info) {
        if (this.world != null) {
            MeteorClient.EVENT_BUS.post(GameLeftEvent.get());
        }
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof WidgetScreen) {
            screen.mouseMoved(this.mouse.getX() * this.window.getScaleFactor(), this.mouse.getY() * this.window.getScaleFactor());
        }
        OpenScreenEvent event = OpenScreenEvent.get(screen);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"doItemUse"}, at={@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z")}, locals=LocalCapture.CAPTURE_FAILHARD)
    private void onDoItemUseHand(CallbackInfo ci, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack) {
        FastUse fastUse = Modules.get().get(FastUse.class);
        if (fastUse.isActive()) {
            this.itemUseCooldown = fastUse.getItemUseCooldown(itemStack);
        }
    }

    @ModifyExpressionValue(method={"doItemUse"}, at={@At(value="FIELD", target="Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;", ordinal=1)})
    private HitResult doItemUseMinecraftClientCrosshairTargetProxy(HitResult original) {
        return MeteorClient.EVENT_BUS.post(ItemUseCrosshairTargetEvent.get((HitResult)original)).target;
    }

    @ModifyReturnValue(method={"reloadResources(ZLnet/minecraft/client/MinecraftClient$LoadingContext;)Ljava/util/concurrent/CompletableFuture;"}, at={@At(value="RETURN")})
    private CompletableFuture<Void> onReloadResourcesNewCompletableFuture(CompletableFuture<Void> original) {
        return original.thenRun(() -> MeteorClient.EVENT_BUS.post(ResourcePacksReloadedEvent.get()));
    }

    @ModifyArg(method={"updateWindowTitle"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        String title;
        if (Config.get() == null || !Config.get().customWindowTitle.get().booleanValue()) {
            return original;
        }
        String customTitle = Config.get().customWindowTitleText.get();
        Script script = MeteorStarscript.compile(customTitle);
        if (script != null && (title = MeteorStarscript.run(script)) != null) {
            customTitle = title;
        }
        return customTitle;
    }

    @Inject(method={"onResolutionChanged"}, at={@At(value="TAIL")})
    private void onResolutionChanged(CallbackInfo info) {
        MeteorClient.EVENT_BUS.post(ResolutionChangedEvent.get());
    }

    @Inject(method={"getFramerateLimit"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        if (Modules.get().isActive(UnfocusedCPU.class) && !this.isWindowFocused()) {
            info.setReturnValue((Object)Math.min(Modules.get().get(UnfocusedCPU.class).fps.get(), (Integer)this.options.getMaxFps().getValue()));
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void onRender(CallbackInfo info) {
        long time = System.currentTimeMillis();
        if (this.firstFrame) {
            this.lastTime = time;
            this.firstFrame = false;
        }
        Utils.frameTime = (double)(time - this.lastTime) / 1000.0;
        this.lastTime = time;
    }

    @ModifyExpressionValue(method={"doItemUse"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z")})
    private boolean doItemUseModifyIsBreakingBlock(boolean original) {
        return !Modules.get().isActive(Multitask.class) && original;
    }

    @ModifyExpressionValue(method={"handleBlockBreaking"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")})
    private boolean handleBlockBreakingModifyIsUsingItem(boolean original) {
        return !Modules.get().isActive(Multitask.class) && original;
    }

    @ModifyExpressionValue(method={"handleInputEvents"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal=0)})
    private boolean handleInputEventsModifyIsUsingItem(boolean original) {
        return !Modules.get().get(Multitask.class).attackingEntities() && original;
    }

    @Inject(method={"handleInputEvents"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal=0, shift=At.Shift.BEFORE)})
    private void handleInputEventsInjectStopUsingItem(CallbackInfo info) {
        if (Modules.get().get(Multitask.class).attackingEntities() && this.player.isUsingItem()) {
            if (!this.options.useKey.isPressed()) {
                this.interactionManager.stopUsingItem((PlayerEntity)this.player);
            }
            while (this.options.useKey.wasPressed()) {
            }
        }
    }

    @Override
    public void meteor_client$rightClick() {
        this.rightClick = true;
    }
}

