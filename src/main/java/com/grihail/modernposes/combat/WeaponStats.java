package com.grihail.modernposes.combat;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemTool;

import com.google.common.collect.Multimap;

/** 26.3's Items table: what weapons and tools add to the player's damage of 1 and attack speed of 4. */
public final class WeaponStats {

    private static final String DAMAGE = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();
    private static final String SPEED = Combat.ATTACK_SPEED.getAttributeUnlocalizedName();

    private WeaponStats() {}

    /** Properties.sword(material, 3, -2.4); {@code bonus} is the material's. */
    public static void sword(Multimap modifiers, float bonus) {
        set(modifiers, 3.0F + bonus, -2.4D);
    }

    /** Properties.axe, .pickaxe and .shovel. Tools of other kinds keep what they have. */
    public static void tool(Multimap modifiers, ItemTool tool, Item.ToolMaterial material) {
        float bonus = material.getDamageVsEntity();
        if (tool instanceof ItemAxe) {
            // wood and gold 6 + 0, stone 7 + 1, iron 6 + 2, diamond 5 + 3, netherite 5 + 4
            set(modifiers, bonus <= 0.0F ? 6.0F : Math.max(8.0F, 5.0F + bonus), axeSpeed(material));
        } else if (tool instanceof ItemPickaxe) {
            set(modifiers, 1.0F + bonus, -2.8D);
        } else if (tool instanceof ItemSpade) {
            set(modifiers, 1.5F + bonus, -3.0D);
        }
    }

    private static double axeSpeed(Item.ToolMaterial material) {
        if (material == Item.ToolMaterial.WOOD || material == Item.ToolMaterial.STONE) {
            return -3.2D;
        }
        return material == Item.ToolMaterial.IRON ? -3.1D : -3.0D;
    }

    /** Properties.hoe: its damage is always 0 (a baseline of minus the bonus), the speed grows with the material. */
    public static void hoe(Multimap modifiers, Item.ToolMaterial material) {
        speed(modifiers, -3.0D + Math.max(0, Math.min(3, material.getHarvestLevel())));
    }

    public static void speed(Multimap modifiers, double speed) {
        modifiers.removeAll(SPEED);
        modifiers.put(SPEED, new AttributeModifier(Combat.SPEED_MODIFIER, "Weapon modifier", speed, 0));
    }

    private static void set(Multimap modifiers, float damage, double speed) {
        modifiers.removeAll(DAMAGE);
        modifiers.put(DAMAGE, new AttributeModifier(Combat.DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
        speed(modifiers, speed);
    }
}
