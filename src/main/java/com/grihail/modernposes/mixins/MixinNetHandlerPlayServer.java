package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C03PacketPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.server.ServerPoses;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP playerEntity;

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
