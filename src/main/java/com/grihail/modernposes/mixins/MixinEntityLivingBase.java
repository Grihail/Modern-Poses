package com.grihail.modernposes.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.grihail.modernposes.api.StabItem;
import com.grihail.modernposes.combat.ArmorRules;
import com.grihail.modernposes.combat.Combat;
import com.grihail.modernposes.hands.Hands;

/** A stab lasts as long as its item says (26.3's SwingAnimation.duration), not 1.7.10's 6 ticks. */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

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

    /** LivingEntity.knockback: of the strength given, and resistance takes a part of it instead of being a chance. */
    @Inject(method = "knockBack", at = @At("HEAD"), cancellable = true)
    private void modernposes$knockback(Entity from, float damage, double x, double z, CallbackInfo ci) {
        ci.cancel();
        Combat.knockback((EntityLivingBase) (Object) this, 0.4D, x, z);
    }

    @Inject(method = "getArmSwingAnimationEnd", at = @At("HEAD"), cancellable = true)
    private void modernposes$stabLength(CallbackInfoReturnable<Integer> cir) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (!(self instanceof EntityPlayer) || Hands.of((EntityPlayer) self).swingOff) {
            return;
        }
        ItemStack held = ((EntityPlayer) self).inventory.getCurrentItem();
        if (held != null && held.getItem() instanceof StabItem) {
            cir.setReturnValue(Math.max(1, ((StabItem) held.getItem()).swingTicks(held)));
        }
    }
}
