package com.grihail.modernposes.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.combat.Combat;

/** Feature attackCooldown. */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBaseCombat {

    /** LivingEntity.knockback: of the strength given, and resistance takes a part of it instead of being a chance. */
    @Inject(method = "knockBack", at = @At("HEAD"), cancellable = true)
    private void modernposes$knockback(Entity from, float damage, double x, double z, CallbackInfo ci) {
        ci.cancel();
        Combat.knockback((EntityLivingBase) (Object) this, 0.4D, x, z);
    }
}
