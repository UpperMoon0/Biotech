package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.network.EnergyPacket;
import com.nstut.biotech.network.PacketRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EnergyHatchBlockEntity extends CapabilityBlockEntity {
    public static final int ENERGY_THROUGHPUT = 512;
    public static final int ENERGY_CAPACITY = ENERGY_THROUGHPUT * 1200;

    protected final DirtyEnergyStorage energyStorage = new DirtyEnergyStorage(
            ENERGY_CAPACITY,
            ENERGY_THROUGHPUT,
            ENERGY_THROUGHPUT,
            this::setChanged);

    private final IEnergyStorage externalEnergy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return energyStorage.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energyStorage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyStorage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private int lastSyncedEnergy = Integer.MIN_VALUE;

    protected EnergyHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Internal mutable energy storage used by a validated multiblock machine transaction. */
    public final EnergyStorage getInternalEnergyStorage() {
        return energyStorage;
    }

    /** NeoForge capability view. External automation may only receive energy on the hatch-facing side. */
    public final @Nullable IEnergyStorage getEnergyCapability(@Nullable Direction facing) {
        if (facing == null) {
            return energyStorage;
        }
        return facing == getBlockState().getValue(IOHatchBlock.FACING) ? externalEnergy : null;
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(registries, tag.get("energy"));
        }
        lastSyncedEnergy = Integer.MIN_VALUE;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energyStorage.serializeNBT(registries));
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T value) {
        if (!(value instanceof EnergyHatchBlockEntity blockEntity) || level.isClientSide) {
            return;
        }
        int current = blockEntity.energyStorage.getEnergyStored();
        if (current != blockEntity.lastSyncedEnergy) {
            blockEntity.lastSyncedEnergy = current;
            if (level instanceof ServerLevel serverLevel) {
                PacketRegistries.sendToTrackingChunk(serverLevel, pos, new EnergyPacket(current, pos));
            }
        }
    }

    /** Sets client mirror energy to an absolute value instead of accumulating packets. */
    public void setEnergy(int energy) {
        energyStorage.setAbsoluteEnergy(energy);
    }
}
