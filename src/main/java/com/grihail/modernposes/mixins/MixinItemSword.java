package com.grihail.modernposes.mixins;

import net.minecraft.item.ItemSword;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Multimap;
import com.grihail.modernposes.api.WeaponItem;
import com.grihail.modernposes.combat.WeaponStats;

/** Feature weaponStats. */
@Mixin(ItemSword.class)
public abstract class MixinItemSword {

    /** 4 + the material's bonus. */
    @Shadow
    private float field_150934_a;

    @Inject(method = "getItemAttributeModifiers", at = @At("RETURN"))
    private void modernposes$stats(CallbackInfoReturnable<Multimap> cir) {
        if (!((Object) this instanceof WeaponItem)) {
            WeaponStats.sword(cir.getReturnValue(), this.field_150934_a - 4.0F);
        }
    }
}
