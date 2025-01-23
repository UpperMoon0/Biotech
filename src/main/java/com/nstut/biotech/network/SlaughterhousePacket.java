package com.nstut.biotech.network;

import com.nstut.biotech.blocks.entites.machines.SlaughterhouseBlockEntity;
import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SlaughterhousePacket extends MultiblockMachinePacket {

    private final int fluidCapacity;
    private final FluidStack fluidStored;

    public SlaughterhousePacket(int energyCapacity,
                                int energyStored,
                                int energyConsumeRate,
                                int consumedEnergy,
                                int recipeEnergyCost,
                                int fluidCapacity,
                                FluidStack fluidStored,
                                boolean isStructureValid,
                                BlockPos pos,
                                ModRecipeData recipe) {
        this.energyCapacity = energyCapacity;
        this.energyStored = energyStored;
        this.energyConsumeRate = energyConsumeRate;
        this.consumedEnergy = consumedEnergy;
        this.recipeEnergyCost = recipeEnergyCost;
        this.fluidCapacity = fluidCapacity;
        this.fluidStored = fluidStored;
        this.isStructureValid = isStructureValid;
        this.pos = pos;
        this.recipe = recipe;
    }

    public SlaughterhousePacket(FriendlyByteBuf buf) {
        this.energyCapacity = buf.readInt();
        this.energyStored = buf.readInt();
        this.energyConsumeRate = buf.readInt();
        this.consumedEnergy = buf.readInt();
        this.recipeEnergyCost = buf.readInt();
        this.fluidCapacity = buf.readInt();
        this.fluidStored = buf.readFluidStack();
        this.isStructureValid = buf.readBoolean();
        this.pos = buf.readBlockPos();
        boolean hasRecipe = buf.readBoolean();
        if(hasRecipe) {
            this.recipe = ModRecipeData.fromBuf(buf);
        } else {
            this.recipe = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(energyCapacity);
        buf.writeInt(energyStored);
        buf.writeInt(energyConsumeRate);
        buf.writeInt(consumedEnergy);
        buf.writeInt(recipeEnergyCost);
        buf.writeInt(fluidCapacity);
        buf.writeFluidStack(fluidStored);
        buf.writeBoolean(isStructureValid);
        buf.writeBlockPos(pos);
        boolean hasRecipe = recipe != null;
        buf.writeBoolean(hasRecipe);
        if(hasRecipe) {
            recipe.writeToBuf(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(pos) instanceof SlaughterhouseBlockEntity) {
                LocalPlayer player = Minecraft.getInstance().player;
                if(player != null
                        && player.containerMenu instanceof SlaughterhouseMenu menu
                        && menu.getBlockEntity().getBlockPos().equals(pos)) {
                    menu.setEnergyCapacity(energyCapacity);
                    menu.setEnergyStored(energyStored);
                    menu.setEnergyConsumeRate(energyConsumeRate);
                    menu.setEnergyConsumed(consumedEnergy);
                    menu.setRecipeEnergyCost(recipeEnergyCost);
                    menu.setFluidCapacity(fluidCapacity);
                    menu.setFluidStored(fluidStored);
                    menu.setStructureValid(isStructureValid);
                    menu.setRecipe(recipe);
                }
            }
        });
    }
}

