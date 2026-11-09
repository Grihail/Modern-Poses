package com.grihail.modernposes.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemHoe.class)
public interface AccessorItemHoe {

    @Accessor("theToolMaterial")
    Item.ToolMaterial modernposes$getMaterial();
}
