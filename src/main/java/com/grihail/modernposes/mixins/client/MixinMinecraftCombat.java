package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.CombatClient;

/** Feature attackCooldown: a left click at no entity starts the charge over. */
@Mixin(Minecraft.class)
public abstract class MixinMinecraftCombat {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    @Shadow
    private int leftClickCounter;

    @Inject(method = "func_147116_af", at = @At("HEAD"))
    private void modernposes$leftClick(CallbackInfo ci) {
        if (this.thePlayer != null) {
            CombatClient.leftClick((Minecraft) (Object) this, this.thePlayer, this.leftClickCounter);
        }
    }
}
