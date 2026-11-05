package com.grihail.modernposes.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;

import com.grihail.modernposes.PoseState;
import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.api.ModernPoses;
import com.grihail.modernposes.api.PoseSet;
import com.grihail.modernposes.api.PoseSize;
import com.grihail.modernposes.net.CPose;
import com.grihail.modernposes.net.PoseNet;
import com.grihail.modernposes.net.SPose;

/**
 * Client pose logic, ported from the modern player:
 * <ul>
 * <li>wanted pose: swim while sprint-swimming, sneak while holding sneak (not when flying), a low "sprint" pose while
 * sprinting if the set has one, else stand;</li>
 * <li>if the wanted box does not fit, the set's fallback list is tried (stand, sneak, crawl): a 1.5..1.8 gap
 * forces sneaking, a lower one crawling;</li>
 * <li>sprint-swimming starts when sprinting with the eyes under water and continues while sprinting in water.</li>
 * </ul>
 * The own client decides and tells the server, which checks it (ServerPoses).
 */
public final class ClientPoses {

    private static final double FIT_SLACK = 1.0E-7;
    private static final int GROW_TICKS = 5;
    private static final float SWIM_BLEND = 0.09F, EYE_SMOOTH = 0.5F;

    private ClientPoses() {}

    /** Client tick END. */
    static void tick(Minecraft mc) {
        for (Map.Entry<Integer, PoseState> en : PoseState.map(true)
            .entrySet()) {
            PoseState s = en.getValue();
            s.prevSwimAmount = s.swimAmount;
            boolean lying = s.pose != null && s.pose.isHorizontal();
            s.swimAmount = lying ? Math.min(1F, s.swimAmount + SWIM_BLEND) : Math.max(0F, s.swimAmount - SWIM_BLEND);
        }
        EntityPlayerSP p = mc.thePlayer;
        PoseState s = PoseState.get(p);
        if (s == null || s.pose == null) return;
        updateEye(p, s);
        // lying in water (swimming, a one block deep channel) is no walk: no walk bobbing, like modern swimming
        if (p.isInWater() && s.pose.isHorizontal()) {
            p.cameraYaw = p.prevCameraYaw = 0F;
            p.cameraPitch = p.prevCameraPitch = 0F;
        }
        if (p.isPlayerSleeping()) return;
        PoseSet set = ModernPoses.posesOf(p);
        if (set == PoseSet.WAIT) return;

        updateSwimming(p, s, set);
        if (s.poseCooldown > 0) {
            s.poseCooldown--;
            return;
        }
        PoseSize next = resolve(p, set, wantedPose(p, s, set));
        if (next == null || next.name.equals(s.pose.name)) {
            s.growTicks = 0;
            return;
        }
        // Shrinking happens at once. In water growing waits until the bigger box fitted for a few ticks in a row,
        // otherwise bobbing next to a block edge flips between poses every tick (fits / does not fit); on land the
        // box grows at once, together with the model.
        boolean grows = next.height > s.pose.height || next.width > s.pose.width;
        if (grows && !p.isRiding() && p.isInWater() && ++s.growTicks < GROW_TICKS) return;
        s.growTicks = 0;
        s.pose = next;
        Sizes.apply(p, next);
        PoseNet.NET.sendToServer(new CPose(next.name));
    }

    /** From the server: a pose of another player, a correction of ours, or one the server set (login, new set). */
    public static void onPose(SPose msg) {
        Minecraft mc = Minecraft.getMinecraft();
        PoseState s = PoseState.getOrCreate(true, msg.entityId);
        s.pose = msg.pose;
        if (mc.thePlayer != null && msg.entityId == mc.thePlayer.getEntityId()) {
            // a correction: take the server's pose and do not ask again right away
            s.poseCooldown = msg.correction ? 20 : 0;
            if (!msg.correction) s.swimming = false;
        }
        Entity e = mc.theWorld == null ? null : mc.theWorld.getEntityByID(msg.entityId);
        if (e instanceof EntityPlayer) Sizes.apply((EntityPlayer) e, s.pose);
    }

