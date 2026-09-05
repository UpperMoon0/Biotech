package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.IOHatchBlock;
import com.nstut.biotech.blocks.entites.CapabilityBlockEntity;
import com.nstut.biotech.network.FluidHatchPacket;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.nstutlib.recipes.TransactionalFluidTankAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FluidHatchBlockEntity extends CapabilityBlockEntity {
    public static final int TANK_CAPACITY = FluidType.BUCKET_VOLUME * 32;

    protected final ItemStacksResourceHandler slots = new ItemStacksResourceHandler(2) {
        @Override protected void onContentsChanged(int index, ItemStack previousContents) { setChanged(); }
    };
    protected final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, TANK_CAPACITY) {
        @Override protected void onContentsChanged(int index, FluidStack previousContents) { setChanged(); }
    };

    private final IItemHandler internalSlots = IItemHandler.of(slots);
    private final IFluidHandler internalTankDelegate = IFluidHandler.of(tank);
    private final IFluidHandler internalTank = new TransactionalFluidTankAdapter(
            internalTankDelegate,
            TANK_CAPACITY,
            this::restoreFluid);
    private final ResourceHandler<FluidResource> externalTank = new ResourceHandler<>() {
        @Override public int size() { return tank.size(); }
        @Override public FluidResource getResource(int index) { return tank.getResource(index); }
        @Override public long getAmountAsLong(int index) { return tank.getAmountAsLong(index); }
        @Override public long getCapacityAsLong(int index, FluidResource resource) { return tank.getCapacityAsLong(index, resource); }
        @Override public boolean isValid(int index, FluidResource resource) { return isInputHatch() && tank.isValid(index, resource); }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return isInputHatch() ? tank.insert(index, resource, amount, transaction) : 0;
        }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return isInputHatch() ? 0 : tank.extract(index, resource, amount, transaction);
        }
    };

    private FluidStack lastSyncedFluid = FluidStack.EMPTY;

    protected FluidHatchBlockEntity(@NotNull BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    protected abstract boolean isInputHatch();

    /** Legacy views used by existing bucket handling and machine transaction code. */
    public final IItemHandler getInternalItemStorage() { return internalSlots; }
    public final IFluidHandler getInternalTank() { return internalTank; }

    /** Native NeoForge 26 item storage used by menus and transfer-native code. */
    public final ItemStacksResourceHandler getInternalItemResourceStorage() { return slots; }

    /** Native NeoForge 26 item capability for the hatch's bucket slots. */
    public final @Nullable ResourceHandler<ItemResource> getItemCapability(@Nullable Direction facing) {
        return facing == null ? slots : null;
    }

    /** Native NeoForge 26 fluid capability. */
    public final @Nullable ResourceHandler<FluidResource> getFluidCapability(@Nullable Direction facing) {
        if (facing == null) return tank;
        return facing == getBlockState().getValue(IOHatchBlock.FACING) ? externalTank : null;
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("item").ifPresent(slots::deserialize);
        input.child("fluid").ifPresent(tank::deserialize);
        lastSyncedFluid = FluidStack.EMPTY;
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        slots.serialize(output.child("item"));
        tank.serialize(output.child("fluid"));
    }

    public void dropItem() {
        if (level == null) return;
        SimpleContainer inventory = new SimpleContainer(slots.size());
        for (int i = 0; i < slots.size(); i++) inventory.setItem(i, ItemUtil.getStack(slots, i));
        Containers.dropContents(level, worldPosition, inventory);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T value) {
        if (!(value instanceof FluidHatchBlockEntity blockEntity) || level.isClientSide()) return;
        blockEntity.bucketHandling();
        FluidStack current = FluidUtil.getStack(blockEntity.tank, 0).copy();
        if (!sameFluidAndAmount(current, blockEntity.lastSyncedFluid)) {
            blockEntity.lastSyncedFluid = current.copy();
            if (level instanceof ServerLevel serverLevel) PacketRegistries.sendToTrackingChunk(serverLevel, pos, new FluidHatchPacket(current, pos));
        }
    }

    private void bucketHandling() {
        ItemStack input = internalSlots.getStackInSlot(0);
        ItemStack output = internalSlots.getStackInSlot(1);
        FluidStack current = internalTank.getFluidInTank(0);
        if (isInputHatch()) {
            if (input.is(Items.WATER_BUCKET)
                    && current.getAmount() <= TANK_CAPACITY - FluidType.BUCKET_VOLUME
                    && (output.isEmpty() || (output.is(Items.BUCKET) && output.getCount() < output.getMaxStackSize()))) {
                FluidStack water = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
                if (internalTank.fill(water, IFluidHandler.FluidAction.SIMULATE) == FluidType.BUCKET_VOLUME) {
                    internalTank.fill(water, IFluidHandler.FluidAction.EXECUTE);
                    internalSlots.extractItem(0, 1, false);
                    ItemStack remainder = internalSlots.insertItem(1, new ItemStack(Items.BUCKET), false);
                    if (!remainder.isEmpty()) throw new IllegalStateException("Fluid input hatch bucket output changed during commit");
                }
            }
        } else if (input.is(Items.BUCKET) && current.getAmount() >= FluidType.BUCKET_VOLUME
                && current.getFluid() == Fluids.WATER && output.isEmpty()) {
            FluidStack request = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
            FluidStack simulated = internalTank.drain(request, IFluidHandler.FluidAction.SIMULATE);
            if (simulated.getAmount() == FluidType.BUCKET_VOLUME) {
                internalTank.drain(request, IFluidHandler.FluidAction.EXECUTE);
                internalSlots.extractItem(0, 1, false);
                ItemStack remainder = internalSlots.insertItem(1, new ItemStack(Items.WATER_BUCKET), false);
                if (!remainder.isEmpty()) throw new IllegalStateException("Fluid output hatch bucket output changed during commit");
            }
        }
    }

    private void restoreFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) tank.set(0, FluidResource.EMPTY, 0);
        else tank.set(0, FluidResource.of(fluidStack), fluidStack.getAmount());
    }

    public void setFluid(FluidStack fluidStack) {
        restoreFluid(fluidStack);
    }

    private static boolean sameFluidAndAmount(FluidStack first, FluidStack second) {
        if (first.isEmpty() && second.isEmpty()) return true;
        return first.getAmount() == second.getAmount() && FluidStack.isSameFluidSameComponents(first, second);
    }
}
