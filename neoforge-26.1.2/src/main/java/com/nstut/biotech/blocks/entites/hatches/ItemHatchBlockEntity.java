package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public abstract class ItemHatchBlockEntity extends CapabilityBlockEntity {
    public static final int INVENTORY_SIZE = 9;

    protected final ItemStacksResourceHandler slots = new ItemStacksResourceHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    };

    private final IItemHandler legacyInternalSlots = IItemHandler.of(slots);
    private final IItemHandlerModifiable internalSlots = new IItemHandlerModifiable() {
        @Override public void setStackInSlot(int index, ItemStack stack) {
            try (Transaction transaction = Transaction.openRoot()) {
                int currentAmount = Math.toIntExact(slots.getAmountAsLong(index));
                if (currentAmount > 0) {
                    ItemResource current = slots.getResource(index);
                    int extracted = slots.extract(index, current, currentAmount, transaction);
                    if (extracted != currentAmount) {
                        throw new IllegalStateException("Failed to replace item hatch slot " + index);
                    }
                }
                if (!stack.isEmpty()) {
                    int inserted = slots.insert(index, ItemResource.of(stack), stack.getCount(), transaction);
                    if (inserted != stack.getCount()) {
                        throw new IllegalArgumentException("Stack does not fit item hatch slot " + index);
                    }
                }
                transaction.commit();
            }
        }
        @Override public int getSlots() { return legacyInternalSlots.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return legacyInternalSlots.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return legacyInternalSlots.insertItem(slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return legacyInternalSlots.extractItem(slot, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return legacyInternalSlots.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return legacyInternalSlots.isItemValid(slot, stack); }
    };
    private final ResourceHandler<ItemResource> externalSlots = new ResourceHandler<>() {
        @Override public int size() { return slots.size(); }
        @Override public ItemResource getResource(int index) { return slots.getResource(index); }
        @Override public long getAmountAsLong(int index) { return slots.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, ItemResource resource) {
            return isInputHatch() ? slots.getCapacityAsLong(index, resource) : 0;
        }
        @Override public boolean isValid(int index, ItemResource resource) {
            return isInputHatch() && slots.isValid(index, resource);
        }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return isInputHatch() ? slots.insert(index, resource, amount, transaction) : 0;
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return isInputHatch() ? 0 : slots.extract(index, resource, amount, transaction);
        }
    };

    protected ItemHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract boolean isInputHatch();

    /** Legacy view used by the existing machine transaction and menu code. */
    public final IItemHandlerModifiable getInternalItemStorage() {
        return internalSlots;
    }

    /** Native NeoForge 26 transfer capability. */
    public final @Nullable ResourceHandler<ItemResource> getItemCapability(@Nullable Direction facing) {
        if (facing == null) return slots;
        return facing == getBlockState().getValue(IOHatchBlock.FACING) ? externalSlots : null;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("item").ifPresent(slots::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        slots.serialize(output.child("item"));
    }

    public void dropItem() {
        if (level == null) return;
        SimpleContainer inventory = new SimpleContainer(slots.size());
        for (int i = 0; i < slots.size(); i++) {
            inventory.setItem(i, ItemUtil.getStack(slots, i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }
}
