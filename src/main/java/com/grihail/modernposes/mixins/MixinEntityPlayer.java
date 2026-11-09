package com.grihail.modernposes.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.grihail.modernposes.ModernPosesMod;
import com.grihail.modernposes.Sizes;
import com.grihail.modernposes.api.RaisedBlockItem;
import com.grihail.modernposes.combat.ArmorRules;
import com.grihail.modernposes.combat.Combat;
import com.grihail.modernposes.hands.HandUse;
import com.grihail.modernposes.hands.Hands;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    @Unique
    private boolean modernposes$finishOff;

    /** Attributes.ATTACK_SPEED, on the base of 4. */
    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    private void modernposes$attackSpeed(CallbackInfo ci) {
        ((EntityPlayer) (Object) this).getAttributeMap()
            .registerAttribute(Combat.ATTACK_SPEED);
    }

    /** 26.3's armor for a player; Forge's method has 1.7.10's formula inside. */
    @Redirect(
        method = "damageEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/ISpecialArmor$ArmorProperties;ApplyArmor(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;D)F",
            remap = false))
    private float modernposes$armor(EntityLivingBase self, ItemStack[] armor, DamageSource source, double damage) {
        return ArmorRules.applyPlayer(self, armor, source, damage);
    }

    /** 26.3's attack instead of 1.7.10's. */
    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"), cancellable = true)
    private void modernposes$attack(Entity target, CallbackInfo ci) {
        ci.cancel();
        Combat.attack((EntityPlayer) (Object) this, target);
    }

    /** The stack the item in use is compared with: the hand's that holds it. */
    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;",
            ordinal = 0))
    private ItemStack modernposes$handOfUse(InventoryPlayer inventory) {
        return HandUse.handOfUse((EntityPlayer) (Object) this);
    }

    /** What an item in use leaves (an empty bottle, a bowl) goes to the hand that held it. */
    @Inject(method = "onItemUseFinish", at = @At("HEAD"))
    private void modernposes$finishBegin(CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        this.modernposes$finishOff = Hands.of(self)
            .usedOff();
        if (this.modernposes$finishOff) {
            HandUse.swap(self);
        }
    }

    @Inject(method = "onItemUseFinish", at = @At("RETURN"))
    private void modernposes$finishEnd(CallbackInfo ci) {
        if (this.modernposes$finishOff) {
            this.modernposes$finishOff = false;
            HandUse.swap((EntityPlayer) (Object) this);
        }
    }

    /** 1.7.10 halves the damage while blocking; a raised item stops what it stops by itself. */
    @Redirect(
        method = "damageEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isBlocking()Z"))
    private boolean modernposes$halves(EntityPlayer self) {
        return self.isBlocking() && !(self.getItemInUse()
            .getItem() instanceof RaisedBlockItem);
    }

    /** Sneaking does not lower the camera by ySize (the pose's eye does); modern water movement of the own player. */
    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"), cancellable = true)
    private void modernposes$move(float strafe, float forward, CallbackInfo ci) {
        EntityPlayer self = (EntityPlayer) (Object) this;
        Sizes.beforeMove(self);
        if (self.worldObj.isRemote && ModernPosesMod.proxy.handleMovement(self, strafe, forward)) ci.cancel();
    }
}
