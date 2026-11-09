package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 26.3's Consumables.GOLDEN_APPLE and ENCHANTED_GOLDEN_APPLE. */
@Mixin(ItemAppleGold.class)
public abstract class MixinItemAppleGold {

    @Inject(method = "onFoodEaten", at = @At("HEAD"), cancellable = true)
    private void modernposes$effects(ItemStack stack, World world, EntityPlayer p, CallbackInfo ci) {
        ci.cancel();
        if (world.isRemote) {
            return;
        }
        if (stack.getItemDamage() > 0) {
            p.addPotionEffect(new PotionEffect(Potion.regeneration.id, 400, 1));
            p.addPotionEffect(new PotionEffect(Potion.resistance.id, 6000, 0));
            p.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 6000, 0));
            p.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 2400, 3));
        } else {
            p.addPotionEffect(new PotionEffect(Potion.regeneration.id, 100, 1));
            p.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 2400, 0));
        }
    }
}
