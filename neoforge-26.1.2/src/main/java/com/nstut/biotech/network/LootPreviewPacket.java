package com.nstut.biotech.network;
import com.nstut.biotech.jei.SlaughterhouseLootPreview;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record LootPreviewPacket(String json) implements CustomPacketPayload {
    public static final Type<LootPreviewPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("biotech", "loot_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LootPreviewPacket> STREAM_CODEC = StreamCodec.ofMember(
            (packet, buf) -> buf.writeUtf(packet.json, 1048576), buf -> new LootPreviewPacket(buf.readUtf(1048576)));
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public void handle(IPayloadContext context) { context.enqueueWork(() -> SlaughterhouseLootPreview.receive(json)); }
}
