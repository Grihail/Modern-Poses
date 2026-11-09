package com.grihail.modernposes.net;

import com.grihail.modernposes.ModernPosesMod;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Server -> client: the particles of a melee hit that 1.7.10 has no packet for. */
public class SCombatFx implements IMessage {

    public static final int SWEEP = 0, DAMAGE = 1;

    public int kind, count;
    public double x, y, z;

    public SCombatFx() {}

    public SCombatFx(int kind, double x, double y, double z, int count) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.z = z;
        this.count = count;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.kind);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeShort(this.count);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.kind = buf.readByte();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.count = buf.readShort();
    }

    public static class Handler implements IMessageHandler<SCombatFx, IMessage> {

        @Override
        public IMessage onMessage(SCombatFx msg, MessageContext ctx) {
            ModernPosesMod.proxy.runOnClient(() -> com.grihail.modernposes.client.CombatParticles.spawn(msg));
            return null;
        }
    }
}
