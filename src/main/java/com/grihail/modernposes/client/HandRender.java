package com.grihail.modernposes.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.grihail.modernposes.api.Hand;
import com.grihail.modernposes.api.HeldModel;
import com.grihail.modernposes.api.ModernHands;
import com.grihail.modernposes.api.RaisedBlockItem;
import com.grihail.modernposes.api.ScopeItem;
import com.grihail.modernposes.hands.Hands;
import com.grihail.modernposes.mixins.AccessorEntityPlayer;
import com.grihail.modernposes.mixins.client.AccessorItemRenderer;

/**
 * Items in the hands. Third person: the off hand's item on the left arm, 1.7.10's drawing mirrored; items with a
 * {@link HeldModel} at 26.3's place. First person: each hand drawn by itself, the off hand's item by 1.7.10's own code
 * mirrored in the projection (so that its light stays right), {@link HeldModel}s by 26.3's arm transforms.
 */
public final class HandRender {

    private static boolean inside;

    /** The off hand's item shown in the first person, and how far it is raised (as ItemRenderer's main hand). */
    private static ItemStack offShown;
    private static float offProgress, prevOffProgress;

    private HandRender() {}

    // ------------------------------------------------------------------ third person

    /** RenderPlayer.renderEquippedItems, at its end. */
    public static void afterEquipped(EntityPlayer p, ModelBiped model, float partial) {
        atEye(p, model, partial);
        if (ModernHands.heldModel(p.inventory.getCurrentItem()) != null) {
            GL11.glPushMatrix();
            model.bipedRightArm.postRender(0.0625F);
            atArm(p, Hand.MAIN, partial);
            GL11.glPopMatrix();
        }
        if (Hands.of(p)
            .get() != null) {
            GL11.glPushMatrix();
            model.bipedLeftArm.postRender(0.0625F);
            atArm(p, Hand.OFF, partial);
            GL11.glPopMatrix();
        }
    }

