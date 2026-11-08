package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.grihail.modernposes.api.OffhandItem;
import com.grihail.modernposes.hands.Hands;
import com.grihail.modernposes.hands.OffhandSlot;

/** The off hand's slot in the player's inventory, and shift-click into it for the items that belong there. */
@Mixin(ContainerPlayer.class)
public abstract class MixinContainerPlayer extends Container {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void modernposes$slot(InventoryPlayer inventory, boolean local, EntityPlayer player, CallbackInfo ci) {
        addSlotToContainer(new OffhandSlot(Hands.of(player)));
    }

    @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true)
    private void modernposes$shiftToOffhand(EntityPlayer player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 9 || index >= OffhandSlot.INDEX) {
            return;
        }
        Slot from = (Slot) this.inventorySlots.get(index);
        Slot off = (Slot) this.inventorySlots.get(OffhandSlot.INDEX);
        if (from == null || !from.getHasStack()
            || off.getHasStack()
            || !(from.getStack()
                .getItem() instanceof OffhandItem)) {
            return;
        }
        ItemStack stack = from.getStack();
        ItemStack before = stack.copy();
        if (!mergeItemStack(stack, OffhandSlot.INDEX, OffhandSlot.INDEX + 1, false)) {
            return;
        }
        if (stack.stackSize == 0) {
            from.putStack(null);
        } else {
            from.onSlotChanged();
        }
        from.onPickupFromSlot(player, stack);
        cir.setReturnValue(before);
    }
}
