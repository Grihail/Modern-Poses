package com.grihail.modernposes.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;

import com.grihail.modernposes.CommonProxy;
import com.grihail.modernposes.Features;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    /** Client tick counter. */
    public static long ticks;
    public static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        register(new ClientEvents());
        register(new HandClient());
        register(new CombatHud());
        if (Features.offhand) {
            cpw.mods.fml.client.registry.ClientRegistry.registerKeyBinding(HandClient.SWAP);
        }
    }

    @Override
    public boolean isLocalPlayer(EntityPlayer p) {
        return p instanceof EntityPlayerSP;
    }

    @Override
    public boolean handleMovement(EntityPlayer p, float strafe, float forward) {
        return p instanceof EntityPlayerSP && SwimMovement.tick((EntityPlayerSP) p, strafe, forward);
    }

    @Override
    public void runOnClient(Runnable r) {
        QUEUE.add(r);
    }
}
