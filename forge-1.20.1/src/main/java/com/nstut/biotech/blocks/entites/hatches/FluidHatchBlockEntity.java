package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.network.FluidHatchPacket;
import com.nstut.biotech.network.PacketRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FluidHatchBlockEntity extends CapabilityBlockEntity {
    public static final int TANK_CAPACITY = FluidType.BUCKET_VOLUME * 32;

    protected final ItemStackHandler slots = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    protected final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final IFluidHandler externalTank = new IFluidHandler() {
        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
            return isInputHatch() && tank.isFluidValid(tankIndex, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return isInputHatch() ? tank.fill(resource, action) : 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return isInputHatch() ? FluidStack.EMPTY : tank.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return isInputHatch() ? FluidStack.EMPTY : tank.drain(maxDrain, action);
        }
    };

    private LazyOptional<IItemHandler> lazySlots = LazyOptional.of(() -> slots);
    private LazyOptional<IFluidHandler> lazyTank = LazyOptional.of(() -> tank);
    private LazyOptional<IFluidHandler> lazyExternalTank = LazyOptional.of(() -> externalTank);

    private FluidStack lastSyncedFluid = FluidStack.EMPTY;

    protected FluidHatchBlockEntity(@NotNull BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract boolean isInputHatch();

    /** Internal restorable tank used by a validated multiblock machine transaction. */
    public final FluidTank getInternalTank() {
        return tank;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (facing == null) {
                return lazyTank.cast();
            }
            if (facing == getBlockState().getValue(IOHatchBlock.FACING)) {
                return lazyExternalTank.cast();
            }
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER && facing == null) {
            return lazySlots.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("item")) {
            slots.deserializeNBT(tag.getCompound("item"));
        }
        if (tag.contains("fluid")) {
            tank.readFromNBT(tag.getCompound("fluid"));
        }
        lastSyncedFluid = FluidStack.EMPTY;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("item", slots.serializeNBT());
        CompoundTag fluidTag = new CompoundTag();
        tank.writeToNBT(fluidTag);
        tag.put("fluid", fluidTag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazySlots.invalidate();
        lazyTank.invalidate();
        lazyExternalTank.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        lazySlots = LazyOptional.of(() -> slots);
        lazyTank = LazyOptional.of(() -> tank);
        lazyExternalTank = LazyOptional.of(() -> externalTank);
    }

    public void dropItem() {
        if (level == null) {
            return;
        }
        SimpleContainer inventory = new SimpleContainer(slots.getSlots());
        for (int i = 0; i < slots.getSlots(); i++) {
            inventory.setItem(i, slots.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T value) {
        if (!(value instanceof FluidHatchBlockEntity blockEntity) || level.isClientSide()) {
            return;
        }

        blockEntity.bucketHandling();
        FluidStack current = blockEntity.tank.getFluid().copy();
        if (!sameFluidAndAmount(current, blockEntity.lastSyncedFluid)) {
            blockEntity.lastSyncedFluid = current.copy();
            if (level instanceof ServerLevel serverLevel) {
                PacketRegistries.sendToTrackingChunk(serverLevel, pos, new FluidHatchPacket(current, pos));
            }
        }
    }

    private void bucketHandling() {
        ItemStack input = slots.getStackInSlot(0);
        ItemStack output = slots.getStackInSlot(1);

        if (isInputHatch()) {
            if (input.is(Items.WATER_BUCKET)
                    && tank.getFluidAmount() <= tank.getCapacity() - FluidType.BUCKET_VOLUME
                    && (output.isEmpty() || (output.is(Items.BUCKET) && output.getCount() < output.getMaxStackSize()))) {
                int accepted = tank.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE);
                if (accepted == FluidType.BUCKET_VOLUME) {
                    tank.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                    slots.extractItem(0, 1, false);
                    ItemStack remainder = slots.insertItem(1, new ItemStack(Items.BUCKET), false);
                    if (!remainder.isEmpty()) {
                        throw new IllegalStateException("Fluid input hatch bucket output changed during commit");
                    }
                }
            }
        } else if (input.is(Items.BUCKET)
                && tank.getFluidAmount() >= FluidType.BUCKET_VOLUME
                && tank.getFluid().getFluid() == Fluids.WATER
                && output.isEmpty()) {
            FluidStack request = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
            FluidStack simulated = tank.drain(request, IFluidHandler.FluidAction.SIMULATE);
            if (simulated.getAmount() == FluidType.BUCKET_VOLUME) {
                tank.drain(request, IFluidHandler.FluidAction.EXECUTE);
                slots.extractItem(0, 1, false);
                ItemStack remainder = slots.insertItem(1, new ItemStack(Items.WATER_BUCKET), false);
                if (!remainder.isEmpty()) {
                    throw new IllegalStateException("Fluid output hatch bucket output changed during commit");
                }
            }
        }
    }

    public void setFluid(FluidStack fluidStack) {
        tank.setFluid(fluidStack.copy());
    }

    private static boolean sameFluidAndAmount(FluidStack first, FluidStack second) {
        if (first.isEmpty() && second.isEmpty()) {
            return true;
        }
        return first.getAmount() == second.getAmount() && first.isFluidEqual(second);
    }
}
