package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.combat.Combat;

/** Feature attackCooldown. */
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMPCombat {

    /** MultiPlayerGameMode.attack: an attack starts the charge over and dips the arm. */
    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void modernposes$attacked(EntityPlayer p, Entity target, CallbackInfo ci) {
        Combat.reset(p);
    }

    /** MultiPlayerGameMode.stopDestroyBlock: giving up on a block starts it over too. */
    @Inject(
        method = "resetBlockRemoving",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V"))
    private void modernposes$stoppedMining(CallbackInfo ci) {
        Combat.reset(Minecraft.getMinecraft().thePlayer);
    }
}
