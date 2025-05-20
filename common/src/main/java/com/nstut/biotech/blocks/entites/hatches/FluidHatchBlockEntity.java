package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.network.FluidHatchPacket;
import com.nstut.biotech.network.PacketRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
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

    protected FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    protected final LazyOptional<IItemHandler> lazySlots = LazyOptional.of(() -> slots);

    protected final LazyOptional<IFluidHandler> lazyTank = LazyOptional.of(() -> tank);

    public FluidHatchBlockEntity(@NotNull BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state)
    {
        super(blockEntityType, pos, state);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == ForgeCapabilities.FLUID_HANDLER)
            if (facing == getBlockState().getValue(IOHatchBlock.FACING) || facing == null)
                return lazyTank.cast();
        if(capability == ForgeCapabilities.ITEM_HANDLER) {
            if (facing == null)
                return lazySlots.cast();
        }
        return super.getCapability(capability, facing);
    }

    public void dropItem() {
        SimpleContainer inventory = new SimpleContainer(slots.getSlots());
        for (int i = 0; i < slots.getSlots(); i++) {
            inventory.setItem(i, slots.getStackInSlot(i));
        }

        Containers.dropContents(level, this.worldPosition, inventory);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        FluidHatchBlockEntity blockEntity = (FluidHatchBlockEntity) t;
        if (!level.isClientSide()) {
            blockEntity.bucketHandling(blockEntity);
            PacketRegistries.sendToClients(new FluidHatchPacket(blockEntity.tank.getFluid(), blockPos));
            setChanged(level, blockPos, blockState);
        }
    }

    protected void bucketHandling(FluidHatchBlockEntity blockEntity) {
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            ItemStack slot0 = handler.getStackInSlot(0);
            ItemStack slot1 = handler.getStackInSlot(1);
            Item item0 = slot0.getItem();
            int tankCapacity = tank.getCapacity();
            int tankAmount = tank.getFluidAmount();
            Fluid tankFluid = tank.getFluidInTank(0).getFluid();

            if (item0.equals(Items.WATER_BUCKET)
                    && tankAmount <= tankCapacity - 1000
                    && (slot1.isEmpty() || (slot1.getItem().equals(Items.BUCKET)) && slot1.getCount() < 64)) {
                tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                handler.extractItem(0, 1, false);
                handler.insertItem(1, Items.BUCKET.getDefaultInstance(), false);
            }
            else if (item0.equals(Items.BUCKET)
                    && tankAmount >= 1000
                    && tankFluid.equals(Fluids.WATER)
                    && slot1.isEmpty()) {
                tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                handler.extractItem(0, 1, false);
                handler.insertItem(1, Items.WATER_BUCKET.getDefaultInstance(), false);
            }
        });
    }

    public void setFluid(FluidStack fluidStack) {
        tank.setFluid(fluidStack);
    }
}
