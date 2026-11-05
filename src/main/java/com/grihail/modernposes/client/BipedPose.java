package com.grihail.modernposes.client;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import com.grihail.modernposes.PoseState;

/**
 * The player model in modern poses, applied on top of vanilla angles in ModelBiped.setRotationAngles. Every
 * ModelBiped drawn for a player runs through it, so vanilla armor and modded armor models that call
 * super.setRotationAngles follow the same pose.
 * <ul>
 * <li>sneaking: legs on the ground, body leaned, head down inside the 1.5 high box (1.7.10 lifts the legs instead and
 * lowers the whole player);</li>
 * <li>swimming / crawling: the stroke (arms sweep forward over the head, out to the sides and back, a 26-step cycle
 * over limbSwing, legs flutter) and the body turned to lie (after RenderPlayer.rotateCorpse);</li>
 * <li>gliding (26.3's fall flying): the body along the flight (tilted by the pitch, turned toward the movement), the
 * head up at 45 degrees, arms and legs swinging the less the faster the flight (HumanoidModel, AvatarRenderer).</li>
 * </ul>
 */
public final class BipedPose {

    private static final float PI = (float) Math.PI;
    /** Sneak model: body and arms go down by this (px), the head pivot sits here (top of the head ~1.52 blocks). */
    private static final float SNEAK_DROP = 5F, SNEAK_HEAD = 6F;

    /** Set by RenderPlayer before drawing (setRotationAngles does not get partial ticks). */
    public static float partialTicks;
    /** First person arm: no pose there. */
    public static boolean firstPerson;

    /** Rest pivots (body y, right arm y, left arm y) of each model, read before this class first moves them. */
    private static final Map<ModelBiped, float[]> BASE = new WeakHashMap<>();

    private BipedPose() {}

    public static void afterSetRotationAngles(ModelBiped m, float limbSwing, Entity e) {
        if (firstPerson || !(e instanceof EntityPlayer)) return;
        PoseState s = PoseState.get(e);
        if (s == null) return;
        sneak(m);
        if (s.pose != null && s.pose.isFallFlying()) {
            // HumanoidModel.setupAnim: the head looks forward along the flight whatever the pitch
            m.bipedHead.rotateAngleX = -PI / 4F;
            m.bipedHeadwear.rotateAngleX = m.bipedHead.rotateAngleX;
            return; // 26.3 draws no stroke while gliding (swimAmount only grows when visually swimming)
        }
        applySwim(m, limbSwing, s);
    }

    /**
     * setRotationAngles' limb swing amount: 26.3 divides the swing of arms and legs by speedValue, which while gliding
     * is (speed^2 / 0.2)^3, at least 1 (HumanoidMobRenderer), so a fast flight keeps the limbs still.
     */
    public static float limbAmount(Entity e, float amount) {
        if (firstPerson || !(e instanceof EntityPlayer)) return amount;
        PoseState s = PoseState.get(e);
        if (s == null || s.pose == null || !s.pose.isFallFlying()) return amount;
        float speed = (float) (s.moveX * s.moveX + s.moveY * s.moveY + s.moveZ * s.moveZ) / 0.2F;
        speed = speed * speed * speed;
        return speed < 1.0F ? amount : amount / speed;
    }

    /** Players whose sneak model is drawn by {@link #sneak} (on the ground, no vanilla 0.125 lowering). */
    public static boolean groundedSneak(EntityPlayer p) {
        return PoseState.get(p) != null;
    }

    /** Every value it touches is set every frame (vanilla does not reset the arm and body pivots itself). */
    private static void sneak(ModelBiped m) {
        float[] base = BASE.get(m);
        if (base == null) { // vanilla never moves these, so this is the model's own rest value
            base = new float[] { m.bipedBody.rotationPointY, m.bipedRightArm.rotationPointY,
                m.bipedLeftArm.rotationPointY };
            BASE.put(m, base);
        }
        float drop = m.isSneak ? SNEAK_DROP : 0F;
        m.bipedBody.rotationPointY = base[0] + drop;
        m.bipedRightArm.rotationPointY = base[1] + drop;
        m.bipedLeftArm.rotationPointY = base[2] + drop;
        if (!m.isSneak) return;
        m.bipedHead.rotationPointY = SNEAK_HEAD;
        m.bipedHeadwear.rotationPointY = SNEAK_HEAD;
        m.bipedRightLeg.rotationPointY = 12F;
        m.bipedLeftLeg.rotationPointY = 12F;
        m.bipedRightLeg.rotationPointZ = 4F;
        m.bipedLeftLeg.rotationPointZ = 4F;
    }

