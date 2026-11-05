package com.grihail.modernposes.client;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/** The water current that pushes the player this tick, computed like World.handleMaterialAcceleration (normalized). */
final class WaterFlow {

    private WaterFlow() {}

    static double[] flow(EntityPlayer p) {
        AxisAlignedBB bb = p.boundingBox.expand(0.0, com.grihail.modernposes.Sizes.waterInset(p, -0.4), 0.0)
            .contract(0.001, 0.001, 0.001);
        Vec3 v = Vec3.createVectorHelper(0, 0, 0);
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX + 1); x++)
            for (int y = MathHelper.floor_double(bb.minY); y < MathHelper.floor_double(bb.maxY + 1); y++)
                for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ + 1); z++) {
                    Block b = p.worldObj.getBlock(x, y, z);
                    if (b.getMaterial() == Material.water) b.velocityToAddToEntity(p.worldObj, x, y, z, p, v);
                }
        if (v.lengthVector() > 0) v = v.normalize();
        return new double[] { v.xCoord, v.yCoord, v.zCoord };
    }
}
