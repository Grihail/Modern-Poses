package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.HandClient;
import com.grihail.modernposes.client.HandRender;
import com.grihail.modernposes.combat.Combat;

/** Both hands in the first person. */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void modernposes$hands(float partial, CallbackInfo ci) {
        if (HandRender.firstPerson((ItemRenderer) (Object) this, partial)) {
            ci.cancel();
        }
    }

    /** A use by the off hand bobs the off hand's item, not the main one's. */
    @Inject(method = { "resetEquippedProgress", "resetEquippedProgress2" }, at = @At("HEAD"), cancellable = true)
    private void modernposes$offReset(CallbackInfo ci) {
        if (HandClient.isOffPass()) {
            HandRender.resetOffhand();
            ci.cancel();
        }
    }

    /** FirstPersonHandsAndItems.tick: the arm comes back up with the cube of the charge. */
    @ModifyConstant(method = "updateEquippedItem", constant = @Constant(floatValue = 1.0F))
    private float modernposes$armHeight(float vanilla) {
        float scale = Combat.swapScale(Minecraft.getMinecraft().thePlayer, 1.0F);
        return scale * scale * scale;
    }

    @Inject(method = "updateEquippedItem", at = @At("HEAD"))
    private void modernposes$keepRaised(CallbackInfo ci) {
        HandRender.keepRaised((ItemRenderer) (Object) this, Minecraft.getMinecraft().thePlayer);
    }

    @Inject(method = "updateEquippedItem", at = @At("TAIL"))
    private void modernposes$tick(CallbackInfo ci) {
        HandRender.tickFirstPerson(Minecraft.getMinecraft().thePlayer);
    }
}
