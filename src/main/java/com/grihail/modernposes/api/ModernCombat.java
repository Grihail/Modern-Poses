package com.grihail.modernposes.api;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;

import com.grihail.modernposes.combat.Combat;

/** 26.3's attack charge, for any mod. */
public final class ModernCombat {

    /** The player's attack speed attribute (hits per second at full charge). */
    public static final IAttribute ATTACK_SPEED = Combat.ATTACK_SPEED;

    private ModernCombat() {}

    /** How charged the next attack is, 0..1 (Player.getAttackStrengthScale). */
    public static float attackStrength(EntityPlayer player, float partial) {
        return Combat.strength(player, partial);
    }

    /** Ticks a full charge takes with what the player holds. */
    public static float attackDelay(EntityPlayer player) {
        return Combat.delay(player);
    }

    /** Starts the charge over (Player.resetAttackStrengthTicker). */
    public static void resetAttack(EntityPlayer player) {
        Combat.reset(player);
    }
}
