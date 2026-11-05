package com.grihail.modernposes.net;

import net.minecraft.entity.player.EntityPlayerMP;

import com.grihail.modernposes.server.ServerPoses;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Client -> server: the own player switched pose. Not queued for the server tick like other messages: it is stored
 * as "pending" and applied right before the next movement packet of that player (see {@link ServerPoses}), so the
 * server checks that movement with the same hitbox the client used.
 */
public class CPose implements IMessage {

    public String pose;

    public CPose() {}

    public CPose(String pose) {
        this.pose = pose;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, pose);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pose = ByteBufUtils.readUTF8String(buf);
        if (pose.length() > 32) pose = pose.substring(0, 32);
    }

    public static class Handler implements IMessageHandler<CPose, IMessage> {

        @Override
        public IMessage onMessage(CPose msg, MessageContext ctx) {
            EntityPlayerMP p = ctx.getServerHandler().playerEntity;
            ServerPoses.request(p, msg.pose);
            return null;
        }
    }
}
