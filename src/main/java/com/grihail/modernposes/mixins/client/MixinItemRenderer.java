package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.HandRender;

/** Both hands in the first person, and items drawn as models. */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void modernposes$hands(float partial, CallbackInfo ci) {
        if (HandRender.firstPerson((ItemRenderer) (Object) this, partial)) {
            ci.cancel();
        }
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
