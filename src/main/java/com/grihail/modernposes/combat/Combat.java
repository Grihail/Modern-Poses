package com.grihail.modernposes.combat;

import java.util.List;
import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import com.grihail.modernposes.Features;
import com.grihail.modernposes.api.WeaponItem;
import com.grihail.modernposes.net.PoseNet;
import com.grihail.modernposes.net.SCombatFx;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

/** 26.3's melee attack (Player.attack and what it calls) on 1.7.10's entities. */
public final class Combat {

    /** Attributes.ATTACK_SPEED. */
    public static final IAttribute ATTACK_SPEED = new RangedAttribute("generic.attackSpeed", 4.0D, 0.0D, 1024.0D)
        .setDescription("Attack Speed")
        .setShouldWatch(true);
    /** Item.BASE_ATTACK_SPEED_ID. */
    public static final UUID SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    /** Item.field_111210_e, the id 1.7.10 gives the damage of held items. */
    public static final UUID DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    private static final String SOUND = "modernposes:player.attack.";

    /** MobEffects: Haste and Mining Fatigue change the attack speed, Strength becomes a flat bonus. */
    public static void registerPotions() {
        Potion.digSpeed.func_111184_a(ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.1D, 2);
        Potion.digSlowdown.func_111184_a(ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1D, 2);
        Potion.damageBoost
            .func_111184_a(SharedMonsterAttributes.attackDamage, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 3.0D, 0);
    }

    // ------------------------------------------------------------------ the charge

    /** Player.getCurrentItemAttackStrengthDelay: ticks a full charge takes. */
    public static float delay(EntityPlayer p) {
        if (!Features.attackCooldown) {
            return 0.0F;
        }
        IAttributeInstance speed = p.getEntityAttribute(ATTACK_SPEED);
        return (float) (1.0D / (speed == null ? 4.0D : speed.getAttributeValue()) * 20.0D);
    }

    /** Player.getAttackStrengthScale. */
    public static float strength(EntityPlayer p, float partial) {
        if (!Features.attackCooldown) {
            return 1.0F;
        }
        return MathHelper.clamp_float((AttackState.of(p).strengthTicker + partial) / delay(p), 0.0F, 1.0F);
    }

    /** Player.getItemSwapScale. */
    public static float swapScale(EntityPlayer p, float partial) {
        if (!Features.attackCooldown) {
            return 1.0F;
        }
        return MathHelper.clamp_float((AttackState.of(p).swapTicker + partial) / delay(p), 0.0F, 1.0F);
    }

    /** Player.resetAttackStrengthTicker. */
    public static void reset(EntityPlayer p) {
        AttackState s = AttackState.of(p);
        s.strengthTicker = 0;
        s.swapTicker = 0;
    }

