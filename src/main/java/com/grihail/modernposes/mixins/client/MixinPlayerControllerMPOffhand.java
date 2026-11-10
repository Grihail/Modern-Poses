package com.grihail.modernposes.mixins.client;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.grihail.modernposes.client.HandClient;

/** Feature offhand: what a click did decides whether the off hand is tried next. */
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMPOffhand {

    /** The off hand does not act on entities (its packet could not say whose hand); it is then used in the air. */
    @Inject(method = "interactWithEntitySendPacket", at = @At("HEAD"), cancellable = true)
    private void modernposes$offNoEntity(EntityPlayer p, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (HandClient.isOffPass()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interactWithEntitySendPacket", at = @At("RETURN"))
    private void modernposes$entityDone(EntityPlayer p, Entity target, CallbackInfoReturnable<Boolean> cir) {
        HandClient.markConsumed(cir.getReturnValueZ());
    }

    @Inject(method = "onPlayerRightClick", at = @At("RETURN"))
    private void modernposes$blockDone(EntityPlayer p, World w, ItemStack s, int x, int y, int z, int side, Vec3 hit,
        CallbackInfoReturnable<Boolean> cir) {
        HandClient.markConsumed(cir.getReturnValueZ());
    }

    @Inject(method = "sendUseItem", at = @At("HEAD"), cancellable = true)
    private void modernposes$suppressUse(EntityPlayer p, World w, ItemStack s, CallbackInfoReturnable<Boolean> cir) {
        if (HandClient.suppressMainUse()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "sendUseItem", at = @At("RETURN"))
    private void modernposes$useDone(EntityPlayer p, World w, ItemStack s, CallbackInfoReturnable<Boolean> cir) {
        HandClient.markConsumed(cir.getReturnValueZ());
    }
}
