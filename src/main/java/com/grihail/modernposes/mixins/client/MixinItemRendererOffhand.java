package com.grihail.modernposes.mixins.client;

import net.minecraft.client.renderer.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.HandClient;
import com.grihail.modernposes.client.HandRender;

/** Feature offhand. */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRendererOffhand {

    /** A use by the off hand bobs the off hand's item, not the main one's. */
    @Inject(method = { "resetEquippedProgress", "resetEquippedProgress2" }, at = @At("HEAD"), cancellable = true)
    private void modernposes$offReset(CallbackInfo ci) {
        if (HandClient.isOffPass()) {
            HandRender.resetOffhand();
            ci.cancel();
        }
    }
}
