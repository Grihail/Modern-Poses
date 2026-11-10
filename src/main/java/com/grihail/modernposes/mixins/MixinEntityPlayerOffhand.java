package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.hands.HandUse;
import com.grihail.modernposes.hands.Hands;

/** Feature offhand. */
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayerOffhand {

    @Unique
    private boolean modernposes$finishOff;

    /** The stack the item in use is compared with: the hand's that holds it. */
    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;",
            ordinal = 0))
    private ItemStack modernposes$handOfUse(InventoryPlayer inventory) {
        return HandUse.handOfUse((EntityPlayer) (Object) this);
    }

    /** What an item in use leaves (an empty bottle, a bowl) goes to the hand that held it. */
    @Inject(method = "onItemUseFinish", at = @At("HEAD"))
    private void modernposes$finishBegin(CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        this.modernposes$finishOff = Hands.of(self)
            .usedOff();
        if (this.modernposes$finishOff) {
            HandUse.swap(self);
        }
    }

    @Inject(method = "onItemUseFinish", at = @At("RETURN"))
    private void modernposes$finishEnd(CallbackInfo ci) {
        if (this.modernposes$finishOff) {
            this.modernposes$finishOff = false;
            HandUse.swap((EntityPlayer) (Object) this);
        }
    }
}
