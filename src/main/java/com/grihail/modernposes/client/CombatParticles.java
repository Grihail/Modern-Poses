package com.grihail.modernposes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.grihail.modernposes.net.SCombatFx;

/** 26.3's AttackSweepParticle and the damage indicator (CritParticle.DamageIndicatorProvider). */
public final class CombatParticles {

    private CombatParticles() {}

    public static void spawn(SCombatFx msg) {
        Minecraft mc = Minecraft.getMinecraft();
        World w = mc.theWorld;
        if (w == null) {
            return;
        }
        if (msg.kind == SCombatFx.SWEEP) {
            mc.effectRenderer.addEffect(new Sweep(w, msg.x, msg.y, msg.z));
            return;
        }
        // ServerLevel.sendParticles: spread 0.1, 0, 0.1 and speed 0.2
        for (int i = 0; i < msg.count; i++) {
            mc.effectRenderer.addEffect(
                new Damage(
                    w,
                    msg.x + w.rand.nextGaussian() * 0.1D,
                    msg.y,
                    msg.z + w.rand.nextGaussian() * 0.1D,
                    w.rand.nextGaussian() * 0.2D,
                    w.rand.nextGaussian() * 0.2D + 1.0D,
                    w.rand.nextGaussian() * 0.2D));
        }
    }

    /** A particle with a texture of its own: EffectRenderer's layer 3 leaves the drawing to the particle. */
    private abstract static class Own extends EntityFX {

        Own(World w, double x, double y, double z) {
            super(w, x, y, z, 0.0D, 0.0D, 0.0D);
            this.noClip = true;
        }

        @Override
        public int getFXLayer() {
            return 3;
        }

        abstract ResourceLocation texture();

        abstract float size(float partial);

        @Override
        public void renderParticle(Tessellator t, float partial, float rx, float rxz, float rz, float ryz, float rxy) {
            float s = size(partial);
            float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partial - interpPosX);
            float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partial - interpPosY);
            float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partial - interpPosZ);
            // 26.3 draws both as cutouts, unlit by the world's GL light
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(texture());
            t.startDrawingQuads();
            t.setBrightness(getBrightnessForRender(partial));
            t.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, 1.0F);
            t.addVertexWithUV(x - rx * s - ryz * s, y - rxz * s, z - rz * s - rxy * s, 1.0D, 1.0D);
            t.addVertexWithUV(x - rx * s + ryz * s, y + rxz * s, z - rz * s + rxy * s, 1.0D, 0.0D);
            t.addVertexWithUV(x + rx * s + ryz * s, y + rxz * s, z + rz * s + rxy * s, 0.0D, 0.0D);
            t.addVertexWithUV(x + rx * s - ryz * s, y - rxz * s, z + rz * s - rxy * s, 0.0D, 1.0D);
            t.draw();
            GL11.glPopAttrib();
        }
    }

    private static final class Sweep extends Own {

        private static final ResourceLocation[] FRAMES = new ResourceLocation[8];

        static {
            for (int i = 0; i < FRAMES.length; i++) {
                FRAMES[i] = new ResourceLocation("modernposes", "textures/particle/sweep_" + i + ".png");
            }
        }

        Sweep(World w, double x, double y, double z) {
            super(w, x, y, z);
            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.particleMaxAge = 4;
            float col = this.rand.nextFloat() * 0.6F + 0.4F;
            this.particleRed = this.particleGreen = this.particleBlue = col;
        }

        @Override
        public void onUpdate() {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            if (this.particleAge++ >= this.particleMaxAge) {
                setDead();
            }
        }

        @Override
        public int getBrightnessForRender(float partial) {
            return 15728880;
        }

        @Override
        ResourceLocation texture() {
            return FRAMES[Math.min(7, this.particleAge * 7 / this.particleMaxAge)];
        }

        @Override
        float size(float partial) {
            return 1.0F;
        }
    }

    private static final class Damage extends Own {

        private static final ResourceLocation TEXTURE = new ResourceLocation(
            "modernposes",
            "textures/particle/damage.png");

        private final float quadSize;

        Damage(World w, double x, double y, double z, double xa, double ya, double za) {
            super(w, x, y, z);
            // Particle's constructor with no speed of its own, then CritParticle's
            double dx = (Math.random() * 2.0D - 1.0D) * 0.4D;
            double dy = (Math.random() * 2.0D - 1.0D) * 0.4D;
            double dz = (Math.random() * 2.0D - 1.0D) * 0.4D;
            double speed = (Math.random() + Math.random() + 1.0D) * 0.15D;
            double length = Math.max(1.0E-4D, MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz));
            this.motionX = dx / length * speed * 0.4D * 0.1D + xa * 0.4D;
            this.motionY = (dy / length * speed * 0.4D + 0.1D) * 0.1D + ya * 0.4D;
            this.motionZ = dz / length * speed * 0.4D * 0.1D + za * 0.4D;
            float col = this.rand.nextFloat() * 0.3F + 0.6F;
            this.particleRed = this.particleGreen = this.particleBlue = col;
            this.quadSize = 0.1F * (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F * 0.75F;
            this.particleMaxAge = 20;
        }

        @Override
        public void onUpdate() {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            if (this.particleAge++ >= this.particleMaxAge) {
                setDead();
                return;
            }
            this.motionY -= 0.04D * 0.5D;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.motionX *= 0.7D;
            this.motionY *= 0.7D;
            this.motionZ *= 0.7D;
            this.particleGreen *= 0.96F;
            this.particleBlue *= 0.9F;
        }

        @Override
        ResourceLocation texture() {
            return TEXTURE;
        }

        @Override
        float size(float partial) {
            return this.quadSize
                * MathHelper.clamp_float((this.particleAge + partial) / this.particleMaxAge * 32.0F, 0.0F, 1.0F);
        }
    }
}
