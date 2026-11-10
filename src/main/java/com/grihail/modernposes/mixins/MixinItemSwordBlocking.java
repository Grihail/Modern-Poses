package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Feature noSwordBlocking: a sword does not block in 26.3; that is the shield's. */
@Mixin(ItemSword.class)
public abstract class MixinItemSwordBlocking {

    @Inject(method = "getItemUseAction", at = @At("HEAD"), cancellable = true)
    private void modernposes$noBlockAction(ItemStack stack, CallbackInfoReturnable<EnumAction> cir) {
        cir.setReturnValue(EnumAction.none);
    }

    @Inject(method = "getMaxItemUseDuration", at = @At("HEAD"), cancellable = true)
    private void modernposes$noBlockTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    private void modernposes$noBlock(ItemStack stack, World world, EntityPlayer p,
        CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(stack);
    }
}
