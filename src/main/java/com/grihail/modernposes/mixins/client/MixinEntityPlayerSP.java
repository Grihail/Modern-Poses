package com.grihail.modernposes.mixins.client;

import net.minecraft.client.entity.EntityPlayerSP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.ClientPoses;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    /** Right after the keys were read into movementInput: crawl slowdown, double-tap sprint in water. */
    @Inject(
        method = "onLivingUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V",
            shift = At.Shift.AFTER))
    private void modernposes$afterInput(CallbackInfo ci) {
        ClientPoses.afterMoveInput((EntityPlayerSP) (Object) this);
    }
}
