package com.grihail.modernposes.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Placing a half slab next to the own player with a low hitbox (sneaking 1.5, a crawl or a body's own poses).
 * <p>
 * Before sending the placement the client asks ItemBlock.func_150936_a, which tests the new block's collision box
 * against every entity, the own player included. For a slab that box comes from the slab block's current bounds,
 * not from the half that will be placed: an empty cell counts as a bottom slab, so a top slab right above a 1.5 high
 * box (y + 1.5 .. y + 2, free) was refused because the bottom half (y + 1 .. y + 1.5) touches the head. Vanilla never
 * hits this (the player is always 1.8 high). The server does not test the placing player at all.
 * <p>
 * When the vanilla check refuses a slab, this one repeats it with the half that onBlockPlaced will choose (from the
 * clicked side and the hit height).
 */
public final class SlabPlacement {

    private SlabPlacement() {}

    /** Client check of PlayerControllerMP.onPlayerRightClick, with the hit point the vanilla check does not get. */
    public static boolean canPlace(ItemBlock item, World w, int x, int y, int z, int side, EntityPlayer p,
        ItemStack stack, Vec3 hit) {
        if (item.func_150936_a(w, x, y, z, side, p, stack)) return true;
        if (!(item instanceof ItemSlab) || hit == null) return false;
        Block slab = item.field_150939_a;
        if (!(slab instanceof BlockSlab) || slab.isOpaqueCube()) return false; // double slabs are full blocks
        float hx = (float) hit.xCoord - x, hy = (float) hit.yCoord - y, hz = (float) hit.zCoord - z;
        // the cell the block goes into, like ItemBlock.func_150936_a
        Block at = w.getBlock(x, y, z);
        if (at == Blocks.snow_layer) side = 1;
        else if (at != Blocks.vine && at != Blocks.tallgrass
            && at != Blocks.deadbush
            && !at.isReplaceable(w, x, y, z)) {
                if (side == 0) y--;
                if (side == 1) y++;
                if (side == 2) z--;
                if (side == 3) z++;
                if (side == 4) x--;
                if (side == 5) x++;
            }
        int meta = slab.onBlockPlaced(w, x, y, z, side, hx, hy, hz, item.getMetadata(stack.getItemDamage()));
        boolean top = (meta & 8) != 0;
        AxisAlignedBB half = AxisAlignedBB
            .getBoundingBox(x, y + (top ? 0.5 : 0.0), z, x + 1, y + (top ? 1.0 : 0.5), z + 1);
        return w.checkNoEntityCollision(half) && w.getBlock(x, y, z)
            .isReplaceable(w, x, y, z) && slab.canReplace(w, x, y, z, side, stack);
    }
}
