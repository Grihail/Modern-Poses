package com.grihail.modernposes.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/** 26.3's Player.attackStrengthTicker and itemSwapTicker; not saved, as in 26.3 they start over with the entity. */
public final class AttackState implements IExtendedEntityProperties {

    private static final String KEY = "modernposes.attack";

    /** Ticks since the last attack (or the change of the main hand's item). */
    public int strengthTicker;
    /** Ticks since the main hand's item changed or a client attack: what the first person's arm follows. */
    public int swapTicker;
    /** What the main hand held on the last tick. */
    public ItemStack lastMain;

    private AttackState() {}

    public static AttackState of(EntityPlayer p) {
        AttackState s = (AttackState) p.getExtendedProperties(KEY);
        if (s == null) {
            s = new AttackState();
            p.registerExtendedProperties(KEY, s);
        }
        return s;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {}

    @Override
    public void loadNBTData(NBTTagCompound compound) {}

    @Override
    public void init(Entity entity, World world) {}
}
