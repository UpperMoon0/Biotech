package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemHatchBlockEntity extends CapabilityBlockEntity {
    public static final int INVENTORY_SIZE = 9;

    protected final ItemStackHandler slots = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler externalSlots = new IItemHandler() {
        @Override
        public int getSlots() {
            return slots.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slots.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isInputHatch() ? slots.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isInputHatch() ? ItemStack.EMPTY : slots.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slots.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isInputHatch() && slots.isItemValid(slot, stack);
        }
    };

    private LazyOptional<IItemHandler> lazySlots = LazyOptional.of(() -> slots);
    private LazyOptional<IItemHandler> lazyExternalSlots = LazyOptional.of(() -> externalSlots);

    protected ItemHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract boolean isInputHatch();

    /** Internal mutable storage used by a validated multiblock machine transaction. */
    public final ItemStackHandler getInternalItemStorage() {
        return slots;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction facing) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (facing == null) {
                return lazySlots.cast();
            }
            if (facing == getBlockState().getValue(IOHatchBlock.FACING)) {
                return lazyExternalSlots.cast();
            }
        }
        return super.getCapability(cap, facing);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("item")) {
            slots.deserializeNBT(tag.getCompound("item"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("item", slots.serializeNBT());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazySlots.invalidate();
        lazyExternalSlots.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        lazySlots = LazyOptional.of(() -> slots);
        lazyExternalSlots = LazyOptional.of(() -> externalSlots);
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
}
