package com.grihail.modernposes.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Multimap;
import com.grihail.modernposes.api.WeaponItem;
import com.grihail.modernposes.combat.WeaponStats;

@Mixin(ItemTool.class)
public abstract class MixinItemTool {

    @Shadow
    protected Item.ToolMaterial toolMaterial;

    @Inject(method = "getItemAttributeModifiers", at = @At("RETURN"))
    private void modernposes$stats(CallbackInfoReturnable<Multimap> cir) {
        Object self = this;
        if (!(self instanceof WeaponItem)) {
            WeaponStats.tool(cir.getReturnValue(), (ItemTool) self, this.toolMaterial);
        }
    }
}
