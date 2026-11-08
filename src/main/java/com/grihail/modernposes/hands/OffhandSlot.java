package com.grihail.modernposes.hands;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The off hand's slot in the player's inventory (index {@link #INDEX}, where 26.3 has it: x 77, y 62). */
public final class OffhandSlot extends Slot {

    /** The slot's index in ContainerPlayer (1.7.10's slots are 0 to 44). */
    public static final int INDEX = 45;

    public OffhandSlot(IInventory inventory) {
        super(inventory, 0, 77, 62);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getBackgroundIconIndex() {
        return com.grihail.modernposes.client.HandClient.emptySlot;
    }
}
