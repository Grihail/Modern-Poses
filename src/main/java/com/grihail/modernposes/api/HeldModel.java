package com.grihail.modernposes.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Draws an item held in a hand as 26.3 does: as a model with display transforms. Registered with
 * {@link ModernHands#registerHeldModel}; such an item replaces 1.7.10's item-in-hand drawing in the first and third
 * person. Client only.
 * <p>
 * The matrix is 26.3's item space of the hand: {@code ItemInHandLayer} in the third person, after
 * {@code applyItemArmTransform} and the swing in the first. What is left to do is the model's own display transform
 * (for the left hand with 26.3's left hand fix: x translation, y and z rotation negated), and the model.
 */
public interface HeldModel {

    /**
     * @param holder      who holds it
     * @param left        in the left arm (26.3's *_lefthand transforms)
     * @param firstPerson in the first person view
     * @param using       it is the item in use (a shield: raised)
     */
    void render(ItemStack stack, EntityLivingBase holder, boolean left, boolean firstPerson, boolean using,
        float partialTicks);

    /**
     * True if the model moves itself during the attack swing (a spear's stab), so the first person's ordinary swing is
     * not applied; it reads the swing with {@link ModernHands#swingProgress}.
     */
    default boolean ownSwing(ItemStack stack) {
        return false;
    }

    /**
     * A {@link ScopeItem} in use, third person: drawn at the eye (PlayerItemInHandLayer.renderItemHeldToEye). The
     * matrix is at the head, after CustomHeadLayer's translateToHead and 2.5 px to the hand's side: what is left is
     * the model's head display transform and the model.
     */
    default void renderAtEye(ItemStack stack, EntityLivingBase holder, boolean left, float partialTicks) {}
}
