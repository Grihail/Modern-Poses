package com.grihail.modernposes.mixins.client;

import net.minecraft.client.entity.EntityOtherPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.HandClient;

/** Vanilla puts only the main hand's item in use for other players. */
@Mixin(EntityOtherPlayerMP.class)
public abstract class MixinEntityOtherPlayerMP {

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void modernposes$offUse(CallbackInfo ci) {
        HandClient.remoteUse((EntityOtherPlayerMP) (Object) this);
    }
}
