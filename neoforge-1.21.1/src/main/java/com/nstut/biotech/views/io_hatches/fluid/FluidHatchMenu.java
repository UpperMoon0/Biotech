package com.nstut.biotech.views.io_hatches.fluid;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.FluidHatchBlockEntity;
import com.nstut.biotech.views.machines.menu.MachineMenu;
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

public abstract class FluidHatchMenu extends MachineMenu {
    private final FluidHatchBlockEntity BLOCK_ENTITY;
    private final Level LEVEL;
    private FluidStack fluidStack;
    // Getters
    public FluidStack getFluidStack() {
        return fluidStack;
    }
    public FluidHatchBlockEntity getFluidHatchBlockEntity() {
        return BLOCK_ENTITY;
    }
    // Setters
    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }
    public FluidHatchMenu(MenuType menu, int pContainerId, Inventory inventory, BlockEntity blockEntity) {
        super(menu, pContainerId);
        this.BLOCK_ENTITY = (FluidHatchBlockEntity) blockEntity;
        LEVEL = inventory.player.level();
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
        return stillValid(ContainerLevelAccess.create(LEVEL, BLOCK_ENTITY.getBlockPos()), pPlayer, BlockRegistries.FLUID_INPUT_HATCH.get())
                || stillValid(ContainerLevelAccess.create(LEVEL, BLOCK_ENTITY.getBlockPos()), pPlayer, BlockRegistries.FLUID_OUTPUT_HATCH.get());
    }
}
