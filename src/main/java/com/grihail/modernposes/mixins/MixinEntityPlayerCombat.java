package com.grihail.modernposes.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.combat.Combat;

/** Feature attackCooldown. */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerCombat {

    /** Attributes.ATTACK_SPEED, on the base of 4. */
    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    private void modernposes$attackSpeed(CallbackInfo ci) {
        ((EntityPlayer) (Object) this).getAttributeMap()
            .registerAttribute(Combat.ATTACK_SPEED);
    }

    /** 26.3's attack instead of 1.7.10's. */
    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"), cancellable = true)
    private void modernposes$attack(Entity target, CallbackInfo ci) {
        ci.cancel();
        Combat.attack((EntityPlayer) (Object) this, target);
    }
}
