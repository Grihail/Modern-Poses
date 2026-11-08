package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.hands.HandUse;
import com.grihail.modernposes.hands.Hands;
import com.grihail.modernposes.hands.HandsServer;
import com.grihail.modernposes.server.ServerPoses;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP playerEntity;

    /** A use marked as the off hand's runs with the hands' stacks swapped (hands/HandUse). */
    @Inject(method = "processPlayerBlockPlacement", at = @At("HEAD"))
    private void modernposes$useBegin(C08PacketPlayerBlockPlacement packet, CallbackInfo ci) {
        AccessorC08 a = (AccessorC08) packet;
        if (a.modernposes$getHitX() >= HandUse.OFF_MARK) {
            a.modernposes$setHitX(a.modernposes$getHitX() - HandUse.OFF_MARK);
            HandsServer.begin(playerEntity);
        } else if (!playerEntity.isUsingItem()) {
            Hands.of(playerEntity)
                .setUsedOff(false);
        }
    }

    @Inject(method = "processPlayerBlockPlacement", at = @At("RETURN"))
    private void modernposes$useEnd(C08PacketPlayerBlockPlacement packet, CallbackInfo ci) {
        if (HandsServer.isActive()) {
            HandsServer.end(playerEntity);
        }
    }

    /** While the hands are swapped the hotbar is not what the client must see. */
    @Redirect(
        method = "processPlayerBlockPlacement",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;detectAndSendChanges()V"))
    private void modernposes$noDetect(Container container) {
        if (!HandsServer.isActive()) {
            container.detectAndSendChanges();
        }
    }

    @Redirect(
        method = "processPlayerBlockPlacement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void modernposes$noSlot(NetHandlerPlayServer self, Packet packet) {
        if (!HandsServer.isActive() || !(packet instanceof S2FPacketSetSlot)) {
            self.sendPacket(packet);
        }
    }

    /** The swing after an off hand's use is that hand's. */
    @Inject(method = "processAnimation", at = @At("HEAD"))
    private void modernposes$swing(C0APacketAnimation packet, CallbackInfo ci) {
        if (packet.func_149421_d() == 1) {
            HandsServer.onSwing(playerEntity);
        }
    }

    /** The creative screen sets slots up to 44; the off hand's is 45. */
    @Redirect(
        method = "processCreativeInventoryAction",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getHotbarSize()I"))
    private int modernposes$creativeSlots(C10PacketCreativeInventoryAction packet) {
        return InventoryPlayer.getHotbarSize() + 1;
    }

    /** Apply a pose change the client sent before this movement packet, so both sides move the same box. */
    @Inject(method = "processPlayer", at = @At("HEAD"))
    private void modernposes$pendingPose(C03PacketPlayer packet, CallbackInfo ci) {
        ServerPoses.drain(playerEntity);
    }

    /** "Illegal stance" kicks when the eye is more than 1.65 above the feet; tall poses need a higher limit. */
    @ModifyConstant(method = "processPlayer", constant = @Constant(doubleValue = 1.65D))
    private double modernposes$maxStance(double vanilla) {
        return Sizes.maxStance(playerEntity);
    }
}
