package com.grihail.modernposes.mixins.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.BipedPose;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    /** Modern sneak and swim model on top of vanilla angles; armor models run through here too, so armor follows. */
    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void modernposes$pose(float limbSwing, float limbAmount, float age, float headYaw, float pitch, float scale,
        Entity entity, CallbackInfo ci) {
        BipedPose.afterSetRotationAngles((ModelBiped) (Object) this, limbSwing, entity);
    }
}
