package com.grihail.modernposes.hands;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * The off hand of a player: the one slot's stack (saved with the player) and what its use and swing are doing. The
 * other players' clients get the stack and the state from the server (HandsServer).
 */
public final class Hands implements IExtendedEntityProperties, IInventory {

    private static final String KEY = "modernposes.hands";
    private static final String TAG = "Offhand";

    private final EntityPlayer player;
    private ItemStack stack;

    /** The item in use is the off hand's. */
    private boolean usedOff;
    /** The swing going on is the off hand's. */
    public boolean swingOff;
    /** Client: ticks the swing flag waits for its swing to start. */
    public int swingGrace;
    /** Server: the tick an off hand use came in (its swing, if any, follows it). */
    public long offUseTick = -100;
    /** Server: what the clients that see this player last heard. */
    public ItemStack sentStack;
    public boolean sentUsedOff;
    /** Another player's entity on a client: the off hand's item was put in use from HandClient. */
    public boolean remoteUse;

    private Hands(EntityPlayer player) {
        this.player = player;
    }

    public static Hands of(EntityPlayer p) {
        Hands h = (Hands) p.getExtendedProperties(KEY);
        if (h == null) {
            h = new Hands(p);
            p.registerExtendedProperties(KEY, h);
        }
        return h;
    }

    public ItemStack get() {
        return this.stack;
    }

    public void set(ItemStack s) {
        this.stack = s;
    }

    /** The item in use is the off hand's. */
    public boolean usedOff() {
        return this.usedOff && this.player.isUsingItem();
    }

    /** What was last said of the use, whether or not the item is in use yet. */
    public boolean wantsOff() {
        return this.usedOff;
    }

    public void setUsedOff(boolean off) {
        this.usedOff = off;
    }

    // ------------------------------------------------------------------ saved with the player

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        if (this.stack != null) {
            compound.setTag(TAG, this.stack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        this.stack = compound.hasKey(TAG) ? ItemStack.loadItemStackFromNBT(compound.getCompoundTag(TAG)) : null;
    }

    @Override
    public void init(Entity entity, World world) {}

    // ------------------------------------------------------------------ the slot's inventory

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.stack;
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (this.stack == null) {
            return null;
        }
        ItemStack taken = this.stack.splitStack(Math.min(count, this.stack.stackSize));
        if (this.stack.stackSize <= 0) {
            this.stack = null;
        }
        return taken;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack s) {
        this.stack = s;
    }

    @Override
    public String getInventoryName() {
        return "container.modernposes.offhand";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUseableByPlayer(EntityPlayer p) {
        return p == this.player;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack s) {
        return true;
    }
}
