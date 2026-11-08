package com.grihail.modernposes.net;

import net.minecraft.entity.player.EntityPlayerMP;

import com.grihail.modernposes.hands.HandsServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Client -> server: the swap key was pressed. */
public class CSwapHands implements IMessage {

    public CSwapHands() {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<CSwapHands, IMessage> {

        @Override
        public IMessage onMessage(CSwapHands msg, MessageContext ctx) {
            EntityPlayerMP p = ctx.getServerHandler().playerEntity;
            HandsServer.runOnServer(() -> HandsServer.swapHands(p));
            return null;
        }
    }
}
