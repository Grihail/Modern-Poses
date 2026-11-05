package com.grihail.modernposes.mixins.client;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.grihail.modernposes.Sizes;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

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
