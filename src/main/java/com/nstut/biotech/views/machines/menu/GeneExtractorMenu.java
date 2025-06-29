package com.nstut.biotech.views.machines.menu;

import com.nstut.biotech.blocks.entites.machines.GeneExtractorBlockEntity;
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

public class GeneExtractorMenu extends AbstractContainerMenu {
    public final GeneExtractorBlockEntity blockEntity;
    private final Level level;

    public GeneExtractorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public GeneExtractorMenu(int id, Inventory inv, BlockEntity entity) {
        super(MenuType.GENERIC_3x3, id); // Placeholder MenuType
        blockEntity = ((GeneExtractorBlockEntity) entity);
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Example slot, adjust as needed for Gene Extractor
            this.addSlot(new SlotItemHandler(handler, 0, 80, 35));
        });
    }

    @Override
    public ItemStack quickMoveStack(Player p_38450_, int p_38451_) {
        return ItemStack.EMPTY; // Implement quick move logic later
    }

    @Override
    public boolean stillValid(Player p_38447_) {
        return stillValid(p_38447_, blockEntity.getLevel(), blockEntity.getBlockPos());
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