    private static void updateEye(EntityPlayerSP p, PoseState s) {
        float target = p.yOffset;
        if (Float.isNaN(s.eye)) s.eye = s.prevEye = target;
        s.prevEye = s.eye;
        s.eye += (target - s.eye) * EYE_SMOOTH;
        if (Math.abs(target - s.eye) < 0.001F) s.eye = target;
        // the eye glides down after the box shrank; only if a ceiling is already right above it (walking under a half
        // slab while the glide is not over), it jumps down to stay out of the block
        if (s.eye > target) {
            double top = p.boundingBox.minY + s.eye + 0.15;
            AxisAlignedBB above = AxisAlignedBB.getBoundingBox(
                p.boundingBox.minX + 0.05,
                p.boundingBox.minY + target,
                p.boundingBox.minZ + 0.05,
                p.boundingBox.maxX - 0.05,
                top,
                p.boundingBox.maxZ - 0.05);
            if (!p.worldObj.getCollidingBoundingBoxes(p, above)
                .isEmpty()) s.eye = s.prevEye = target;
        }
    }

    private static void updateSwimming(EntityPlayerSP p, PoseState s, PoseSet set) {
        boolean can = set.canSwim() && p.isSprinting() && !p.isRiding() && !p.capabilities.isFlying && p.isInWater();
        if (s.swimming) s.swimming = can;
        else s.swimming = can && p.isInsideOfMaterial(Material.water);
    }

    private static String wantedPose(EntityPlayerSP p, PoseState s, PoseSet set) {
        if (s.swimming) return "swim";
        if (p.isSneaking() && !p.capabilities.isFlying && !p.isRiding() && set.poses.containsKey("sneak"))
            return "sneak";
        // a set with a "sprint" pose runs low (a fox on four paws through one block holes); stopping keeps it while
        // the bigger poses do not fit (put "sprint" last in the fallback list)
        if (p.isSprinting() && !p.capabilities.isFlying && !p.isRiding() && set.poses.containsKey("sprint"))
            return "sprint";
        return "stand";
    }

    /** Wanted pose, or the first pose of the fallback chain after it that fits; null keeps the current pose. */
    static PoseSize resolve(EntityPlayer p, PoseSet set, String wanted) {
        List<String> order = new ArrayList<>();
        order.add(wanted);
        int i = set.fallback.indexOf(wanted);
        List<String> rest = i >= 0 ? set.fallback.subList(i + 1, set.fallback.size()) : set.fallback;
        for (String n : rest) if (!order.contains(n)) order.add(n);
        for (String n : order) {
            PoseSize pose = set.get(n);
            if (pose == null) continue;
            if (p.isRiding() || Sizes.fits(p, pose, FIT_SLACK)) return pose;
        }
        return null;
    }

    /**
     * Right after the movement input was read (EntityPlayerSP.onLivingUpdate): crawling is as slow as sneaking, and
     * double-tapping forward starts sprinting in water (vanilla 1.7.10 only allows that on the ground).
     */
    public static void afterMoveInput(EntityPlayerSP p) {
        PoseState s = PoseState.get(p);
        if (s == null || s.pose == null) return;
        // crawling, and sneaking forced by a low ceiling, are as slow as sneaking (the key already slows itself)
        // modern: only out of water (in water a low gap means swimming, which is not slowed down)
        if (("crawl".equals(s.pose.name) && !p.isInWater() || "sneak".equals(s.pose.name)) && !p.movementInput.sneak) {
            p.movementInput.moveStrafe *= 0.3F;
            p.movementInput.moveForward *= 0.3F;
        }
        boolean forward = p.movementInput.moveForward >= 0.8F;
        PoseSet set = ModernPoses.posesOf(p);
        if (set.canSwim() && !p.onGround && p.isInWater() && p.isInsideOfMaterial(Material.water)) {
            boolean food = p.getFoodStats()
                .getFoodLevel() > 6 || p.capabilities.allowFlying;
            if (!s.prevForward && forward
                && !p.isSprinting()
                && food
                && !p.isUsingItem()
                && !p.isPotionActive(Potion.blindness)) {
                if (s.sprintTimer > 0) p.setSprinting(true);
                else s.sprintTimer = 7;
            }
        }
        if (s.sprintTimer > 0) s.sprintTimer--;
        s.prevForward = forward;
    }

    /** Drawn crouching: sneak key, or a sneak pose forced by the ceiling. */
    public static boolean drawSneaking(EntityPlayer p) {
        PoseState s = PoseState.get(p);
        return p.isSneaking() || s != null && s.pose != null && "sneak".equals(s.pose.name);
    }

    /** Smoothed eye height of the own player for the camera, or NaN. */
    public static float eye(Entity e, float partial) {
        PoseState s = PoseState.get(e);
        if (s == null || Float.isNaN(s.eye)) return Float.NaN;
        return s.prevEye + (s.eye - s.prevEye) * partial;
    }
}
