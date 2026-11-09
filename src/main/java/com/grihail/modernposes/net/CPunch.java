package com.grihail.modernposes.net;

import net.minecraft.entity.player.EntityPlayerMP;

import com.grihail.modernposes.combat.Combat;
import com.grihail.modernposes.hands.HandsServer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Client -> server: a left click that hit no entity (26.3's ServerboundPunchPacket). 1.7.10's swing packet cannot say
 * so: it comes before the attack's own packet and with every swing of mining.
 */
public class CPunch implements IMessage {

    public CPunch() {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<CPunch, IMessage> {

        @Override
        public IMessage onMessage(CPunch msg, MessageContext ctx) {
            EntityPlayerMP p = ctx.getServerHandler().playerEntity;
            HandsServer.runOnServer(() -> Combat.reset(p));
            return null;
        }
    }
}
