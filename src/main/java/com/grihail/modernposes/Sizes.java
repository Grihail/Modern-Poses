package com.grihail.modernposes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

import com.grihail.modernposes.api.PoseSize;

/** Hitbox and eye height of players. Free of client-only classes (loads on a dedicated server). */
public final class Sizes {

    /** Vanilla teleports (S08) put the client's posY at feet + this. */
    public static final float VANILLA_EYE = 1.62F;
    /** Vanilla "Illegal stance" upper limit (eye above the feet); raised per player for tall poses. */
    public static final float VANILLA_MAX_STANCE = 1.65F;

    private Sizes() {}

    /** True if the player's hitbox is set by a pose. */
    public static boolean hasPose(Entity e) {
        PoseState s = PoseState.get(e);
        return s != null && s.pose != null;
    }

    /** Box of {@code pose} standing at the player's current feet and center. */
    public static AxisAlignedBB boxFor(EntityPlayer p, PoseSize pose) {
        float w = pose == null ? 0.6F : pose.width, h = pose == null ? 1.8F : pose.height;
        double minY = p.boundingBox.minY;
        return AxisAlignedBB
            .getBoundingBox(p.posX - w / 2, minY, p.posZ - w / 2, p.posX + w / 2, minY + h, p.posZ + w / 2);
    }

    /** Would the pose's box at the current feet collide with blocks. {@code slack} shrinks the box (tolerance). */
    public static boolean fits(EntityPlayer p, PoseSize pose, double slack) {
        AxisAlignedBB box = boxFor(p, pose).contract(slack, slack, slack);
        return p.worldObj.getCollidingBoundingBoxes(p, box)
            .isEmpty();
    }

    /**
     * Applies the pose hitbox and eye height (or the vanilla size for {@code pose == null}) so both sides collide
     * identically. Keeps boundingBox.minY (the feet stay where they are).
     * <p>
     * 1.7.10 pitfalls: for the client's own player (EntityPlayerSP) posY is the eye and yOffset is the eye height;
     * on the server and for other players posY is the feet and yOffset is 0. Forge's eyeHeight field keeps its
     * vanilla default for the own player (getEyeHeight is used relative to posY there).
     */
    public static void apply(EntityPlayer p, PoseSize pose) {
        if (p.isPlayerSleeping()) return; // vanilla sets the 0.2 sleeping box itself
        boolean posIsEye = ModernPosesMod.proxy.isLocalPlayer(p);
        float w = pose == null ? 0.6F : pose.width;
        float h = pose == null ? 1.8F : pose.height;
        float yOff = posIsEye ? (pose == null ? VANILLA_EYE : pose.eye) : 0F;
        float eye = (pose == null || posIsEye) ? p.getDefaultEyeHeight() : pose.eye;

        if (p.width == w && p.height == h && p.yOffset == yOff && p.eyeHeight == eye) return;

        if (!p.worldObj.isRemote) { // the client may still send the old eye height for a few ticks
            PoseState s = PoseState.get(p);
            if (s != null) {
                s.stanceGraceEye = Math.max(p.eyeHeight, s.stanceGraceUntil > p.ticksExisted ? s.stanceGraceEye : 0F);
                s.stanceGraceUntil = p.ticksExisted + 40;
            }
        }
        double minY = p.boundingBox.minY;
        double oldY = p.posY;
        p.width = w;
        p.height = h;
        p.yOffset = yOff;
        p.eyeHeight = eye;
        // setPosition rebuilds the bounding box from posY - yOffset with the new width/height
        p.setPosition(p.posX, minY + yOff, p.posZ);
        // keep the interpolation base consistent with the shifted posY (the camera smooths the eye separately)
        double dy = p.posY - oldY;
        p.prevPosY += dy;
        p.lastTickPosY += dy;
    }

    /**
     * Before vanilla movement of the own player. Vanilla sneaking sets ySize = 0.2 to lower the camera; poses lower
     * the eye instead, and the server kicks for "Illegal stance" when (yOffset - ySize) is below 0.1, which small
     * poses would hit.
     */
    public static void beforeMove(EntityPlayer p) {
        if (!p.worldObj.isRemote || !ModernPosesMod.proxy.isLocalPlayer(p) || !hasPose(p)) return;
        if (p.ySize != 0) {
            p.ySize = 0;
            p.posY = p.boundingBox.minY + p.yOffset;
        }
    }

    /** Upper "Illegal stance" limit for this player (vanilla 1.65; tall poses need more). */
    public static double maxStance(EntityPlayer p) {
        PoseState s = PoseState.get(p);
        PoseSize pose = s == null ? null : s.pose;
        double max = pose == null ? VANILLA_MAX_STANCE : Math.max(VANILLA_MAX_STANCE, pose.eye + 0.05F);
        if (s != null && s.stanceGraceUntil > p.ticksExisted) max = Math.max(max, s.stanceGraceEye + 0.05F);
        return max;
    }

    /**
     * Vertical inset of the water test box. Vanilla shrinks the box by 0.4 at top and bottom, which turns boxes lower
     * than 0.8 inside out (a 0.6 swimming box would never be "in water"). Scaled down for low boxes.
     */
    public static double waterInset(Entity e, double vanilla) {
        if (!(e instanceof EntityPlayer) || !hasPose(e)) return vanilla;
        return Math.min(Math.abs(vanilla), e.height * 0.25) * Math.signum(vanilla);
    }

    /** Client: a vanilla teleport packet (S08) carries y = feet + 1.62. Turn it into this pose's posY (feet + eye). */
    public static double teleportY(EntityPlayer p, double y) {
        return hasPose(p) ? y - VANILLA_EYE + p.yOffset : y;
    }
}
