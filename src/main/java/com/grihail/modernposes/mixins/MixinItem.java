package com.grihail.modernposes.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Multimap;
import com.grihail.modernposes.Features;
import com.grihail.modernposes.api.WeaponItem;
import com.grihail.modernposes.combat.WeaponStats;

/** The hoe does not override these two in 1.7.10, and an added override would not get its obfuscated name. */
@Mixin(Item.class)
public abstract class MixinItem {

    @Inject(method = "getItemAttributeModifiers", at = @At("RETURN"))
    private void modernposes$speed(CallbackInfoReturnable<Multimap> cir) {
        Object self = this;
        if (self instanceof WeaponItem) {
            WeaponStats.speed(cir.getReturnValue(), ((WeaponItem) self).attackSpeedModifier());
        } else if (self instanceof ItemHoe && Features.weaponStats) {
            WeaponStats.hoe(cir.getReturnValue(), ((AccessorItemHoe) self).modernposes$getMaterial());
        }
    }

    /** ToolMaterial.applyToolProperties: Weapon(2), a tool loses 2 durability per hit. */
    @Inject(method = "hitEntity", at = @At("RETURN"), cancellable = true)
    private void modernposes$hoeHit(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker,
        CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ItemHoe && Features.weaponStats) {
            stack.damageItem(2, attacker);
            cir.setReturnValue(true);
        }
    }
}