    /** Player.tick: the charge grows, a different item in the main hand starts it over. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.START) {
            return;
        }
        EntityPlayer p = e.player;
        AttackState s = AttackState.of(p);
        s.strengthTicker++;
        s.swapTicker++;
        ItemStack held = p.inventory.getCurrentItem();
        if (!ItemStack.areItemStacksEqual(s.lastMain, held)) {
            boolean sameItem = s.lastMain != null && held != null && s.lastMain.getItem() == held.getItem();
            if (!sameItem) {
                reset(p);
            }
            if (p.worldObj.isRemote) {
                // 1.7.10 applies an item's attributes on the server only; the indicator must not wait for the packet
                BaseAttributeMap map = p.getAttributeMap();
                if (s.lastMain != null) {
                    map.removeAttributeModifiers(s.lastMain.getAttributeModifiers());
                }
                if (held != null) {
                    map.applyAttributeModifiers(held.getAttributeModifiers());
                }
            }
            s.lastMain = ItemStack.copyItemStack(held);
        }
    }

    // ------------------------------------------------------------------ the attack

    /** EntityPlayer.attackTargetEntityWithCurrentItem, with Forge's hooks kept. */
    public static void attack(EntityPlayer self, Entity target) {
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(self, target))) {
            return;
        }
        ItemStack held = self.getCurrentEquippedItem();
        if (held != null && held.getItem()
            .onLeftClickEntity(held, self, target)) {
            return;
        }
        if (!target.canAttackWithItem() || target.hitByEntity(self)) {
            return;
        }
        if (self.worldObj.isRemote) {
            predict(self, target);
            return;
        }
        EntityLivingBase living = target instanceof EntityLivingBase ? (EntityLivingBase) target : null;
        float base = (float) self.getEntityAttribute(SharedMonsterAttributes.attackDamage)
            .getAttributeValue();
        float scale = strength(self, 0.5F);
        float magic = living == null ? 0.0F : scale * EnchantmentHelper.getEnchantmentModifierLiving(self, living);
        base *= damageScale(scale);
        AttackState.of(self).strengthTicker = 0;
        if (base <= 0.0F && magic <= 0.0F) {
            return;
        }
        boolean full = scale > 0.9F;
        boolean knockbackAttack = self.isSprinting() && full;
        if (knockbackAttack) {
            sound(self, "knockback");
        }
        boolean crit = full && canCrit(self, target);
        if (crit) {
            base *= 1.5F;
        }
        float total = base + magic;
        boolean sweep = isSweep(self, held, full, crit, knockbackAttack);
        float oldHealth = living == null ? 0.0F : living.getHealth();
        double oldX = target.motionX, oldY = target.motionY, oldZ = target.motionZ;
        DamageSource source = DamageSource.causePlayerDamage(self);
        if (!target.attackEntityFrom(source, total)) {
            sound(self, "nodamage");
            return;
        }
        float knockback = (living == null ? 0 : EnchantmentHelper.getKnockbackModifier(self, living))
            + (knockbackAttack ? 0.5F : 0.0F);
        extraKnockback(self, target, knockback, oldX, oldY, oldZ);
        if (sweep) {
            sweep(self, target, base, source, scale);
        }
        if (crit) {
            sound(self, "crit");
            self.onCriticalHit(target);
        } else if (!sweep) {
            sound(self, full ? "strong" : "weak");
        }
        if (magic > 0.0F) {
            self.onEnchantmentCritical(target);
        }
        if (total >= 18.0F) {
            self.triggerAchievement(AchievementList.overkill);
        }
        self.setLastAttacker(target);
        afterHit(self, target, living);
        if (living != null) {
            float dealt = oldHealth - living.getHealth();
            self.addStat(StatList.damageDealtStat, Math.round(dealt * 10.0F));
            if (dealt > 2.0F) {
                fx(
                    self,
                    SCombatFx.DAMAGE,
                    target.posX,
                    target.posY + target.height * 0.5D,
                    target.posZ,
                    (int) (dealt * 0.5D));
            }
        }
        self.addExhaustion(0.1F);
    }

    /** Player.baseDamageScaleFactor. */
    public static float damageScale(float scale) {
        return 0.2F + scale * scale * 0.8F;
    }

    /** What the attack does to the attacker on its own client, at once (26.3 runs Player.attack there too). */
    private static void predict(EntityPlayer self, Entity target) {
        float knockback = target instanceof EntityLivingBase
            ? EnchantmentHelper.getKnockbackModifier(self, (EntityLivingBase) target)
            : 0;
        if (self.isSprinting() && strength(self, 0.5F) > 0.9F) {
            knockback += 0.5F;
        }
        if (knockback > 0.0F) {
            self.motionX *= 0.6D;
            self.motionZ *= 0.6D;
            self.setSprinting(false);
        }
    }

    /** Player.canCriticalAttack. */
    private static boolean canCrit(EntityPlayer self, Entity target) {
        return self.fallDistance > 0.0F && !self.onGround
            && !self.isOnLadder()
            && !self.isInWater()
            && !self.isPotionActive(Potion.blindness)
            && self.ridingEntity == null
            && target instanceof EntityLivingBase
            && !self.isSprinting();
    }

    /** Player.isSweepAttack. */
    private static boolean isSweep(EntityPlayer self, ItemStack held, boolean full, boolean crit, boolean knockback) {
        if (!full || crit || knockback || !self.onGround || held == null) {
            return false;
        }
        double dx = self.posX - self.prevPosX, dz = self.posZ - self.prevPosZ;
        double max = self.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
            .getAttributeValue() * 2.5D;
        return dx * dx + dz * dz < max * max && sweeps(held);
    }

    /** The item is one of 26.3's ItemTags.SWORDS. */
    public static boolean sweeps(ItemStack stack) {
        return stack.getItem() instanceof WeaponItem ? ((WeaponItem) stack.getItem()).sweeps(stack)
            : stack.getItem() instanceof ItemSword;
    }

    /** Player.causeExtraKnockback. */
    private static void extraKnockback(EntityPlayer self, Entity target, float amount, double oldX, double oldY,
        double oldZ) {
        if (amount > 0.0F) {
            float yaw = self.rotationYaw * (float) Math.PI / 180.0F;
            if (target instanceof EntityLivingBase) {
                knockback((EntityLivingBase) target, amount, MathHelper.sin(yaw), -MathHelper.cos(yaw));
            } else {
                target.addVelocity(-MathHelper.sin(yaw) * amount, 0.1D, MathHelper.cos(yaw) * amount);
            }
            self.motionX *= 0.6D;
            self.motionZ *= 0.6D;
            self.setSprinting(false);
        }
        if (target instanceof EntityPlayerMP && target.velocityChanged) {
            ((EntityPlayerMP) target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
            target.velocityChanged = false;
            target.motionX = oldX;
            target.motionY = oldY;
            target.motionZ = oldZ;
        }
    }

    /** LivingEntity.knockback. */
    public static void knockback(EntityLivingBase e, double power, double x, double z) {
        power *= 1.0D - e.getEntityAttribute(SharedMonsterAttributes.knockbackResistance)
            .getAttributeValue();
        if (power <= 0.0D) {
            return;
        }
        e.isAirBorne = true;
        while (x * x + z * z < 1.0E-5D) {
            x = (e.getRNG()
                .nextDouble()
                - e.getRNG()
                    .nextDouble())
                * 0.01D;
            z = (e.getRNG()
                .nextDouble()
                - e.getRNG()
                    .nextDouble())
                * 0.01D;
        }
        double length = Math.sqrt(x * x + z * z);
        e.motionX = e.motionX / 2.0D - x / length * power;
        e.motionY = e.onGround ? Math.min(0.4D, e.motionY / 2.0D + power) : e.motionY;
        e.motionZ = e.motionZ / 2.0D - z / length * power;
    }

    /** Player.doSweepAttack. */
    private static void sweep(EntityPlayer self, Entity target, float base, DamageSource source, float scale) {
        sound(self, "sweep");
        float yaw = self.rotationYaw * (float) Math.PI / 180.0F;
        float damage = 1.0F + EnchantmentSweeping.ratio(self.getCurrentEquippedItem()) * base;
        List<?> near = self.worldObj
            .getEntitiesWithinAABB(EntityLivingBase.class, target.boundingBox.expand(1.0D, 0.25D, 1.0D));
        for (Object o : near) {
            EntityLivingBase e = (EntityLivingBase) o;
            if (e == self || e == target || self.isOnSameTeam(e) || self.getDistanceSqToEntity(e) >= 9.0D) {
                continue;
            }
            float dealt = (damage + EnchantmentHelper.getEnchantmentModifierLiving(self, e)) * scale;
            if (e.attackEntityFrom(source, dealt)) {
                knockback(e, 0.4D, MathHelper.sin(yaw), -MathHelper.cos(yaw));
                EnchantmentHelper.func_151384_a(e, self);
                EnchantmentHelper.func_151385_b(self, e);
                int fire = EnchantmentHelper.getFireAspectModifier(self);
                if (fire > 0) {
                    e.setFire(fire * 4);
                }
            }
        }
        double dx = -MathHelper.sin(yaw), dz = MathHelper.cos(yaw);
        fx(self, SCombatFx.SWEEP, self.posX + dx, self.posY + self.height * 0.5D, self.posZ + dz, 0);
    }

    private static void fx(EntityPlayer p, int kind, double x, double y, double z, int count) {
        PoseNet.NET.sendToAllAround(
            new SCombatFx(kind, x, y, z, count),
            new NetworkRegistry.TargetPoint(p.dimension, x, y, z, 32.0D));
    }

    /** Player.itemAttackInteraction and the enchantments' post attack effects. */
    private static void afterHit(EntityPlayer self, Entity target, EntityLivingBase living) {
        if (living != null) {
            EnchantmentHelper.func_151384_a(living, self);
        }
        EnchantmentHelper.func_151385_b(self, target);
        ItemStack stack = self.getCurrentEquippedItem();
        Object hurt = target;
        if (target instanceof EntityDragonPart) {
            IEntityMultiPart parent = ((EntityDragonPart) target).entityDragonObj;
            if (parent instanceof EntityLivingBase) {
                hurt = parent;
            }
        }
        if (stack != null && hurt instanceof EntityLivingBase) {
            stack.hitEntity((EntityLivingBase) hurt, self);
            if (stack.stackSize <= 0) {
                self.destroyCurrentEquippedItem();
            }
        }
        int fire = EnchantmentHelper.getFireAspectModifier(self);
        if (living != null && fire > 0) {
            target.setFire(fire * 4);
        }
    }

    private static void sound(EntityPlayer p, String name) {
        p.worldObj.playSoundEffect(p.posX, p.posY, p.posZ, SOUND + name, 1.0F, 1.0F);
    }
}
