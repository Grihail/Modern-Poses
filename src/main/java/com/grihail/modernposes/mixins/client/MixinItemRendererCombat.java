package com.grihail.modernposes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.grihail.modernposes.combat.Combat;

/** Feature attackCooldown. */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRendererCombat {

    /** FirstPersonHandsAndItems.tick: the arm comes back up with the cube of the charge. */
    @ModifyConstant(method = "updateEquippedItem", constant = @Constant(floatValue = 1.0F))
    private float modernposes$armHeight(float vanilla) {
        float scale = Combat.swapScale(Minecraft.getMinecraft().thePlayer, 1.0F);
        return scale * scale * scale;
    }
}
