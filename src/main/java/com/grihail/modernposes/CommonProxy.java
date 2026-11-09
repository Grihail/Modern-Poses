package com.grihail.modernposes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import com.grihail.modernposes.net.PoseNet;
import com.grihail.modernposes.server.ServerPoses;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        PoseNet.init();
    }

    public void init(FMLInitializationEvent event) {
        register(new ServerPoses());
        register(new com.grihail.modernposes.hands.HandsServer());
        register(new com.grihail.modernposes.combat.Combat());
    }

    protected static void register(Object events) {
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance()
            .bus()
            .register(events);
    }

    /** True for the client's own player entity (whose posY is the eye position, not the feet). */
    public boolean isLocalPlayer(EntityPlayer p) {
        return false;
    }

    /** Client hook: modern water movement of the own player. True = skip vanilla moveEntityWithHeading. */
    public boolean handleMovement(EntityPlayer p, float strafe, float forward) {
        return false;
    }

    /** Called from the netty thread for server-to-client messages; the client proxy queues it for the main thread. */
    public void runOnClient(Runnable r) {}
}
