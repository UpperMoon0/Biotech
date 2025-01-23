package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public abstract class ItemHatchBlockEntity extends CapabilityBlockEntity {
    public final int INVENTORY_SIZE = 9;
    protected final ItemStackHandler slots = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    protected final LazyOptional<IItemHandler> lazySlots = LazyOptional.of(() -> slots);

    protected ItemHatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction facing) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            if (facing == getBlockState().getValue(IOHatchBlock.FACING) || facing == null)
                return lazySlots.cast();
        }
        return super.getCapability(cap, facing);
    }

    public void dropItem() {
        SimpleContainer inventory = new SimpleContainer(slots.getSlots());
        for (int i = 0; i < slots.getSlots(); i++) {
            inventory.setItem(i, slots.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
}
