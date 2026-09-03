package com.nstut.biotech.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketRegistries {
    public static final String PROTOCOL_VERSION = "2";
    private PacketRegistries() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(FluidHatchPacket.TYPE, FluidHatchPacket.STREAM_CODEC, FluidHatchPacket::handle);
        registrar.playToClient(EnergyPacket.TYPE, EnergyPacket.STREAM_CODEC, EnergyPacket::handle);
        registrar.playToClient(BreedingChamberPacket.TYPE, BreedingChamberPacket.STREAM_CODEC, BreedingChamberPacket::handle);
        registrar.playToClient(TerrestrialHabitatPacket.TYPE, TerrestrialHabitatPacket.STREAM_CODEC, TerrestrialHabitatPacket::handle);
        registrar.playToClient(SlaughterhousePacket.TYPE, SlaughterhousePacket.STREAM_CODEC, SlaughterhousePacket::handle);
        registrar.playToClient(GreenhousePacket.TYPE, GreenhousePacket.STREAM_CODEC, GreenhousePacket::handle);
        registrar.playToClient(FermenterPacket.TYPE, FermenterPacket.STREAM_CODEC, FermenterPacket::handle);
        registrar.playToClient(MixerPacket.TYPE, MixerPacket.STREAM_CODEC, MixerPacket::handle);
    }

    public static void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload message) {
        if (level.hasChunkAt(pos)) PacketDistributor.sendToPlayersTrackingChunk(level, ChunkPos.containing(pos), message);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
