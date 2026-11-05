package com.grihail.modernposes.net;

import com.grihail.modernposes.ModernPosesMod;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class PoseNet {

    public static final SimpleNetworkWrapper NET = NetworkRegistry.INSTANCE.newSimpleChannel(ModernPosesMod.MODID);

    private PoseNet() {}

    public static void init() {
        NET.registerMessage(SPose.Handler.class, SPose.class, 0, Side.CLIENT);
        NET.registerMessage(CPose.Handler.class, CPose.class, 1, Side.SERVER);
    }
}
