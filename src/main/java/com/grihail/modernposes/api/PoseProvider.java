package com.grihail.modernposes.api;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Gives the poses of a player (a mod with custom bodies: a tall bee, a small gecko). Registered with
 * {@link ModernPoses#register}; the first provider that does not return null wins, otherwise the player is the
 * modern vanilla player ({@link PoseSet#VANILLA}).
 * <p>
 * Called on both logical sides, often (every tick for the own player): cache the sets. Both sides must give the same
 * sizes for the same player, or the server rejects the client's poses. When the answer changes (another body), call
 * {@link ModernPoses#refresh} on the server.
 */
public interface PoseProvider {

    /** The player's poses, {@link PoseSet#WAIT} (client: data not here yet), or null (not handled). */
    PoseSet posesFor(EntityPlayer player);
}
