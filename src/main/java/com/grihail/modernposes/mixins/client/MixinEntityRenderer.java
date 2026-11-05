package com.grihail.modernposes.mixins.client;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.client.PoseCamera;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "orientCamera", at = @At("HEAD"), require = 1)
    private void modernposes$partial(float partialTicks, CallbackInfo ci) {
        PoseCamera.partialTicks = partialTicks;
    }

    /**
     * orientCamera starts with {@code f1 = renderViewEntity.yOffset - 1.62} (vanilla: the eye is always 1.62 above
     * the feet) and puts the camera, the third person rays and the cloud fog test at feet + 1.62. The read of yOffset
     * (the only one in the method) returns a value that makes {@code f1 = yOffset - eye}, so all of them start at the
     * pose's real eye. {@code require = 1}: a miss fails at startup instead of silently.
     */
    @Redirect(
        method = "orientCamera",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;yOffset:F", ordinal = 0),
        require = 1)
    private float modernposes$eyeOffset(EntityLivingBase e) {
        return PoseCamera.eyeOffset(e.yOffset - Sizes.VANILLA_EYE) + Sizes.VANILLA_EYE;
    }
}
