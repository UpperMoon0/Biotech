package com.nstut.biotech.views.machines.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class MachineMenu extends AbstractContainerMenu {
    protected MachineMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    protected void addInventorySlots(Inventory inventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, row * 18 + 84));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    protected ItemStack adaptiveQuickMoveStack(int pIndex, int containerSlotsCount, int outputIndexStart) {
        // Get the clicked slot
        Slot slot = this.slots.get(pIndex);
        // Return empty stack if slot is empty or invalid
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        // Get the item stack in the slot
        ItemStack stack = slot.getItem();
        // Make a copy of the stack to return later
        ItemStack itemstack = stack.copy();
        // If the index is in the input slots of the container
        if (pIndex >= 0 && pIndex < outputIndexStart) {
            // Try to move the stack to the player inventory
            if (!this.moveItemStackTo(stack, containerSlotsCount, containerSlotsCount + 36, true)) {
                return ItemStack.EMPTY;
            }
        }
        // If the index is in the cropId slots of the container
        else if (pIndex >= outputIndexStart && pIndex < containerSlotsCount) {
            // Try to move the stack to the player inventory
            if (!this.moveItemStackTo(stack, containerSlotsCount, containerSlotsCount + 36, true)) {
                return ItemStack.EMPTY;
            }
        }
        // If the index is in the player inventory
        else if (pIndex >= containerSlotsCount && pIndex < containerSlotsCount + 36) {
            // Try to move the stack to the input slots of the container
            if (!this.moveItemStackTo(stack, 0, outputIndexStart, false)) {
                return ItemStack.EMPTY;
            }
        }
        // Set slot content to empty if stack is empty after moving
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }
        // Otherwise, notify slot of changes
        else {
            slot.setChanged();
        }
        // Return copied stack
        return itemstack;
    }
}
