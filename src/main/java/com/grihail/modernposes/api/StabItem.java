package com.grihail.modernposes.api;

import net.minecraft.item.ItemStack;

/**
 * An item whose attack is a stab, not a swing (26.3's SwingAnimation of type STAB: the spears). Its swing lasts
 * {@link #swingTicks} instead of 1.7.10's 6 ticks, and the arm thrusts forward ({@link SpearMotion#stabArmX}).
 */
public interface StabItem {

    /** The stab's length, ticks (SwingAnimation.duration). */
    int swingTicks(ItemStack stack);
}
