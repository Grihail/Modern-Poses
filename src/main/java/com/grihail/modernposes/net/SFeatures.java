package com.grihail.modernposes.net;

import net.minecraft.entity.player.EntityPlayerMP;

import com.grihail.modernposes.Features;
import com.grihail.modernposes.ModernPosesMod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Server -> client at login: the server's features, so that a client with other ones is warned. */
public class SFeatures implements IMessage {

    public int mask;

    public SFeatures() {}

    public SFeatures(int mask) {
        this.mask = mask;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.mask);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.mask = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SFeatures, IMessage> {

        @Override
        public IMessage onMessage(SFeatures msg, MessageContext ctx) {
            ModernPosesMod.proxy.runOnClient(() -> ModernPosesMod.proxy.checkFeatures(msg.mask));
            return null;
        }
    }

    /** Registered on the event buses. */
    public static class Events {

        @SubscribeEvent
        public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
            if (e.player instanceof EntityPlayerMP) {
                PoseNet.NET.sendTo(new SFeatures(Features.mask()), (EntityPlayerMP) e.player);
            }
        }
    }
}
