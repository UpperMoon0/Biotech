package com.nstut.biotech.network;
import com.nstut.biotech.jei.SlaughterhouseLootPreview;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record LootPreviewPacket(String json) {
    public LootPreviewPacket(FriendlyByteBuf buf) { this(buf.readUtf(1048576)); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(json, 1048576); }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> SlaughterhouseLootPreview.receive(json));
        context.setPacketHandled(true);
    }
}
