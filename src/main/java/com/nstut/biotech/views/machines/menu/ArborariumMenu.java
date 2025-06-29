package com.nstut.biotech.views.machines.menu;

import com.nstut.biotech.blocks.entites.machines.ArborariumBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class ArborariumMenu extends AbstractContainerMenu {

    public final ArborariumBlockEntity blockEntity;
    private final Level level;

    public ArborariumMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(pMenuType, pContainerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ArborariumMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, BlockEntity entity) {
        super(pMenuType, pContainerId);
        this.blockEntity = (ArborariumBlockEntity) entity;
        this.level = inventory.player.level();

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Example slot, adjust as needed for Arborarium
            this.addSlot(new SlotItemHandler(handler, 0, 80, 35));
        });
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the clicked slot is one of the machine's slots
        if (pIndex < this.blockEntity.getContainerSize()) {
            // Try to transfer to player inventory
            if (!moveItemStackTo(sourceStack, this.blockEntity.getContainerSize(), slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, 0, this.blockEntity.getContainerSize(), false)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(pPlayer, blockEntity.getLevel(), blockEntity.getBlockPos());
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