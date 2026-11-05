package com.grihail.modernposes.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.grihail.modernposes.PoseState;
import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.server.ServerPoses;

/**
 * Entry points for other mods. Modern Poses owns the player's hitbox and eye height: the pose (stand, sneak, swim,
 * crawl...) is decided by the own client and checked by the server; a {@link PoseProvider} may change the sizes.
 */
public final class ModernPoses {

    private static final List<PoseProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private ModernPoses() {}

    /** Adds a provider of pose sizes (call during init). */
    public static void register(PoseProvider provider) {
        PROVIDERS.add(provider);
    }

    /** The poses of a player on its logical side. */
    public static PoseSet posesOf(EntityPlayer p) {
        for (PoseProvider pr : PROVIDERS) {
            PoseSet s = pr.posesFor(p);
            if (s != null) return s;
        }
        return PoseSet.VANILLA;
    }

    /**
     * Server: the player's poses changed (another body). Goes back to the stand pose of the new set, applies it and
     * tells every client, the player's own included. No-op on the client.
     */
    public static void refresh(EntityPlayer p) {
        if (p instanceof EntityPlayerMP && !p.worldObj.isRemote) ServerPoses.refresh((EntityPlayerMP) p);
    }

    /** Current pose of a player, or null if not known yet (then it has the vanilla 0.6 x 1.8 box). */
    public static PoseSize pose(Entity e) {
        PoseState s = PoseState.get(e);
        return s == null ? null : s.pose;
    }

    /** Name of the current pose ("stand" when not known). */
    public static String poseName(Entity e) {
        PoseSize p = pose(e);
        return p == null ? "stand" : p.name;
    }

    /** Hitbox height of the current pose, blocks. */
    public static float poseHeight(Entity e) {
        PoseSize p = pose(e);
        return p == null ? 1.8F : p.height;
    }

    /** Lying down: swimming or crawling. */
    public static boolean isLying(Entity e) {
        PoseSize p = pose(e);
        return p != null && p.isHorizontal();
    }

    /** Client: 0..1 blend into the lying render pose (like modern swimAmount). */
    public static float swimAmount(Entity e, float partial) {
        PoseState s = PoseState.get(e);
        return s == null ? 0F : s.swimAmount(partial);
    }

    /** Client, own player: sprint-swimming right now. */
    public static boolean isSprintSwimming(Entity e) {
        PoseState s = PoseState.get(e);
        return s != null && s.swimming;
    }

    /** Would this pose's box, at the player's feet and center, fit among the blocks. */
    public static boolean fits(EntityPlayer p, PoseSize pose, double slack) {
        return Sizes.fits(p, pose, slack);
    }
}
