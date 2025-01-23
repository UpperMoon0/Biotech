package com.nstut.biotech.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistries {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("biotech", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

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
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
