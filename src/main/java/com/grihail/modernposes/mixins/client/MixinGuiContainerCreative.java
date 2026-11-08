package com.grihail.modernposes.mixins.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.hands.OffhandSlot;

/** The off hand's slot on the creative inventory tab, to the right of the armor. */
@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends GuiContainer {

    @Unique
    private static final ResourceLocation modernposes$TAB = new ResourceLocation(
        "textures/gui/container/creative_inventory/tab_inventory.png");
    @Unique
    private static final int modernposes$X = 90, modernposes$Y = 33;

    @Shadow
    private static int selectedTabIndex;

    protected MixinGuiContainerCreative(Container container) {
        super(container);
    }

    @Inject(method = "setCurrentCreativeTab", at = @At("RETURN"))
    private void modernposes$placeSlot(CreativeTabs tab, CallbackInfo ci) {
        if (tab == CreativeTabs.tabInventory && this.inventorySlots.inventorySlots.size() > OffhandSlot.INDEX) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(OffhandSlot.INDEX);
            slot.xDisplayPosition = modernposes$X;
            slot.yDisplayPosition = modernposes$Y;
        }
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("TAIL"))
    private void modernposes$frame(float partial, int mouseX, int mouseY, CallbackInfo ci) {
        if (selectedTabIndex == CreativeTabs.tabInventory.getTabIndex()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            this.mc.getTextureManager()
                .bindTexture(modernposes$TAB);
            drawTexturedModalRect(this.guiLeft + modernposes$X - 1, this.guiTop + modernposes$Y - 1, 8, 111, 18, 18);
        }
    }
}
