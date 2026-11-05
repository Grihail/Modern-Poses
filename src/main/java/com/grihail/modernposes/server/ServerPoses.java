package com.grihail.modernposes.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.grihail.modernposes.PoseState;
import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.api.ModernPoses;
import com.grihail.modernposes.api.PoseSet;
import com.grihail.modernposes.api.PoseSize;
import com.grihail.modernposes.net.PoseNet;
import com.grihail.modernposes.net.SPose;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Pose changes requested by clients. The client decides its pose (like vanilla movement); the server checks that
 * the pose exists for the player and, if the hitbox grows, that it fits. A request is applied right before the
 * player's next movement packet (MixinNetHandlerPlayServer) or at the start of the next server tick, whichever comes
 * first. The request is read from the TCP stream before the movement packet that follows it, so it is always
 * applied first and both sides move the same box.
 */
public final class ServerPoses {

    /** Collision tolerance when growing (client and server positions differ by rounding). */
    private static final double SLACK = 0.01;

    private static final Map<Integer, String> PENDING = new ConcurrentHashMap<>();

    /** Netty thread. */
    public static void request(EntityPlayerMP p, String pose) {
        PENDING.put(p.getEntityId(), pose);
    }

    /** Main thread, right before processing a movement packet of {@code p}. */
    public static void drain(EntityPlayerMP p) {
        String pose = PENDING.remove(p.getEntityId());
        if (pose != null) apply(p, pose);
    }

    static void apply(EntityPlayerMP p, String name) {
        PoseState s = PoseState.get(p);
        if (s == null || s.pose == null || name.equals(s.pose.name)) return;
        PoseSize wanted = ModernPoses.posesOf(p)
            .get(name);
        boolean grows = wanted != null && (wanted.height > s.pose.height || wanted.width > s.pose.width);
        if (wanted == null || p.isPlayerSleeping() || grows && !Sizes.fits(p, wanted, SLACK)) {
            PoseNet.NET.sendTo(new SPose(p.getEntityId(), s.pose, true), p);
            return;
        }
        s.pose = wanted;
        Sizes.apply(p, wanted);
        for (EntityPlayerMP r : players()) if (r != p) PoseNet.NET.sendTo(new SPose(p.getEntityId(), wanted, false), r);
    }

    /** The player's poses changed: back to the stand pose of the new set, told to everyone. */
    public static void refresh(EntityPlayerMP p) {
        PoseState s = PoseState.getOrCreate(false, p.getEntityId());
        PoseSet set = ModernPoses.posesOf(p);
        if (set == PoseSet.WAIT) set = PoseSet.VANILLA;
        s.pose = set.stand();
        Sizes.apply(p, s.pose);
        PENDING.remove(p.getEntityId()); // a request made with the old poses
        SPose msg = new SPose(p.getEntityId(), s.pose, false);
        for (EntityPlayerMP r : players()) PoseNet.NET.sendTo(msg, r);
    }

    @SuppressWarnings("unchecked")
    static List<EntityPlayerMP> players() {
        MinecraftServer srv = MinecraftServer.getServer();
        return srv == null ? new ArrayList<>()
            : new ArrayList<>((List<EntityPlayerMP>) srv.getConfigurationManager().playerEntityList);
    }

    // ------------------------------------------------------------------ events

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.START || PENDING.isEmpty()) return;
        Set<Integer> online = new HashSet<>();
        for (EntityPlayerMP p : players()) {
            online.add(p.getEntityId());
            drain(p);
        }
        PENDING.keySet()
            .retainAll(online); // players that logged out meanwhile
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.START || e.player.worldObj.isRemote) return;
        PoseState s = PoseState.get(e.player);
        // cheap no-op unless something reset the size (respawn, waking up)
        if (s != null && s.pose != null) Sizes.apply(e.player, s.pose);
    }

    @SubscribeEvent
    public void onLogin(PlayerLoggedInEvent e) {
        EntityPlayerMP p = (EntityPlayerMP) e.player;
        if (PoseState.get(p) == null || PoseState.get(p).pose == null) refresh(p);
        else { // a provider already refreshed it this login: just tell the player itself
            PoseNet.NET.sendTo(new SPose(p.getEntityId(), PoseState.get(p).pose, false), p);
        }
        for (EntityPlayerMP other : players()) {
            PoseState o = PoseState.get(other);
            if (other != p && o != null && o.pose != null)
                PoseNet.NET.sendTo(new SPose(other.getEntityId(), o.pose, false), p);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerLoggedOutEvent e) {
        PENDING.remove(e.player.getEntityId());
        PoseState.map(false)
            .remove(e.player.getEntityId());
    }
}
