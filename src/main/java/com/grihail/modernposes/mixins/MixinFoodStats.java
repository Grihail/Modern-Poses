package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 26.3's FoodData.tick: a full bar with saturation heals fast, at the cost of the saturation. */
@Mixin(FoodStats.class)
public abstract class MixinFoodStats {

    @Shadow
    private int foodLevel;
    @Shadow
    private float foodSaturationLevel;
    @Shadow
    private float foodExhaustionLevel;
    @Shadow
    private int foodTimer;
    @Shadow
    private int prevFoodLevel;

    @Shadow
    public abstract void addExhaustion(float amount);

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    private void modernposes$tick(EntityPlayer p, CallbackInfo ci) {
        ci.cancel();
        EnumDifficulty difficulty = p.worldObj.difficultySetting;
        this.prevFoodLevel = this.foodLevel;
        if (this.foodExhaustionLevel > 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else if (difficulty != EnumDifficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        boolean regen = p.worldObj.getGameRules()
            .getGameRuleBooleanValue("naturalRegeneration");
        if (regen && this.foodSaturationLevel > 0.0F && p.shouldHeal() && this.foodLevel >= 20) {
            this.foodTimer++;
            if (this.foodTimer >= 10) {
                float spent = Math.min(this.foodSaturationLevel, 6.0F);
                p.heal(spent / 6.0F);
                addExhaustion(spent);
                this.foodTimer = 0;
            }
        } else if (regen && this.foodLevel >= 18 && p.shouldHeal()) {
            this.foodTimer++;
            if (this.foodTimer >= 80) {
                p.heal(1.0F);
                addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            this.foodTimer++;
            if (this.foodTimer >= 80) {
                if (p.getHealth() > 10.0F || difficulty == EnumDifficulty.HARD
                    || p.getHealth() > 1.0F && difficulty == EnumDifficulty.NORMAL) {
                    p.attackEntityFrom(DamageSource.starve, 1.0F);
                }
                this.foodTimer = 0;
            }
        } else {
            this.foodTimer = 0;
        }
    }
}
