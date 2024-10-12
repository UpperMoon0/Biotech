package com.nhat.biotech.networking;

import com.nhat.biotech.blocks.block_entites.machines.TerrestrialHabitatBlockEntity;
import com.nhat.biotech.recipes.BiotechRecipeData;
import com.nhat.biotech.view.machines.menu.TerrestrialHabitatMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TerrestrialHabitatPacket extends MultiblockMachinePacket {

    private final int fluidCapacity;
    private final FluidStack fluidStored;

    public TerrestrialHabitatPacket(int energyCapacity,
                                 int energyStored,
                                 int energyConsumeRate,
                                 int consumedEnergy,
                                 int recipeEnergyCost,
                                 int fluidCapacity,
                                 FluidStack fluidStored,
                                 boolean isStructureValid,
                                 BlockPos pos,
                                 BiotechRecipeData recipe) {
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

    public TerrestrialHabitatPacket(FriendlyByteBuf buf) {
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
            this.recipe = BiotechRecipeData.fromBuf(buf);
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
            if(Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(pos) instanceof TerrestrialHabitatBlockEntity) {
                LocalPlayer player = Minecraft.getInstance().player;
                if(player != null
                        && player.containerMenu instanceof TerrestrialHabitatMenu menu
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
