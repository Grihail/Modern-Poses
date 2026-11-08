package com.grihail.modernposes.mixins.client;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.client.HandClient;
import com.grihail.modernposes.hands.HandUse;
import com.grihail.modernposes.mixins.AccessorC08;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    /** A use by the off hand says so in its own packet, so that it stays in order with the other uses. */
    @Inject(method = "addToSendQueue", at = @At("HEAD"))
    private void modernposes$markOffhand(Packet packet, CallbackInfo ci) {
        if (HandClient.isOffPass() && packet instanceof C08PacketPlayerBlockPlacement) {
            AccessorC08 a = (AccessorC08) packet;
            a.modernposes$setHitX(a.modernposes$getHitX() + HandUse.OFF_MARK);
        }
    }

    /**
     * Server teleports (dismounting, /tp, "moved wrongly" resets, respawn) send y = feet + 1.62. Vanilla puts posY
     * there, which for a pose with another eye height leaves the feet in the air; the client then reports a position
     * the server never accepts, and the player is pulled back forever. Convert y to this pose's posY.
     */
    @Redirect(
        method = "handlePlayerPosLook",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;setPositionAndRotation(DDDFF)V"))
    private void modernposes$teleport(EntityClientPlayerMP p, double x, double y, double z, float yaw, float pitch) {
        p.setPositionAndRotation(x, Sizes.teleportY(p, y), z, yaw, pitch);
    }
}