    private static void applySwim(ModelBiped m, float limbSwing, PoseState s) {
        float a = s.swimAmount(partialTicks);
        if (a <= 0F) return;

        m.bipedHead.rotateAngleX = rotlerp(a, m.bipedHead.rotateAngleX, -PI / 4F);
        float t = limbSwing % 26.0F;
        boolean attacking = m.onGround > 0F;
        float ar = attacking ? 0F : a; // the attacking right arm keeps its swing
        float al = a;
        if (t < 14.0F) {
            float k = stroke(t) / stroke(14.0F);
            arm(m, ar, al, 0F, 0F, PI, PI, PI - 1.8707964F * k, PI + 1.8707964F * k);
        } else if (t < 22.0F) {
            float k = (t - 14.0F) / 8.0F;
            arm(m, ar, al, PI / 2F * k, PI / 2F * k, PI, PI, 1.2707963F + 1.8707964F * k, 5.012389F - 1.8707964F * k);
        } else {
            float k = (t - 22.0F) / 4.0F;
            arm(m, ar, al, PI / 2F - PI / 2F * k, PI / 2F - PI / 2F * k, PI, PI, PI, PI);
        }
        m.bipedLeftLeg.rotateAngleX = lerp(a, m.bipedLeftLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing / 3F + PI));
        m.bipedRightLeg.rotateAngleX = lerp(a, m.bipedRightLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing / 3F));

        m.bipedHeadwear.rotateAngleX = m.bipedHead.rotateAngleX;
        m.bipedHeadwear.rotateAngleY = m.bipedHead.rotateAngleY;
        m.bipedHeadwear.rotateAngleZ = m.bipedHead.rotateAngleZ;
    }

    private static void arm(ModelBiped m, float ar, float al, float rx, float lx, float ry, float ly, float rz,
        float lz) {
        m.bipedRightArm.rotateAngleX = lerp(ar, m.bipedRightArm.rotateAngleX, rx);
        m.bipedRightArm.rotateAngleY = lerp(ar, m.bipedRightArm.rotateAngleY, ry);
        m.bipedRightArm.rotateAngleZ = lerp(ar, m.bipedRightArm.rotateAngleZ, rz);
        m.bipedLeftArm.rotateAngleX = rotlerp(al, m.bipedLeftArm.rotateAngleX, lx);
        m.bipedLeftArm.rotateAngleY = rotlerp(al, m.bipedLeftArm.rotateAngleY, ly);
        m.bipedLeftArm.rotateAngleZ = rotlerp(al, m.bipedLeftArm.rotateAngleZ, lz);
    }

    /** Body rotation, called after RenderPlayer.rotateCorpse (before the model's scale(-1, -1, 1)). */
    public static void rotateCorpse(EntityPlayer p, float partial) {
        PoseState s = PoseState.get(p);
        if (s == null || p.isPlayerSleeping()) return;
        if (s.pose != null && s.pose.isFallFlying()) {
            flightRotation(p, s, partial);
            return;
        }
        float a = s.swimAmount(partial);
        if (a <= 0F) return;
        float pitch = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * partial;
        float target = p.isInWater() ? -90.0F - pitch : -90.0F;
        GL11.glRotatef(a * target, 1.0F, 0.0F, 0.0F);
        GL11.glTranslatef(0.0F, -a, 0.3F * a);
    }

    /**
     * AvatarRenderer.setupRotations while fall flying: tilted forward by scale * (-90 - pitch), then turned about the
     * body's up axis by the angle between the horizontal movement and the horizontal look (signed), so a turning
     * glider banks into the turn. The movement is the last two ticks' (lerped), as 26.3's deltaMovement.
     */
    private static void flightRotation(EntityPlayer p, PoseState s, float partial) {
        float pitch = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * partial;
        GL11.glRotatef(s.flyAmount(partial) * (-90.0F - pitch), 1.0F, 0.0F, 0.0F);
        Vec3 look = p.getLook(partial);
        double mx = s.prevMoveX + (s.moveX - s.prevMoveX) * partial;
        double mz = s.prevMoveZ + (s.moveZ - s.prevMoveZ) * partial;
        double moveSq = mx * mx + mz * mz, lookSq = look.xCoord * look.xCoord + look.zCoord * look.zCoord;
        if (moveSq > 1.0E-5F && lookSq > 1.0E-5F) {
            double dot = (mx * look.xCoord + mz * look.zCoord) / Math.sqrt(moveSq * lookSq);
            double sign = mx * look.zCoord - mz * look.xCoord;
            double yRot = Math.signum(sign) * Math.acos(Math.min(1.0, Math.abs(dot)));
            GL11.glRotatef((float) Math.toDegrees(yRot), 0.0F, 1.0F, 0.0F);
        }
    }

    private static float stroke(float t) {
        return -65.0F * t + t * t;
    }

    private static float lerp(float a, float from, float to) {
        return from + a * (to - from);
    }

    /** Lerp along the shorter way around the circle. */
    private static float rotlerp(float a, float from, float to) {
        float d = (to - from) % (2F * PI);
        if (d < -PI) d += 2F * PI;
        if (d >= PI) d -= 2F * PI;
        return from + a * d;
    }
}
