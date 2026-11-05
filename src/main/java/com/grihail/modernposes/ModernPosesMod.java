package com.grihail.modernposes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Modern player poses for 1.7.10: sneaking 1.5 high, swimming and crawling 0.6 high, sprint-swimming, sneaking or
 * crawling forced by a low ceiling, modern water movement, the camera at the pose's eye and the lying player model;
 * the gliding pose (fall flying) when another mod's {@link com.grihail.modernposes.api.PoseTrigger} asks for it
 * (elytra).
 * Other mods give their own pose sizes through {@link com.grihail.modernposes.api.ModernPoses}.
 */
@Mod(
    modid = ModernPosesMod.MODID,
    version = Tags.VERSION,
    name = "Modern Poses",
    acceptedMinecraftVersions = "[1.7.10]")
public class ModernPosesMod {

    public static final String MODID = "modernposes";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.grihail.modernposes.client.ClientProxy",
        serverSide = "com.grihail.modernposes.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
