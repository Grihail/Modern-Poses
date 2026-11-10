package com.grihail.modernposes.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.grihail.modernposes.api.StabItem;
import com.grihail.modernposes.hands.Hands;

/** A stab lasts as long as its item says (26.3's SwingAnimation.duration), not 1.7.10's 6 ticks. */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

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
