package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.grihail.modernposes.client.HandClient;
import com.grihail.modernposes.client.SlabPlacement;
import com.grihail.modernposes.combat.Combat;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

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

    /** The client's "can the block go there" test, with the half of a slab that will really be placed. */
    @Redirect(
        method = "onPlayerRightClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemBlock;func_150936_a(Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean modernposes$slab(ItemBlock item, World w, int x, int y, int z, int side, EntityPlayer p,
        ItemStack stack, EntityPlayer player, World world, ItemStack held, int cx, int cy, int cz, int cside,
        Vec3 hit) {
        return SlabPlacement.canPlace(item, w, x, y, z, side, p, stack, hit);
    }
}
