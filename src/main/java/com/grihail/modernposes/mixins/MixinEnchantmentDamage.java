package com.grihail.modernposes.mixins;

import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentDamage.class)
public abstract class MixinEnchantmentDamage {

    @Shadow
    @Final
    public int damageType;

    /** Sharpness: 1 + 0.5 per level above the first. Smite and Bane stay 2.5 a level, as 1.7.10's. */
    @Inject(method = "func_152376_a", at = @At("HEAD"), cancellable = true)
    private void modernposes$sharpness(int level, EnumCreatureAttribute type, CallbackInfoReturnable<Float> cir) {
        if (this.damageType == 0) {
            cir.setReturnValue(0.5F + 0.5F * level);
        }
    }

    /** Bane of Arthropods: Slowness IV for 1.5 s to 1.5 + 0.5 per level above the first. */
    @Inject(method = "func_151368_a", at = @At("HEAD"), cancellable = true)
    private void modernposes$bane(EntityLivingBase attacker, Entity target, int level, CallbackInfo ci) {
        ci.cancel();
        if (this.damageType == 2 && target instanceof EntityLivingBase
            && ((EntityLivingBase) target).getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
            float seconds = 1.5F + attacker.getRNG()
                .nextFloat() * 0.5F
                * (level - 1);
            ((EntityLivingBase) target)
                .addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, Math.round(seconds * 20.0F), 3));
        }
    }
}
