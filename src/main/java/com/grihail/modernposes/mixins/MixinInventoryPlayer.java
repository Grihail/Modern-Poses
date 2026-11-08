package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.hands.Hands;

/** A death drops the off hand with the rest of the inventory. */
@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    @Shadow
    public EntityPlayer player;

    @Inject(method = "dropAllItems", at = @At("TAIL"))
    private void modernposes$dropOffhand(CallbackInfo ci) {
        Hands h = Hands.of(this.player);
        ItemStack stack = h.get();
        if (stack != null) {
            this.player.func_146097_a(stack, true, false);
            h.set(null);
        }
    }
}
