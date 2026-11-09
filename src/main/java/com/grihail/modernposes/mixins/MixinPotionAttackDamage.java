package com.grihail.modernposes.mixins;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionAttackDamage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** MobEffects: Strength +3 per level, Weakness -4 per level. */
@Mixin(PotionAttackDamage.class)
public abstract class MixinPotionAttackDamage {

    @Inject(method = "func_111183_a", at = @At("HEAD"), cancellable = true)
    private void modernposes$amount(int amplifier, AttributeModifier modifier, CallbackInfoReturnable<Double> cir) {
        boolean weakness = ((Potion) (Object) this).id == Potion.weakness.id;
        cir.setReturnValue((weakness ? -4.0D : 3.0D) * (amplifier + 1));
    }
}
