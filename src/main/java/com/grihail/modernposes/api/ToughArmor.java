package com.grihail.modernposes.api;

import net.minecraft.item.ItemStack;

/** Armor with 26.3's armor toughness (a piece of diamond armor has 2, netherite 3). */
public interface ToughArmor {

    float armorToughness(ItemStack stack);
}
