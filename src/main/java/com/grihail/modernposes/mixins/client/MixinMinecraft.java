package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.client.CombatClient;
import com.grihail.modernposes.client.HandClient;
import com.grihail.modernposes.hands.Hands;

/** The right click for each hand; the left click is the main hand's. */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    @Shadow
    private int leftClickCounter;

    @Shadow
    private void func_147121_ag() {}

    @Inject(method = "func_147121_ag", at = @At("HEAD"), cancellable = true)
    private void modernposes$rightClick(CallbackInfo ci) {
        if (HandClient.rightClick(this.thePlayer, () -> func_147121_ag())) {
            ci.cancel();
        }
    }

    @Inject(method = "func_147116_af", at = @At("HEAD"))
    private void modernposes$leftClick(CallbackInfo ci) {
        if (this.thePlayer != null) {
            Hands.of(this.thePlayer).swingOff = false;
            CombatClient.leftClick((Minecraft) (Object) this, this.thePlayer, this.leftClickCounter);
        }
    }
}
