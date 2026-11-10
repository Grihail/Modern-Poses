package com.grihail.modernposes.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.grihail.modernposes.combat.ArmorRules;

/** Feature armor. */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerArmor {

    /** 26.3's armor for a player; Forge's method has 1.7.10's formula inside. */
    @Redirect(
        method = "damageEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/ISpecialArmor$ArmorProperties;ApplyArmor(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;D)F",
            remap = false))
    private float modernposes$armor(EntityLivingBase self, ItemStack[] armor, DamageSource source, double damage) {
        return ArmorRules.applyPlayer(self, armor, source, damage);
    }
}
