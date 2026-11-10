package com.grihail.modernposes.mixins.client;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.HandClient;
import com.grihail.modernposes.hands.HandUse;
import com.grihail.modernposes.mixins.AccessorC08;

/** Feature offhand. */
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClientOffhand {

    /** A use by the off hand says so in its own packet, so that it stays in order with the other uses. */
    @Inject(method = "addToSendQueue", at = @At("HEAD"))
    private void modernposes$markOffhand(Packet packet, CallbackInfo ci) {
        if (HandClient.isOffPass() && packet instanceof C08PacketPlayerBlockPlacement) {
            AccessorC08 a = (AccessorC08) packet;
            a.modernposes$setHitX(a.modernposes$getHitX() + HandUse.OFF_MARK);
        }
    }
}
