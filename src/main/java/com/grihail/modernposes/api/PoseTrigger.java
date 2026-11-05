package com.grihail.modernposes.api;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Asks for a pose of the own player (a mod's elytra: {@code "fall_flying"} while gliding). Registered with
 * {@link ModernPoses#registerTrigger}; the triggers are asked on the own client every tick, in the order they were
 * registered, before Modern Poses' own rules (sprint-swimming, sneaking, sprinting): the first name that the
 * player's {@link PoseSet} has wins. That pose still goes through the set's fallback when its box does not fit, and
 * the server checks it like any other.
 * <p>
 * Only the own player's client asks (other players' poses come from the server), so a trigger reads what that client
 * knows of the player.
 */
public interface PoseTrigger {

    /** Name of the wanted pose, or null (no wish: the next trigger or Modern Poses' rules decide). */
    String wantedPose(EntityPlayer player);
}
