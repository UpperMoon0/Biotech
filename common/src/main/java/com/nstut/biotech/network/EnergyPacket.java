package com.nstut.biotech.network;

import com.nstut.biotech.blocks.entites.hatches.EnergyHatchBlockEntity;
import com.nstut.biotech.views.io_hatches.energy.EnergyHatchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnergyPacket {
    private final int energy;
    private final BlockPos pos;

    public EnergyPacket(int energy, BlockPos pos) {
        this.energy = energy;
        this.pos = pos;
    }

    public EnergyPacket(FriendlyByteBuf buf) {
        this.energy = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(energy);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if(level != null && level.getBlockEntity(pos) instanceof EnergyHatchBlockEntity blockEntity) {
                blockEntity.setEnergy(energy);

                LocalPlayer player = Minecraft.getInstance().player;
                if(player != null && player.containerMenu instanceof EnergyHatchMenu menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    menu.setEnergy(energy);
                }
            }
        });
        return true;
    }
}
