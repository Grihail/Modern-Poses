package com.grihail.modernposes.hands;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.grihail.modernposes.api.RaisedBlockItem;
import com.grihail.modernposes.mixins.AccessorEntityPlayer;

/**
 * Using the off hand's item with 1.7.10's code, which only knows the held item: for the length of the use the two
 * hands' stacks trade places, on the client (HandClient) and the server (HandsServer) alike. Also what keeps an item
 * in use while the hand holding it is not the selected slot.
 */
public final class HandUse {

    /** A C08 packet of the off hand has this added to its x hit (a byte of sixteenths; a real hit is at most 1). */
    public static final float OFF_MARK = 8.0F;

    private HandUse() {}

    /** Puts the off hand's stack in the main hand and the other way round; twice undoes it. */
    public static void swap(EntityPlayer p) {
        Hands h = Hands.of(p);
        ItemStack main = p.inventory.mainInventory[p.inventory.currentItem];
        p.inventory.mainInventory[p.inventory.currentItem] = h.get();
        h.set(main);
    }

    /**
     * EntityPlayer.onUpdate compares the selected stack with the one in use: this is the stack to compare, the hand's
     * that really holds it. A raised shield or anything in the off hand stays in use when the server replaces the
     * stack with a copy (its damage changed), as in 26.3's updatingUsingItem.
     */
    public static ItemStack handOfUse(EntityPlayer p) {
        Hands h = Hands.of(p);
        ItemStack hand = h.usedOff() ? h.get() : p.inventory.getCurrentItem();
        ItemStack use = p.getItemInUse();
        if (hand != null && use != null
            && hand != use
            && hand.getItem() == use.getItem()
            && (h.usedOff() || hand.getItem() instanceof RaisedBlockItem)) {
            ((AccessorEntityPlayer) p).modernposes$setItemInUse(hand);
        }
        return hand;
    }
}
