package com.grihail.modernposes.mixins.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The off hand's slot on 1.7.10's inventory picture: a frame cut from the picture's own slots. */
@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends GuiContainer {

    protected MixinGuiInventory(Container container) {
        super(container);
    }

    @Inject(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiInventory;func_147046_a(IIIFFLnet/minecraft/entity/EntityLivingBase;)V"))
    private void modernposes$offhandFrame(float partial, int mouseX, int mouseY, CallbackInfo ci) {
        drawTexturedModalRect(this.guiLeft + 76, this.guiTop + 61, 7, 83, 18, 18);
    }
}
