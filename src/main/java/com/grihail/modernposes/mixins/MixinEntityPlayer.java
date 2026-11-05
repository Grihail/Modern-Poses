package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.ModernPosesMod;
import com.grihail.modernposes.Sizes;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    /** Sneaking does not lower the camera by ySize (the pose's eye does); modern water movement of the own player. */
    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"), cancellable = true)
    private void modernposes$move(float strafe, float forward, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        Sizes.beforeMove(self);
        if (self.worldObj.isRemote && ModernPosesMod.proxy.handleMovement(self, strafe, forward)) ci.cancel();
    }
}
