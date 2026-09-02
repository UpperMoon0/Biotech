package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.network.EnergyPacket;
import com.nstut.biotech.network.PacketRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EnergyHatchBlockEntity extends CapabilityBlockEntity {
    public static final int ENERGY_THROUGHPUT = 512;
    public static final int ENERGY_CAPACITY = ENERGY_THROUGHPUT * 1200;

    protected final EnergyStorage energyStorage = new EnergyStorage(
            ENERGY_CAPACITY,
            ENERGY_THROUGHPUT,
            ENERGY_THROUGHPUT);

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

    private LazyOptional<IEnergyStorage> lazyEnergyStorage = LazyOptional.of(() -> energyStorage);
    private LazyOptional<IEnergyStorage> lazyExternalEnergy = LazyOptional.of(() -> externalEnergy);
    private int lastSyncedEnergy = Integer.MIN_VALUE;

    protected EnergyHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ENERGY) {
            if (facing == null) {
                return lazyEnergyStorage.cast();
            }
            if (facing == getBlockState().getValue(IOHatchBlock.FACING)) {
                return lazyExternalEnergy.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(tag.get("energy"));
        }
        lastSyncedEnergy = Integer.MIN_VALUE;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energyStorage.serializeNBT());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyStorage.invalidate();
        lazyExternalEnergy.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        lazyEnergyStorage = LazyOptional.of(() -> energyStorage);
        lazyExternalEnergy = LazyOptional.of(() -> externalEnergy);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T value) {
        if (!(value instanceof EnergyHatchBlockEntity blockEntity) || level.isClientSide()) {
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
        int clamped = Math.max(0, Math.min(ENERGY_CAPACITY, energy));
        energyStorage.deserializeNBT(IntTag.valueOf(clamped));
    }
}
