package com.grihail.modernposes.mixins.client;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.BipedPose;
import com.grihail.modernposes.client.ClientPoses;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", at = @At("HEAD"))
    private void modernposes$partial(AbstractClientPlayer player, double x, double y, double z, float yaw,
        float partialTicks, CallbackInfo ci) {
        BipedPose.partialTicks = partialTicks;
    }

    /** Sneak model also when the ceiling forces the sneak pose. */
    @Redirect(
        method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isSneaking()Z",
            ordinal = 0))
    private boolean modernposes$sneaking(AbstractClientPlayer player) {
        return ClientPoses.drawSneaking(player);
    }

    /**
     * Vanilla lowers other sneaking players by 0.125 because its sneak model lifts the legs by 3 px. Players in poses
     * get the sneak model that stands on the ground (BipedPose), so no lowering for them.
     */
    @Redirect(
        method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isSneaking()Z",
            ordinal = 1))
    private boolean modernposes$lowerSneaking(AbstractClientPlayer player) {
        return !BipedPose.groundedSneak(player) && player.isSneaking();
    }

    @Inject(method = "rotateCorpse(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", at = @At("TAIL"))
    private void modernposes$swimRotation(AbstractClientPlayer player, float age, float bodyYaw, float partialTicks,
        CallbackInfo ci) {
        BipedPose.rotateCorpse(player, partialTicks);
    }

    @Inject(method = "renderFirstPersonArm", at = @At("HEAD"))
    private void modernposes$armHead(EntityPlayer player, CallbackInfo ci) {
        BipedPose.firstPerson = true;
    }

    @Inject(method = "renderFirstPersonArm", at = @At("TAIL"))
    private void modernposes$armTail(EntityPlayer player, CallbackInfo ci) {
        BipedPose.firstPerson = false;
    }
}
