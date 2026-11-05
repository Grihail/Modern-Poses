package com.grihail.modernposes.mixins;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.grihail.modernposes.Sizes;

@Mixin(Entity.class)
public abstract class MixinEntity {

    /** Vanilla shrinks the water test box by 0.4 vertically, which inverts low boxes (swimming 0.6, small poses). */
    @ModifyConstant(method = "handleWaterMovement", constant = @Constant(doubleValue = -0.4000000059604645D))
    private double modernposes$waterInset(double vanilla) {
        return Sizes.waterInset((Entity) (Object) this, vanilla);
    }
}
