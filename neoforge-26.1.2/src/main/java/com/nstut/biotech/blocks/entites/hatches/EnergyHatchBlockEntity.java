package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.network.EnergyPacket;
import com.nstut.biotech.network.PacketRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public abstract class EnergyHatchBlockEntity extends CapabilityBlockEntity {
    public static final int ENERGY_THROUGHPUT = 512;
    public static final int ENERGY_CAPACITY = ENERGY_THROUGHPUT * 1200;

    protected final DirtyEnergyStorage energyStorage = new DirtyEnergyStorage(
            ENERGY_CAPACITY, ENERGY_THROUGHPUT, ENERGY_THROUGHPUT, this::setChanged);
    private final IEnergyStorage internalEnergy = IEnergyStorage.of(energyStorage);
    private final EnergyHandler externalEnergy = new EnergyHandler() {
        @Override public long getAmountAsLong() { return energyStorage.getAmountAsLong(); }
        @Override public long getCapacityAsLong() { return energyStorage.getCapacityAsLong(); }
        @Override public int insert(int amount, TransactionContext transaction) {
            return energyStorage.insert(amount, transaction);
        }
        @Override public int extract(int amount, TransactionContext transaction) { return 0; }
    };

    private int lastSyncedEnergy = Integer.MIN_VALUE;

    protected EnergyHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final IEnergyStorage getInternalEnergyStorage() {
        return internalEnergy;
    }

    public final @Nullable EnergyHandler getEnergyCapability(@Nullable Direction facing) {
        if (facing == null) return energyStorage;
        return facing == getBlockState().getValue(IOHatchBlock.FACING) ? externalEnergy : null;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energyStorage.setAbsoluteEnergy(input.getIntOr("energy", 0));
        lastSyncedEnergy = Integer.MIN_VALUE;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", energyStorage.getAmountAsInt());
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T value) {
        if (!(value instanceof EnergyHatchBlockEntity blockEntity) || level.isClientSide()) return;
        int current = blockEntity.energyStorage.getAmountAsInt();
        if (current != blockEntity.lastSyncedEnergy) {
            blockEntity.lastSyncedEnergy = current;
            if (level instanceof ServerLevel serverLevel) {
                PacketRegistries.sendToTrackingChunk(serverLevel, pos, new EnergyPacket(current, pos));
            }
        }
    }

    public void setEnergy(int energy) {
        energyStorage.setAbsoluteEnergy(energy);
    }
}
