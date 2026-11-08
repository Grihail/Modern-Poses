package com.grihail.modernposes.mixins;

import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The x of the hit, where a packet of the off hand carries its mark (HandUse.OFF_MARK). */
@Mixin(C08PacketPlayerBlockPlacement.class)
public interface AccessorC08 {

    @Accessor("field_149577_f")
    float modernposes$getHitX();

    @Accessor("field_149577_f")
    void modernposes$setHitX(float x);
}
