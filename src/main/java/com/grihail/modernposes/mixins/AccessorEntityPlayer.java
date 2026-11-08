package com.grihail.modernposes.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The stack in use and its count, for the off hand. */
@Mixin(EntityPlayer.class)
public interface AccessorEntityPlayer {

    @Accessor("itemInUse")
    void modernposes$setItemInUse(ItemStack stack);

    @Accessor("itemInUseCount")
    void modernposes$setItemInUseCount(int count);
}
