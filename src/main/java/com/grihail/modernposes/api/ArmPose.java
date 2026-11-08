package com.grihail.modernposes.api;

/**
 * What an arm of a player is doing with its hand (26.3's HumanoidModel.ArmPose, the ones 1.7.10's items have). Read it
 * with {@link ModernHands#armPose}; a mod that draws its own body animates its arms from this.
 */
public enum ArmPose {
    /** Nothing in the hand. */
    EMPTY,
    /** Holding an item. */
    ITEM,
    /** Raised in front of the body by a {@link RaisedBlockItem} (a shield). See {@link ArmAngles#blocking}. */
    BLOCK,
    /** Blocking the old way, with a sword held across the chest (1.7.10's EnumAction.block). */
    SWORD_BLOCK,
    /** Drawing a bow. See {@link ArmAngles#bowAim}. */
    BOW_AND_ARROW
}
