package com.grihail.modernposes.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import com.grihail.modernposes.PoseState;
import com.grihail.modernposes.api.ModernPoses;

/**
 * Modern water movement for the own player (when its pose set has modern swimming). Replaces vanilla
 * moveEntityWithHeading while in water:
 * <ul>
 * <li>water drag 0.8, but 0.9 horizontally while sprinting (also at the surface, not only when sprint-swimming), so
 * a sprinting player makes headway against a current;</li>
 * <li>water gravity 0.005 per tick instead of 0.02, and none while sprinting;</li>
 * <li>sprint-swimming: vertical speed follows where the player looks (blend 0.06, or 0.085 when diving steeper
 * than -0.2), unless looking up at the surface without jumping;</li>
 * <li>the player climbs out at a ledge like vanilla.</li>
 * </ul>
 * The current pushes as in vanilla (0.014 per tick) only while the player does not steer.
 */
public final class SwimMovement {

    private static final double GRAVITY = 0.08;
    /** World.handleMaterialAcceleration: push of the current per tick. */
    private static final double PUSH = 0.014;

    /** Client tick when this movement last ran (F3 diagnostics). */
    public static long lastTick = -1;

    private SwimMovement() {}

    /** @return true if the movement was handled and vanilla must be skipped */
    public static boolean tick(EntityPlayerSP p, float strafe, float forward) {
        PoseState s = PoseState.get(p);
        if (s == null || s.pose == null || p.capabilities.isFlying || p.isRiding() || !p.isInWater()) return false;
        if (!ModernPoses.posesOf(p)
            .canSwim()) return false;

        lastTick = ClientProxy.ticks;
        double startY = p.posY;
        boolean falling = p.motionY <= 0.0;
        boolean sprinting = p.isSprinting();
        if (s.swimming) {
            Vec3 look = p.getLook(1.0F);
            double ly = look.yCoord;
            double blend = ly < -0.2 ? 0.085 : 0.06;
            double feet = p.boundingBox.minY;
            boolean waterAbove = p.worldObj
                .getBlock(
                    MathHelper.floor_double(p.posX),
                    MathHelper.floor_double(feet + 1.0 - 0.1),
                    MathHelper.floor_double(p.posZ))
                .getMaterial() == Material.water;
            if (ly <= 0.0 || p.movementInput.jump || waterAbove) p.motionY += (ly - p.motionY) * blend;
        }

        // while the player steers, the current neither slows nor speeds it up: the same speed as in still water.
        // Vanilla already added the push in handleWaterMovement this tick; take it back. Idle, the current carries.
        if (strafe != 0F || forward != 0F) {
            double[] flow = WaterFlow.flow(p);
            p.motionX -= flow[0] * PUSH;
            p.motionY -= flow[1] * PUSH;
            p.motionZ -= flow[2] * PUSH;
        }
        p.moveFlying(strafe, forward, 0.02F);
        p.moveEntity(p.motionX, p.motionY, p.motionZ);
        double drag = sprinting ? 0.9 : 0.8;
        p.motionX *= drag;
        p.motionY *= 0.8;
        p.motionZ *= drag;
        if (!sprinting) {
            double g = GRAVITY / 16.0;
            boolean settle = falling && Math.abs(p.motionY - 0.005) >= 0.003 && Math.abs(p.motionY - g) < 0.003;
            p.motionY = settle ? -0.003 : p.motionY - g;
        }
        if (p.isCollidedHorizontally
            && p.isOffsetPositionInLiquid(p.motionX, p.motionY + 0.6 - p.posY + startY, p.motionZ)) {
            p.motionY = 0.3;
        }
        // sprint-swimming does not stop when bumping into blocks (vanilla would stop sprinting next tick)
        if (s.swimming) p.isCollidedHorizontally = false;

        // limb swing, as at the end of vanilla moveEntityWithHeading
        p.prevLimbSwingAmount = p.limbSwingAmount;
        double dx = p.posX - p.prevPosX, dz = p.posZ - p.prevPosZ;
        float f = MathHelper.sqrt_double(dx * dx + dz * dz) * 4.0F;
        if (f > 1.0F) f = 1.0F;
        p.limbSwingAmount += (f - p.limbSwingAmount) * 0.4F;
        p.limbSwing += p.limbSwingAmount;
        return true;
    }
}
