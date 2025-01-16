package com.nstut.biotech.network;

import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.core.BlockPos;

public abstract class MultiblockMachinePacket {

    protected int energyCapacity;
    protected int energyStored;
    protected int energyConsumeRate;
    protected int consumedEnergy;
    protected int recipeEnergyCost;
    protected boolean isStructureValid;
    protected BlockPos pos;
    protected ModRecipeData recipe;
}