    /** PlayerItemInHandLayer.renderItemHeldToEye: a scope in use at the eye, the head's pitch kept to -30..90. */
    private static void atEye(EntityPlayer p, ModelBiped model, float partial) {
        Hand hand = ModernHands.usedHand(p);
        ItemStack stack = hand == null ? null : ModernHands.getHeld(p, hand);
        HeldModel held = ModernHands.heldModel(stack);
        if (held == null || !(stack.getItem() instanceof ScopeItem)
            || ModernHands.swingProgress(p, hand, partial) >= 1.0E-5F) {
            return;
        }
        GL11.glPushMatrix();
        float x = model.bipedHead.rotateAngleX;
        model.bipedHead.rotateAngleX = Math.max((float) -Math.PI / 6.0F, Math.min((float) Math.PI / 2.0F, x));
        model.bipedHead.postRender(0.0625F);
        model.bipedHead.rotateAngleX = x;
        // CustomHeadLayer.translateToHead: half a block up, the head's scale 0.625 turned to face out
        GL11.glTranslatef(0.0F, -0.25F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(0.625F, -0.625F, -0.625F);
        boolean left = hand == Hand.OFF;
        GL11.glTranslatef((left ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        held.renderAtEye(stack, p, left, partial);
        GL11.glPopMatrix();
    }

    /** What a hand holds, with the matrix at its arm's pivot. */
    public static void atArm(EntityPlayer p, Hand hand, float partial) {
        ItemStack stack = ModernHands.getHeld(p, hand);
        if (stack == null) {
            return;
        }
        boolean left = hand == Hand.OFF;
        boolean using = ModernHands.usedHand(p) == hand;
        HeldModel model = ModernHands.heldModel(stack);
        if (model != null && using
            && stack.getItem() instanceof ScopeItem
            && ModernHands.swingProgress(p, hand, partial) < 1.0E-5F) {
            return; // drawn at the eye (afterEquipped)
        }
        if (model != null) {
            // 26.3's ItemInHandLayer
            GL11.glPushMatrix();
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef((left ? -1.0F : 1.0F) / 16.0F, 2.0F / 16.0F, -10.0F / 16.0F);
            model.render(stack, p, left, false, using, partial);
            GL11.glPopMatrix();
            return;
        }
        GL11.glPushMatrix();
        if (left) {
            GL11.glScalef(-1.0F, 1.0F, 1.0F);
            GL11.glFrontFace(GL11.GL_CW);
        }
        GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
        vanillaThirdPerson(p, stack, using);
        if (left) {
            GL11.glFrontFace(GL11.GL_CCW);
        }
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** RenderPlayer.renderEquippedItems: the item's transforms by its kind, then the item. */
    private static void vanillaThirdPerson(EntityPlayer p, ItemStack stack, boolean using) {
        IItemRenderer custom = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.EQUIPPED);
        boolean is3D = custom != null && custom.shouldUseRenderHelper(
            IItemRenderer.ItemRenderType.EQUIPPED,
            stack,
            IItemRenderer.ItemRendererHelper.BLOCK_3D);
        float f;
        if (is3D || stack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(
            Block.getBlockFromItem(stack.getItem())
                .getRenderType())) {
            f = 0.375F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-f, -f, f);
        } else if (stack.getItem() == Items.bow) {
            f = 0.625F;
            GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
            GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(f, -f, f);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        } else if (stack.getItem()
            .isFull3D()) {
                f = 0.625F;
                if (stack.getItem()
                    .shouldRotateAroundWhenRendering()) {
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                }
                if (using && stack.getItemUseAction() == EnumAction.block) {
                    GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                    GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
                }
                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                GL11.glScalef(f, -f, f);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            } else {
                f = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(f, f, f);
                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }
        int passes = stack.getItem()
            .requiresMultipleRenderPasses()
                ? stack.getItem()
                    .getRenderPasses(stack.getItemDamage())
                : 1;
        for (int i = 0; i < passes; i++) {
            int c = stack.getItem()
                .getColorFromItemStack(stack, i);
            GL11.glColor4f((c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F, 1.0F);
            RenderManager.instance.itemRenderer.renderItem(p, stack, i);
        }
    }

    // ------------------------------------------------------------------ first person

    /** ItemRenderer.updateEquippedItem: the off hand's item goes down and comes up as the main hand's does. */
    public static void tickFirstPerson(EntityPlayer p) {
        prevOffProgress = offProgress;
        ItemStack now = Hands.of(p)
            .get();
        boolean same = now == offShown;
        if (now != null && offShown != null && now.getItem() == offShown.getItem()) {
            offShown = now;
            same = true;
        }
        offProgress += Math.max(-0.4F, Math.min(0.4F, (same ? 1.0F : 0.0F) - offProgress));
        if (offProgress < 0.1F) {
            offShown = now;
        }
    }

    /** The off hand's item was used: it dips, as ItemRenderer.resetEquippedProgress does for the main one. */
    public static void resetOffhand() {
        offProgress = 0.0F;
    }

    /** ItemRenderer.renderItemInFirstPerson. @return true if it was drawn here (vanilla's run must be skipped). */
    public static boolean firstPerson(ItemRenderer renderer, float partial) {
        if (inside) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP p = mc.thePlayer;
        AccessorItemRenderer a = (AccessorItemRenderer) renderer;
        ItemStack main = a.modernposes$getItemToRender();
        if (p.isUsingItem() && p.getItemInUse()
            .getItem() instanceof ScopeItem) {
            return true; // 26.3 draws no hands while looking through a scope
        }
        HeldModel mainModel = ModernHands.heldModel(main);
        if (offShown == null && mainModel == null) {
            return false;
        }
        Hands h = Hands.of(p);
        boolean offUsed = h.usedOff();
        boolean offSwing = h.swingOff;
        float swing = p.swingProgress, prevSwing = p.prevSwingProgress;
        int useCount = p.getItemInUseCount();
        float attack = p.getSwingProgress(partial);
        AccessorEntityPlayer use = (AccessorEntityPlayer) p;
        inside = true;
        try {
            // The main hand
            if (offSwing) {
                p.swingProgress = p.prevSwingProgress = 0.0F;
            }
            if (offUsed) {
                use.modernposes$setItemInUseCount(0);
            }
            float mainProgress = a.modernposes$getPrevEquippedProgress()
                + (a.modernposes$getEquippedProgress() - a.modernposes$getPrevEquippedProgress()) * partial;
            if (mainModel != null) {
                held(
                    mainModel,
                    main,
                    p,
                    partial,
                    mainProgress,
                    false,
                    useCount > 0 && !offUsed,
                    offSwing ? 0.0F : attack);
            } else {
                GL11.glPushMatrix();
                renderer.renderItemInFirstPerson(partial);
                GL11.glPopMatrix();
            }
            p.swingProgress = swing;
            p.prevSwingProgress = prevSwing;
            use.modernposes$setItemInUseCount(useCount);

            // The off hand
            if (offShown == null || offShown.getItem() instanceof ItemMap) {
                return true;
            }
            float offNow = prevOffProgress + (offProgress - prevOffProgress) * partial;
            HeldModel offModel = ModernHands.heldModel(offShown);
            if (offModel != null) {
                held(offModel, offShown, p, partial, offNow, true, useCount > 0 && offUsed, offSwing ? attack : 0.0F);
                return true;
            }
            if (!offSwing) {
                p.swingProgress = p.prevSwingProgress = 0.0F;
            }
            if (!offUsed) {
                use.modernposes$setItemInUseCount(0);
            }
            float equipped = a.modernposes$getEquippedProgress(),
                prevEquipped = a.modernposes$getPrevEquippedProgress();
            a.modernposes$setItemToRender(offShown);
            a.modernposes$setEquippedProgress(offProgress);
            a.modernposes$setPrevEquippedProgress(prevOffProgress);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glScalef(-1.0F, 1.0F, 1.0F);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glFrontFace(GL11.GL_CW);
            GL11.glPushMatrix();
            renderer.renderItemInFirstPerson(partial);
            GL11.glPopMatrix();
            GL11.glFrontFace(GL11.GL_CCW);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            a.modernposes$setItemToRender(main);
            a.modernposes$setEquippedProgress(equipped);
            a.modernposes$setPrevEquippedProgress(prevEquipped);
            p.swingProgress = swing;
            p.prevSwingProgress = prevSwing;
            use.modernposes$setItemInUseCount(useCount);
            return true;
        } finally {
            inside = false;
        }
    }

    /** A main hand's raised block item keeps its place while its damage changes, as 26.3's items do. */
    public static void keepRaised(ItemRenderer renderer, EntityPlayer p) {
        AccessorItemRenderer a = (AccessorItemRenderer) renderer;
        ItemStack now = p.inventory.getCurrentItem();
        ItemStack shown = a.modernposes$getItemToRender();
        if (now != null && shown != null
            && now != shown
            && now.getItem() == shown.getItem()
            && now.getItem() instanceof RaisedBlockItem) {
            a.modernposes$setItemToRender(now);
        }
    }

    /** 26.3's FirstPersonHandsAndItemsRenderer: the arm's place, its swing unless in use, then the item's model. */
    private static void held(HeldModel model, ItemStack stack, EntityClientPlayerMP p, float partial, float progress,
        boolean left, boolean using, float attack) {
        GL11.glPushMatrix();
        prologue(p, partial);
        float invert = left ? -1.0F : 1.0F;
        GL11.glTranslatef(invert * 0.56F, -0.52F + (1.0F - progress) * -0.6F, -0.72F);
        if (!using && !model.ownSwing(stack)) {
            float root = MathHelper.sqrt_float(attack);
            float pi = (float) Math.PI;
            GL11.glTranslatef(
                invert * -0.4F * MathHelper.sin(root * pi),
                0.2F * MathHelper.sin(root * pi * 2.0F),
                -0.2F * MathHelper.sin(attack * pi));
            GL11.glRotatef(invert * (45.0F + MathHelper.sin(attack * attack * pi) * -20.0F), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(invert * MathHelper.sin(root * pi) * -20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(MathHelper.sin(root * pi) * -80.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(invert * -45.0F, 0.0F, 1.0F, 0.0F);
        }
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        model.render(stack, p, left, true, using, partial);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();
    }

    /** The start of ItemRenderer.renderItemInFirstPerson: the light, the arm's lag, the brightness. */
    private static void prologue(EntityClientPlayerMP p, float partial) {
        GL11.glPushMatrix();
        GL11.glRotatef(p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * partial, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * partial, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        EntityPlayerSP sp = p;
        float armPitch = sp.prevRenderArmPitch + (sp.renderArmPitch - sp.prevRenderArmPitch) * partial;
        float armYaw = sp.prevRenderArmYaw + (sp.renderArmYaw - sp.prevRenderArmYaw) * partial;
        GL11.glRotatef((p.rotationPitch - armPitch) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((p.rotationYaw - armYaw) * 0.1F, 0.0F, 1.0F, 0.0F);
        int light = p.worldObj.getLightBrightnessForSkyBlocks(
            MathHelper.floor_double(p.posX),
            MathHelper.floor_double(p.posY),
            MathHelper.floor_double(p.posZ),
            0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
