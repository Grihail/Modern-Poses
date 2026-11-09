package com.grihail.modernposes.api;

import net.minecraft.item.ItemStack;

/**
 * An item held forward and charged with the use key (26.3's KineticWeapon: the spears). Its holder's arm points it
 * forward ({@link ArmPose#SPEAR}, {@link SpearMotion#holdingArm}); the stages of the charge are timed by these.
 */
public interface KineticItem {

    /** Ticks to raise it (KineticWeapon.delayTicks). */
    int delayTicks(ItemStack stack);

    /** Ticks after the raise the dismounting lasts (the sway's end). */
    int dismountTicks(ItemStack stack);

    /** Ticks after the raise the knockback lasts (the lowering's end). */
    int knockbackTicks(ItemStack stack);

    /** Ticks after the raise the damage lasts (the raise back's end). */
    int damageTicks(ItemStack stack);

    /** How far it goes forward when raised, blocks (KineticWeapon.forwardMovement). */
    float forwardMovement(ItemStack stack);
}
