package com.nstut.biotech.network;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class EnergyPacket implements CustomPacketPayload {
    public static final Type<EnergyPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "energy_hatch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyPacket> STREAM_CODEC = StreamCodec.ofMember(EnergyPacket::write, EnergyPacket::new);
    private final int energy;
    private final BlockPos pos;
    public EnergyPacket(int energy, BlockPos pos) { this.energy = energy; this.pos = pos; }
    private EnergyPacket(RegistryFriendlyByteBuf buf) { this.energy = buf.readInt(); this.pos = buf.readBlockPos(); }
    private void write(RegistryFriendlyByteBuf buf) { buf.writeInt(energy); buf.writeBlockPos(pos); }
    public void handle(IPayloadContext context) { if (FMLEnvironment.dist.isClient()) context.enqueueWork(() -> ClientPacketHandlers.handleEnergy(energy, pos)); }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
