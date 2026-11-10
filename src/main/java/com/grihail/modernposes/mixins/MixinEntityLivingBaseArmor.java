package com.grihail.modernposes.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.grihail.modernposes.combat.ArmorRules;

/** Feature armor. */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBaseArmor {

    @Shadow
    protected abstract void damageArmor(float damage);

    /** CombatRules.getDamageAfterAbsorb, for everything but players (those go through Forge's ApplyArmor). */
    @Inject(method = "applyArmorCalculations", at = @At("HEAD"), cancellable = true)
    private void modernposes$armor(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        if (source.isUnblockable()) {
            cir.setReturnValue(damage);
            return;
        }
        float left = ArmorRules.afterArmor((EntityLivingBase) (Object) this, damage);
        damageArmor(damage);
        cir.setReturnValue(left);
    }

    /** EnchantmentHelper.getDamageProtection. */
    @Redirect(
        method = "applyPotionDamageCalculations",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;getEnchantmentModifierDamage([Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;)I"))
    private int modernposes$protection(ItemStack[] items, DamageSource source) {
        return ArmorRules.protection(items, source);
    }
}
