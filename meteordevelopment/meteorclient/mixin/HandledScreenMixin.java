/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.BookScreen
 *  net.minecraft.client.gui.screen.ingame.BookScreen$Contents
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider
 *  net.minecraft.client.gui.widget.ButtonWidget$Builder
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.text.Text
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.render.ItemHighlight;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenMixin<T extends ScreenHandler>
extends Screen
implements ScreenHandlerProvider<T> {
    @Shadow
    protected Slot focusedSlot;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    private boolean doubleClicking;
    @Unique
    private static final ItemStack[] ITEMS = new ItemStack[27];

    @Shadow
    @Nullable
    protected abstract Slot getSlotAt(double var1, double var3);

    @Shadow
    public abstract T getScreenHandler();

    @Shadow
    protected abstract void onMouseClick(Slot var1, int var2, int var3, SlotActionType var4);

    @Shadow
    public abstract void close();

    public HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        InventoryTweaks invTweaks = Modules.get().get(InventoryTweaks.class);
        if (invTweaks.isActive() && invTweaks.showButtons() && invTweaks.canSteal((ScreenHandler)this.getScreenHandler())) {
            this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Steal"), button -> invTweaks.steal((ScreenHandler)this.getScreenHandler())).position(this.x, this.y - 22).size(40, 20).build());
            this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Dump"), button -> invTweaks.dump((ScreenHandler)this.getScreenHandler())).position(this.x + 42, this.y - 22).size(40, 20).build());
        }
    }

    @Inject(method={"mouseDragged"}, at={@At(value="TAIL")})
    private void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> info) {
        if (button != 0 || this.doubleClicking || !Modules.get().get(InventoryTweaks.class).mouseDragItemMove()) {
            return;
        }
        Slot slot = this.getSlotAt(mouseX, mouseY);
        if (slot != null && slot.hasStack() && HandledScreenMixin.hasShiftDown()) {
            this.onMouseClick(slot, slot.id, button, SlotActionType.QUICK_MOVE);
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        BetterTooltips toolips = Modules.get().get(BetterTooltips.class);
        if (button == 2 && this.focusedSlot != null && !this.focusedSlot.getStack().isEmpty() && MeteorClient.mc.player.currentScreenHandler.getCursorStack().isEmpty() && toolips.middleClickOpen()) {
            ItemStack itemStack = this.focusedSlot.getStack();
            if (Utils.hasItems(itemStack) || itemStack.getItem() == Items.ENDER_CHEST) {
                cir.setReturnValue((Object)Utils.openContainer(this.focusedSlot.getStack(), ITEMS, false));
            } else if (itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) != null || itemStack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT) != null) {
                this.close();
                MeteorClient.mc.setScreen((Screen)new BookScreen(BookScreen.Contents.create((ItemStack)itemStack)));
                cir.setReturnValue((Object)true);
            }
        }
    }

    @Inject(method={"drawSlot"}, at={@At(value="HEAD")})
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        int color = Modules.get().get(ItemHighlight.class).getColor(slot.getStack());
        if (color != -1) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
        }
    }
}

