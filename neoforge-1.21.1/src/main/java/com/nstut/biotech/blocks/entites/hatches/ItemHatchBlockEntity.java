package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
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

    protected ItemHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract boolean isInputHatch();

    /** Internal mutable storage used by a validated multiblock machine transaction. */
    public final ItemStackHandler getInternalItemStorage() {
        return slots;
    }

    /** NeoForge capability view. Null side is reserved for the machine's internal mutable storage. */
    public final @Nullable IItemHandler getItemCapability(@Nullable Direction facing) {
        if (facing == null) {
            return slots;
        }
        return facing == getBlockState().getValue(IOHatchBlock.FACING) ? externalSlots : null;
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("item")) {
            slots.deserializeNBT(registries, tag.getCompound("item"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("item", slots.serializeNBT(registries));
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
