package com.grihail.modernposes.mixins.client;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.grihail.modernposes.client.SlabPlacement;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    /** The client's "can the block go there" test, with the half of a slab that will really be placed. */
    @Redirect(
        method = "onPlayerRightClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemBlock;func_150936_a(Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean modernposes$slab(ItemBlock item, World w, int x, int y, int z, int side, EntityPlayer p,
        ItemStack stack, EntityPlayer player, World world, ItemStack held, int cx, int cy, int cz, int cside,
        Vec3 hit) {
        return SlabPlacement.canPlace(item, w, x, y, z, side, p, stack, hit);
    }
}
