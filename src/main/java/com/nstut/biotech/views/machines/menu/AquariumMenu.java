package com.nstut.biotech.views.machines.menu;

import com.nstut.biotech.blocks.entites.machines.AquariumBlockEntity;
import com.nstut.biotech.machines.MachineRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AquariumMenu extends AbstractContainerMenu {

    private final AquariumBlockEntity blockEntity;
    private final Level level;

    public AquariumMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public AquariumMenu(int id, Inventory inv, BlockEntity entity) {
        super(MachineRegistries.AQUARIUM_MENU.get(), id);
        this.blockEntity = ((AquariumBlockEntity) entity);
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Example slot, adjust as needed for Aquarium
        // this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
        //     this.addSlot(new SlotItemHandler(handler, 0, 80, 35));
        // });
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the clicked slot is one of the block entity's slots
        int entitySlotsStart = 36; // Player inventory + hotbar
        int entitySlotsEnd = entitySlotsStart + blockEntity.getContainerSize(); // Assuming getContainerSize() exists

        if (index < entitySlotsStart) { // From player inventory/hotbar to block entity
            if (!moveItemStackTo(sourceStack, entitySlotsStart, entitySlotsEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= entitySlotsStart && index < entitySlotsEnd) { // From block entity to player inventory/hotbar
            if (!moveItemStackTo(sourceStack, 0, entitySlotsStart, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, copyOfSourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, MachineRegistries.AQUARIUM_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}