package com.grihail.modernposes.combat;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ISpecialArmor;

import com.grihail.modernposes.api.ToughArmor;

/** 26.3's CombatRules, the armor's wear and the protection enchantments. */
public final class ArmorRules {

    private ArmorRules() {}

    /** The armor's toughness: 26.3 gives only diamond (of the materials 1.7.10 has) 2 a piece. */
    public static float toughness(ItemStack stack) {
        if (stack == null) {
            return 0.0F;
        }
        if (stack.getItem() instanceof ToughArmor) {
            return ((ToughArmor) stack.getItem()).armorToughness(stack);
        }
        return stack.getItem() instanceof ItemArmor
            && ((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.DIAMOND ? 2.0F : 0.0F;
    }

    /** CombatRules.getDamageAfterAbsorb. */
    public static float afterArmor(EntityLivingBase e, float damage) {
        float armor = e.getTotalArmorValue();
        float tough = 0.0F;
        for (ItemStack s : e.getLastActiveItems()) {
            tough += toughness(s);
        }
        float divider = 2.0F + tough / 4.0F;
        float real = MathHelper.clamp_float(armor - damage / divider, armor * 0.2F, 20.0F);
        return damage * (1.0F - real / 25.0F);
    }

    /**
     * ArmorProperties.ApplyArmor for a player. Armor with its own rules (ISpecialArmor) stays under Forge's; the rest
     * absorbs and wears by 26.3's (a quarter of the damage per piece, at least 1).
     */
    public static float applyPlayer(EntityLivingBase e, ItemStack[] armor, DamageSource source, double amount) {
        for (ItemStack s : armor) {
            if (s != null && s.getItem() instanceof ISpecialArmor) {
                return ISpecialArmor.ArmorProperties.ApplyArmor(e, armor, source, amount);
            }
        }
        float damage = (float) amount;
        if (source.isUnblockable()) {
            return damage;
        }
        float left = afterArmor(e, damage);
        int wear = Math.max(1, (int) (damage / 4.0F));
        for (int i = 0; i < armor.length; i++) {
            ItemStack s = armor[i];
            if (s != null && s.getItem() instanceof ItemArmor) {
                s.damageItem(wear, e);
                if (s.stackSize <= 0) {
                    armor[i] = null;
                }
            }
        }
        return left;
    }

    /** EnchantmentHelper.getDamageProtection: fixed points per level, no random part. */
    public static int protection(ItemStack[] items, DamageSource source) {
        int total = 0;
        for (ItemStack s : items) {
            NBTTagList list = s == null ? null : s.getEnchantmentTagList();
            if (list == null) {
                continue;
            }
            for (int i = 0; i < list.tagCount(); i++) {
                int id = list.getCompoundTagAt(i)
                    .getShort("id");
                int level = list.getCompoundTagAt(i)
                    .getShort("lvl");
                Enchantment e = id >= 0 && id < Enchantment.enchantmentsList.length ? Enchantment.enchantmentsList[id]
                    : null;
                if (e instanceof EnchantmentProtection) {
                    if (s.getItem() instanceof ItemArmor) {
                        total += points((EnchantmentProtection) e, level, source);
                    }
                } else if (e != null) {
                    total += e.calcModifierDamage(level, source);
                }
            }
        }
        return total;
    }

    private static int points(EnchantmentProtection e, int level, DamageSource source) {
        if (source.canHarmInCreative()) {
            return 0;
        }
        switch (e.protectionType) {
            case 0:
                return level;
            case 1:
                return source.isFireDamage() ? 2 * level : 0;
            case 2:
                return source == DamageSource.fall ? 3 * level : 0;
            case 3:
                return source.isExplosion() ? 2 * level : 0;
            case 4:
                return source.isProjectile() ? 2 * level : 0;
            default:
                return 0;
        }
    }
}
