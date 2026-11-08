package com.grihail.modernposes.mixins.client;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderer.class)
public abstract class MixinEffectRenderer {

    /**
     * renderWorld draws the lit particles (layer 3: campfire smoke and other mods' custom particles) before
     * renderParticles, which is the only place interpPos is set, so they are placed against the previous frame's
     * camera. A pose change moves the own player's posY (the eye) at once, and that one frame threw them up or down.
     * Set interpPos here the same way renderParticles does.
     */
    @Inject(method = "renderLitParticles", at = @At("HEAD"), require = 1)
    private void modernposes$interpPos(Entity e, float partial, CallbackInfo ci) {
        EntityFX.interpPosX = e.lastTickPosX + (e.posX - e.lastTickPosX) * partial;
        EntityFX.interpPosY = e.lastTickPosY + (e.posY - e.lastTickPosY) * partial;
        EntityFX.interpPosZ = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partial;
    }
}
