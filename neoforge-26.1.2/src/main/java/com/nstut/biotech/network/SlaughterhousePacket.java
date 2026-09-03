package com.nstut.biotech.network;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.client.ClientPacketHandlers;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class SlaughterhousePacket extends MultiblockMachinePacket implements CustomPacketPayload {
    public static final Type<SlaughterhousePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "slaughterhouse"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SlaughterhousePacket> STREAM_CODEC =
            StreamCodec.ofMember(SlaughterhousePacket::write, SlaughterhousePacket::new);

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
        this.fluidStored = fluidStored.copy();
        this.isStructureValid = isStructureValid;
        this.pos = pos;
        this.recipe = recipe;
    }

    private SlaughterhousePacket(RegistryFriendlyByteBuf buf) {
        readEnergyPrefix(buf);
        this.fluidCapacity = buf.readInt();
        this.fluidStored = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
        readStateSuffix(buf);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        writeEnergyPrefix(buf);
        buf.writeInt(fluidCapacity);
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, fluidStored);
        writeStateSuffix(buf);
    }

    public void handle(IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> ClientPacketHandlers.handleSlaughterhouse(
                    energyCapacity, energyStored, energyConsumeRate, consumedEnergy, recipeEnergyCost,
                    fluidCapacity, fluidStored, isStructureValid, pos, recipe));
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
