package com.nstut.biotech.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PacketRegistries {
    public static final String PROTOCOL_VERSION = "2";

    private static SimpleChannel instance;
    private static int packetId;

    private PacketRegistries() {
    }

    private static int id() {
        return packetId++;
    }

    public static void register() {
        packetId = 0;
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("biotech", "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        instance = net;

        net.messageBuilder(FluidHatchPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FluidHatchPacket::new)
                .encoder(FluidHatchPacket::toBytes)
                .consumerMainThread(FluidHatchPacket::handle)
                .add();
        net.messageBuilder(EnergyPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EnergyPacket::new)
                .encoder(EnergyPacket::toBytes)
                .consumerMainThread(EnergyPacket::handle)
                .add();
        net.messageBuilder(BreedingChamberPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(BreedingChamberPacket::new)
                .encoder(BreedingChamberPacket::toBytes)
                .consumerMainThread(BreedingChamberPacket::handle)
                .add();
        net.messageBuilder(TerrestrialHabitatPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TerrestrialHabitatPacket::new)
                .encoder(TerrestrialHabitatPacket::toBytes)
                .consumerMainThread(TerrestrialHabitatPacket::handle)
                .add();
        net.messageBuilder(SlaughterhousePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SlaughterhousePacket::new)
                .encoder(SlaughterhousePacket::toBytes)
                .consumerMainThread(SlaughterhousePacket::handle)
                .add();
        net.messageBuilder(GreenhousePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(GreenhousePacket::new)
                .encoder(GreenhousePacket::toBytes)
                .consumerMainThread(GreenhousePacket::handle)
                .add();
        net.messageBuilder(FermenterPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FermenterPacket::new)
                .encoder(FermenterPacket::toBytes)
                .consumerMainThread(FermenterPacket::handle)
                .add();
        net.messageBuilder(MixerPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MixerPacket::new)
                .encoder(MixerPacket::toBytes)
                .consumerMainThread(MixerPacket::handle)
                .add();
    }

    public static <MSG> void sendToTrackingChunk(ServerLevel level, BlockPos pos, MSG message) {
        if (instance != null && level.hasChunkAt(pos)) {
            instance.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), message);
        }
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        if (instance != null) {
            instance.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }
}
