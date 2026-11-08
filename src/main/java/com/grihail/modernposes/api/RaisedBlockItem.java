package com.grihail.modernposes.api;

/**
 * An item that blocks by being raised in front of the body (26.3's BlocksAttacks: a shield), used with
 * EnumAction.block. Its holder gets {@link ArmPose#BLOCK}; 1.7.10's halving of the damage while blocking does not apply
 * (the item decides what it stops); it stays raised while the server updates its damage; and in the off hand it comes
 * before a sword's old blocking in the main hand.
 */
public interface RaisedBlockItem {
}
