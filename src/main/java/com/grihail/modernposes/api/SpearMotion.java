package com.grihail.modernposes.api;

/**
 * 26.3's spear motion as plain math (SpearAnimations, its UseParams and the easing functions it uses), so that any
 * body and any item renderer gets the same movement. Angles in degrees where 26.3 has degrees, radians where
 * ModelBiped needs them; nothing here touches a model or GL.
 */
public final class SpearMotion {

    private static final float PI = (float) Math.PI;
    private static final float RAD = PI / 180.0F;

    private SpearMotion() {}

    /** SpearAnimations.UseParams: the stages of holding a spear forward (charging), from the ticks it is held. */
    public static final class Use {

        public final float raise, raiseStart, raiseMiddle, raiseEnd, sway, lower, raiseBack, swayIntensity, swaySlow,
            swayFast;

        public Use(KineticItem item, net.minecraft.item.ItemStack stack, float time) {
            int raised = item.delayTicks(stack);
            int swayEnd = item.dismountTicks(stack) + raised;
            int swayStart = swayEnd - 20;
            int lowerEnd = item.knockbackTicks(stack) + raised;
            int lowerStart = lowerEnd - 40;
            int raiseBackEnd = item.damageTicks(stack) + raised;
            this.raise = progress(time, 0.0F, raised);
            this.raiseStart = progress(this.raise, 0.0F, 0.5F);
            this.raiseMiddle = progress(this.raise, 0.5F, 0.8F);
            this.raiseEnd = progress(this.raise, 0.8F, 1.0F);
            this.sway = progress(time, swayStart, swayEnd);
            this.lower = outCubic(inOutElastic(progress(time - 20.0F, lowerStart, lowerEnd)));
            this.raiseBack = progress(time, raiseBackEnd - 5, raiseBackEnd);
            this.swayIntensity = 2.0F * outCirc(this.sway) - 2.0F * inCirc(this.raiseBack);
            this.swaySlow = (float) Math.sin(time * 19.0F * RAD) * this.swayIntensity;
            this.swayFast = (float) Math.sin(time * 30.0F * RAD) * this.swayIntensity;
        }
    }

    /**
     * SpearAnimations.thirdPersonHandUse: the arm holding a spear, pointed forward; while charging it rises, sways
     * and lowers.
     *
     * @param useTicks ticks the spear has been used (0 when not in use)
     * @return x, y and z rotation of the arm (radians)
     */
    public static float[] holdingArm(float headX, float headY, boolean rightArm, boolean flyingOrSwimming,
        KineticItem item, net.minecraft.item.ItemStack stack, float useTicks) {
        float invert = rightArm ? 1.0F : -1.0F;
        float y = -0.1F * invert + headY;
        float x = -PI / 2.0F + headX + 0.8F;
        float z = 0.0F;
        if (flyingOrSwimming) {
            x -= 0.9599311F;
        }
        y = RAD * clamp(y / RAD, -60.0F, 60.0F);
        x = RAD * clamp(x / RAD, -120.0F, 30.0F);
        if (useTicks > 0.0F && item != null) {
            Use u = new Use(item, stack, useTicks);
            y += -invert * u.swayFast * RAD * u.swayIntensity;
            z += -invert * u.swaySlow * RAD * u.swayIntensity * 0.5F;
            x += RAD * (-40.0F * u.raiseStart + 30.0F * u.raiseMiddle
                - 20.0F * u.raiseEnd
                + 20.0F * u.lower
                + 10.0F * u.raiseBack
                + 0.6F * u.swaySlow * u.swayIntensity);
        }
        return new float[] { x, y, z };
    }

    /**
     * SpearAnimations.thirdPersonAttackHand: what the stab adds to the attacking arm's x rotation (radians), from the
     * swing's progress 0..1. 26.3 keeps the arms' y rotation from following the body's turn during a stab.
     */
    public static float stabArmX(float progress) {
        float prepare = inOutSine(progress(progress, 0.0F, 0.05F));
        float attack = inQuad(progress(progress, 0.05F, 0.2F));
        float retract = inOutExpo(progress(progress, 0.4F, 1.0F));
        return (90.0F * prepare - 120.0F * attack + 30.0F * retract) * RAD;
    }

    /** The stab's push (attack) and pull back (retract) of the item, 0..1 each (thirdPersonAttackItem and others). */
    public static float stabAttack(float progress) {
        return inQuad(progress(progress, 0.05F, 0.2F));
    }

    public static float stabRetract(float progress) {
        return inOutExpo(progress(progress, 0.4F, 1.0F));
    }

    /** SpearAnimations.hitFeedbackAmount: the jolt of a charged hit, from the ticks since it. */
    public static float hitFeedback(float ticksSince) {
        return 0.4F * (outQuart(progress(ticksSince, 1.0F, 3.0F)) - inOutSine(progress(ticksSince, 3.0F, 10.0F)));
    }

    // ------------------------------------------------------------------ 26.3's Mth and Ease

    public static float progress(float time, float start, float end) {
        if (end == start) {
            return time >= end ? 1.0F : 0.0F;
        }
        return clamp((time - start) / (end - start), 0.0F, 1.0F);
    }

    public static float clamp(float v, float min, float max) {
        return v < min ? min : Math.min(v, max);
    }

    public static float inQuad(float x) {
        return x * x;
    }

    public static float inOutSine(float x) {
        return -((float) Math.cos(PI * x) - 1.0F) / 2.0F;
    }

    public static float outBack(float x) {
        float d = x - 1.0F;
        return 1.0F + 2.70158F * d * d * d + 1.70158F * d * d;
    }

    public static float inOutBack(float x) {
        if (x < 0.5F) {
            return 4.0F * x * x * (7.189819F * x - 2.5949094F) / 2.0F;
        }
        float dt = 2.0F * x - 2.0F;
        return (dt * dt * (3.5949094F * dt + 2.5949094F) + 2.0F) / 2.0F;
    }

    public static float outQuart(float x) {
        float d = (1.0F - x) * (1.0F - x);
        return 1.0F - d * d;
    }

    public static float outCubic(float x) {
        float d = 1.0F - x;
        return 1.0F - d * d * d;
    }

    public static float inOutExpo(float x) {
        if (x < 0.5F) {
            return x == 0.0F ? 0.0F : (float) (Math.pow(2.0, 20.0 * x - 10.0) / 2.0);
        }
        return x == 1.0F ? 1.0F : (float) ((2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0);
    }

    public static float outCirc(float x) {
        return (float) Math.sqrt(1.0F - (x - 1.0F) * (x - 1.0F));
    }

    public static float inCirc(float x) {
        return (float) -Math.sqrt(1.0F - x * x) + 1.0F;
    }

    public static float inOutElastic(float x) {
        if (x == 0.0F || x == 1.0F) {
            return x;
        }
        double sin = Math.sin((20.0 * x - 11.125) * PI * 4.0F / 9.0F);
        return x < 0.5F ? (float) (-(Math.pow(2.0, 20.0 * x - 10.0) * sin) / 2.0)
            : (float) (Math.pow(2.0, -20.0 * x + 10.0) * sin / 2.0 + 1.0);
    }
}
