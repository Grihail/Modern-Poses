package com.grihail.modernposes.hands;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.grihail.modernposes.net.PoseNet;
import com.grihail.modernposes.net.SHands;
import com.grihail.modernposes.net.SSwingOff;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * The server's side of the off hand: the stack and the use go to the clients that see the player, the hand survives
 * dimension changes and (with keepInventory) deaths, the swap key.
 */
public final class HandsServer {

    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();
    /** The player whose C08 packet is being handled as the off hand's. */
    private static EntityPlayerMP active;
    private static long ticks;

    public static void runOnServer(Runnable r) {
        QUEUE.add(r);
    }

    /** NetHandlerPlayServer.processPlayerBlockPlacement starts on a packet of the off hand. */
    public static void begin(EntityPlayerMP p) {
        active = p;
        Hands.of(p).offUseTick = ticks;
        HandUse.swap(p);
    }

    /** The packet of the off hand was handled: the hands back, the client told (the packet's code saw them swapped). */
    public static void end(EntityPlayerMP p) {
        active = null;
        HandUse.swap(p);
        Hands h = Hands.of(p);
        h.setUsedOff(p.isUsingItem() && p.getItemInUse() == h.get());
        if (!p.isUsingItem()) {
            p.sendContainerToPlayer(p.inventoryContainer);
        }
    }

    public static boolean isActive() {
        return active != null;
    }

    /** C0A: the swing that follows an off hand's use is that hand's. */
    public static void onSwing(EntityPlayerMP p) {
        Hands h = Hands.of(p);
        boolean off = ticks - h.offUseTick <= 2;
        h.swingOff = off;
        if (off) {
            h.offUseTick = -100;
            for (Object o : ((WorldServer) p.worldObj).getEntityTracker()
                .getTrackingPlayers(p)) {
                PoseNet.NET.sendTo(new SSwingOff(p.getEntityId()), (EntityPlayerMP) o);
            }
        }
    }

    /** The swap key (26.3's Player.handleSwap). */
    public static void swapHands(EntityPlayerMP p) {
        HandUse.swap(p);
        p.clearItemInUse();
        Hands.of(p)
            .setUsedOff(false);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            ticks++;
            for (Runnable r; (r = QUEUE.poll()) != null;) {
                r.run();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.worldObj.isRemote) {
            return;
        }
        Hands h = Hands.of(e.player);
        if (!ItemStack.areItemStacksEqual(h.sentStack, h.get()) || h.sentUsedOff != h.usedOff()) {
            h.sentStack = ItemStack.copyItemStack(h.get());
            h.sentUsedOff = h.usedOff();
            SHands msg = new SHands(e.player.getEntityId(), h.get(), h.usedOff());
            for (Object o : ((WorldServer) e.player.worldObj).getEntityTracker()
                .getTrackingPlayers(e.player)) {
                PoseNet.NET.sendTo(msg, (EntityPlayerMP) o);
            }
        }
    }

    /** A player comes into view: its off hand with it. */
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        if (e.target instanceof EntityPlayer && e.entityPlayer instanceof EntityPlayerMP) {
            Hands h = Hands.of((EntityPlayer) e.target);
            PoseNet.NET
                .sendTo(new SHands(e.target.getEntityId(), h.get(), h.usedOff()), (EntityPlayerMP) e.entityPlayer);
        }
    }

    /** The new entity of a respawn or a return from the End; a death has dropped the hand, unless keepInventory. */
    @SubscribeEvent
    public void onClone(PlayerEvent.Clone e) {
        Hands.of(e.entityPlayer)
            .set(
                Hands.of(e.original)
                    .raw());
    }
}
