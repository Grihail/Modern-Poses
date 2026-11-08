package com.grihail.modernposes.mixins.client;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** What ItemRenderer shows in the first person, to draw the off hand with its code. */
@Mixin(ItemRenderer.class)
public interface AccessorItemRenderer {

    @Accessor("itemToRender")
    ItemStack modernposes$getItemToRender();

    @Accessor("itemToRender")
    void modernposes$setItemToRender(ItemStack stack);

    @Accessor("equippedProgress")
    float modernposes$getEquippedProgress();

    @Accessor("equippedProgress")
    void modernposes$setEquippedProgress(float progress);

    @Accessor("prevEquippedProgress")
    float modernposes$getPrevEquippedProgress();

    @Accessor("prevEquippedProgress")
    void modernposes$setPrevEquippedProgress(float progress);
}
