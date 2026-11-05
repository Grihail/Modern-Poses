package com.grihail.modernposes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.Entity;

import com.grihail.modernposes.api.PoseSize;

/**
 * Pose state of a player. Two maps (logical client / logical server) keyed by entity id: the id survives respawn and
 * dimension change in 1.7.10, so the state is kept when Minecraft recreates the player entity.
 */
public final class PoseState {

    private static final Map<Integer, PoseState> CLIENT = new ConcurrentHashMap<>();
    private static final Map<Integer, PoseState> SERVER = new ConcurrentHashMap<>();

    /** Current pose; null = vanilla 0.6 x 1.8 box. */
    public PoseSize pose;

    // ---- client ----
    /** Own player: modern sprint-swimming is active. */
    public boolean swimming;
    /** 0..1 blend into the lying (swim / crawl) render pose, like modern swimAmount. */
    public float swimAmount, prevSwimAmount;
    /** Own player: smoothed eye height for the camera (NaN = not initialized). */
    public float eye = Float.NaN, prevEye = Float.NaN;
    /** Own player: ticks to wait before asking the server for a pose again after a rejection. */
    public int poseCooldown;
    /** Own player: ticks the wanted bigger pose has fitted in a row (in water). */
    public int growTicks;
    /** Own player: double-tap-forward sprint detection in water. */
    public boolean prevForward;
    public int sprintTimer;

    // ---- server ----
    /** Eye height still allowed until {@link #stanceGraceUntil} (ticksExisted) after a pose change. */
    public float stanceGraceEye;
    public int stanceGraceUntil;

    public static Map<Integer, PoseState> map(boolean client) {
        return client ? CLIENT : SERVER;
    }

    public static PoseState get(Entity e) {
        if (e == null || e.worldObj == null) return null;
        return map(e.worldObj.isRemote).get(e.getEntityId());
    }

    public static PoseState getOrCreate(boolean client, int entityId) {
        return map(client).computeIfAbsent(entityId, k -> new PoseState());
    }

    public float swimAmount(float partial) {
        return prevSwimAmount + (swimAmount - prevSwimAmount) * partial;
    }
}
