package com.nstut.biotech.network;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.client.ClientPacketHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class FluidHatchPacket implements CustomPacketPayload {
    public static final Type<FluidHatchPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "fluid_hatch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidHatchPacket> STREAM_CODEC = StreamCodec.ofMember(FluidHatchPacket::write, FluidHatchPacket::new);
    private final FluidStack fluidStack;
    private final BlockPos pos;
    public FluidHatchPacket(FluidStack fluidStack, BlockPos pos) { this.fluidStack = fluidStack.copy(); this.pos = pos; }
    private FluidHatchPacket(RegistryFriendlyByteBuf buf) { this.fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf); this.pos = buf.readBlockPos(); }
    private void write(RegistryFriendlyByteBuf buf) { FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, fluidStack); buf.writeBlockPos(pos); }
    public void handle(IPayloadContext context) { if (FMLEnvironment.getDist().isClient()) context.enqueueWork(() -> ClientPacketHandlers.handleFluidHatch(fluidStack, pos)); }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
