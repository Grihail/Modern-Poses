package com.grihail.modernposes.mixins.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.BipedPose;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    /** Before vanilla's angles: what the left arm holds, and an off hand's swing kept from the right arm. */
    @Inject(method = "setRotationAngles", at = @At("HEAD"))
    private void modernposes$before(float limbSwing, float limbAmount, float age, float headYaw, float pitch,
        float scale, Entity entity, CallbackInfo ci) {
        BipedPose.beforeAngles((ModelBiped) (Object) this, entity);
    }

    /** Gliding: the swing of arms and legs shrinks with the flight's speed (26.3's speedValue). */
    @ModifyVariable(method = "setRotationAngles", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float modernposes$limbAmount(float amount, float limbSwing, float limbAmount, float age, float headYaw,
        float pitch, float scale, Entity entity) {
        return BipedPose.limbAmount(entity, amount);
    }

    /** Modern sneak and swim model on top of vanilla angles; armor models run through here too, so armor follows. */
    @Inject(method = "setRotationAngles", at = @At("TAIL"))
    private void modernposes$pose(float limbSwing, float limbAmount, float age, float headYaw, float pitch, float scale,
        Entity entity, CallbackInfo ci) {
        BipedPose.afterSetRotationAngles((ModelBiped) (Object) this, limbSwing, limbAmount, age, entity);
    }
}
