package com.nhat.biotech.view.io_hatches.fluid;

import com.nhat.biotech.blocks.IModBlocks;
import com.nhat.biotech.view.BiotechMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public abstract class FluidHatchMenu extends BiotechMenu {
    private final BlockEntity blockEntity;
    private final Level level;
    private FluidStack fluidStack;
    public FluidHatchMenu(MenuType menu, int pContainerId, Inventory inventory, BlockEntity blockEntity) {
        super(menu, pContainerId);
        this.blockEntity = blockEntity;
        level = inventory.player.level();
        fluidStack = FluidStack.EMPTY;

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
            addSlot(new SlotItemHandler(h, 0, 98, 17));
            addSlot(new SlotItemHandler(h, 1, 98, 53) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
            h.getSlots();
        });

        addInventorySlots(inventory);
    }
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return adaptiveQuickMoveStack(pIndex, 2, 1);
    }
    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, IModBlocks.FLUID_INPUT_HATCH.get())
                || stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, IModBlocks.FLUID_OUTPUT_HATCH.get());
    }
    public FluidStack getFluidStack() {
        return fluidStack;
    }
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }
}
