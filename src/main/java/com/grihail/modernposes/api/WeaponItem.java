package com.grihail.modernposes.api;

import net.minecraft.item.ItemStack;

/** An item with its own place in 26.3's melee combat. */
public interface WeaponItem {

    /** What the item adds to the attack speed of 4 (26.3's attack_speed modifier: a sword's is -2.4). */
    double attackSpeedModifier();

    /** The item makes sweeping attacks (26.3's item tag {@code swords}). */
    default boolean sweeps(ItemStack stack) {
        return false;
    }
}
