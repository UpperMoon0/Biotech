package com.nstut.biotech.views.machines.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MachineMenu extends AbstractContainerMenu {
    protected MachineMenu(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    private com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity controller;
    private int syncedStatus, syncedMode;
    protected final void bindController(com.nstut.biotech.blocks.entites.machines.ControlledMachineBlockEntity machine) {
        controller = machine;
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            public int get() { return machine.getLevel() != null && !machine.getLevel().isClientSide() ? machine.getMachineStatus().ordinal() : syncedStatus; }
            public void set(int value) { syncedStatus = value; }
        });
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            public int get() { return machine.getLevel() != null && !machine.getLevel().isClientSide() ? machine.getRedstoneMode().ordinal() : syncedMode; }
            public void set(int value) { syncedMode = value; }
        });
    }
    public com.nstut.biotech.machines.MachineStatus getMachineStatus() { return com.nstut.biotech.machines.MachineStatus.fromId(syncedStatus); }
    public com.nstut.biotech.machines.RedstoneMode getRedstoneMode() { return com.nstut.biotech.machines.RedstoneMode.fromId(syncedMode); }
    public static final int CYCLE_REDSTONE_BUTTON = 90;
    @Override public boolean clickMenuButton(net.minecraft.world.entity.player.Player player, int id) {
        if (id != CYCLE_REDSTONE_BUTTON || player.containerMenu != this || controller == null || controller.getLevel() == null
                || controller.getLevel().isClientSide() || !stillValid(player)
                || controller.getLevel().getBlockEntity(controller.getBlockPos()) != controller) return false;
        controller.setRedstoneMode(controller.getRedstoneMode().next());
        broadcastChanges();
        return true;
    }

    public static List<Slot> createInventorySlots(Inventory inventory) {
        List<Slot> slots = new ArrayList<>(36);
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                slots.add(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, row * 18 + 84));
            }
        }
        for (int col = 0; col < 9; ++col) {
            slots.add(new Slot(inventory, col, 8 + col * 18, 142));
        }
        return List.copyOf(slots);
    }

    protected void addInventorySlots(Inventory inventory) {
        for (Slot slot : createInventorySlots(inventory)) {
            this.addSlot(slot);
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
