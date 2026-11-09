package com.grihail.modernposes.combat;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

import com.grihail.modernposes.ModernPosesMod;

/** 26.3's Sweeping Edge (Enchantments.SWEEPING_EDGE). */
public final class EnchantmentSweeping extends Enchantment {

    /** The first id tried; it stays the same between runs unless another mod has taken it. */
    private static final int ID = 96;

    public static EnchantmentSweeping instance;

    private EnchantmentSweeping(int id) {
        super(id, 2, EnumEnchantmentType.weapon);
        setName("modernposes.sweeping");
    }

    public static void register() {
        for (int id = ID; id < Enchantment.enchantmentsList.length; id++) {
            if (Enchantment.enchantmentsList[id] == null) {
                instance = new EnchantmentSweeping(id);
                Enchantment.addToBookList(instance);
                if (id != ID) {
                    ModernPosesMod.LOG.warn("Enchantment id {} is taken, Sweeping Edge has {}", ID, id);
                }
                return;
            }
        }
        ModernPosesMod.LOG.error("No free enchantment id for Sweeping Edge");
    }

    /** Attributes.SWEEPING_DAMAGE_RATIO of the enchantment: level / (level + 1). */
    public static float ratio(ItemStack stack) {
        if (instance == null || stack == null) {
            return 0.0F;
        }
        int level = EnchantmentHelper.getEnchantmentLevel(instance.effectId, stack);
        return level / (level + 1.0F);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 5 + (level - 1) * 9;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return 20 + (level - 1) * 9;
    }
}
