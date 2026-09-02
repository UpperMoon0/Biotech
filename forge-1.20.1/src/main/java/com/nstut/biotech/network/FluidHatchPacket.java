package com.nstut.biotech.network;

import com.nstut.biotech.blocks.entites.hatches.FluidHatchBlockEntity;
import com.nstut.biotech.views.io_hatches.fluid.FluidHatchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FluidHatchPacket {
    private final FluidStack fluidStack;
    private final BlockPos pos;

    public FluidHatchPacket(FluidStack fluidStack, BlockPos pos) {
        this.fluidStack = fluidStack.copy();
        this.pos = pos;
    }

    public FluidHatchPacket(FriendlyByteBuf buf) {
        this.fluidStack = buf.readFluidStack();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFluidStack(fluidStack);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            LocalPlayer player = minecraft.player;
            if (level == null || player == null) {
                return;
            }
            if (level.getBlockEntity(pos) instanceof FluidHatchBlockEntity blockEntity) {
                blockEntity.setFluid(fluidStack);
                if (player.containerMenu instanceof FluidHatchMenu menu
                        && menu.getFluidHatchBlockEntity().getBlockPos().equals(pos)) {
                    menu.setFluidStack(fluidStack.copy());
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
