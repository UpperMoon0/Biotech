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
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class MixerPacket extends MultiblockMachinePacket implements CustomPacketPayload {
    public static final Type<MixerPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "mixer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MixerPacket> STREAM_CODEC =
            StreamCodec.ofMember(MixerPacket::write, MixerPacket::new);

    public MixerPacket(int energyCapacity,
                       int energyStored,
                       int energyConsumeRate,
                       int consumedEnergy,
                       int recipeEnergyCost,
                       boolean isStructureValid,
                       BlockPos pos,
                       ModRecipeData recipe) {
        this.energyCapacity = energyCapacity;
        this.energyStored = energyStored;
        this.energyConsumeRate = energyConsumeRate;
        this.consumedEnergy = consumedEnergy;
        this.recipeEnergyCost = recipeEnergyCost;
        this.isStructureValid = isStructureValid;
        this.pos = pos;
        this.recipe = recipe;
    }

    private MixerPacket(RegistryFriendlyByteBuf buf) {
        readEnergyPrefix(buf);
        readStateSuffix(buf);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        writeEnergyPrefix(buf);
        writeStateSuffix(buf);
    }

    public void handle(IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> ClientPacketHandlers.handleMixer(
                    energyCapacity, energyStored, energyConsumeRate, consumedEnergy,
                    recipeEnergyCost, isStructureValid, pos, recipe));
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
