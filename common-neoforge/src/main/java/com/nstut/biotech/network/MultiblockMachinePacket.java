package com.nstut.biotech.network;

import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public abstract class MultiblockMachinePacket {
    protected int energyCapacity;
    protected int energyStored;
    protected int energyConsumeRate;
    protected int consumedEnergy;
    protected int recipeEnergyCost;
    protected boolean isStructureValid;
    protected BlockPos pos;
    protected ModRecipeData recipe;

    protected void readEnergyPrefix(RegistryFriendlyByteBuf buf) {
        energyCapacity = buf.readInt();
        energyStored = buf.readInt();
        energyConsumeRate = buf.readInt();
        consumedEnergy = buf.readInt();
        recipeEnergyCost = buf.readInt();
    }

    protected void writeEnergyPrefix(RegistryFriendlyByteBuf buf) {
        buf.writeInt(energyCapacity);
        buf.writeInt(energyStored);
        buf.writeInt(energyConsumeRate);
        buf.writeInt(consumedEnergy);
        buf.writeInt(recipeEnergyCost);
    }

    protected void readStateSuffix(RegistryFriendlyByteBuf buf) {
        isStructureValid = buf.readBoolean();
        pos = buf.readBlockPos();
        recipe = buf.readBoolean() ? ModRecipeData.fromBuf(buf) : null;
    }

    protected void writeStateSuffix(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(isStructureValid);
        buf.writeBlockPos(pos);
        buf.writeBoolean(recipe != null);
        if (recipe != null) {
            recipe.writeToBuf(buf);
        }
    }
}
