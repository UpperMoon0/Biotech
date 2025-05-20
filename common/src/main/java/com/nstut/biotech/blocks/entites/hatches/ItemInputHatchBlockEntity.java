package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.entites.BlockEntityRegistries;
import com.nstut.biotech.views.io_hatches.item.ItemInputHatchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemInputHatchBlockEntity extends ItemHatchBlockEntity {
    public ItemInputHatchBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistries.ITEM_INPUT_HATCH.get(), pPos, pBlockState);
    }
    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.title.biotech.item_input_hatch");
    }
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ItemInputHatchMenu(pContainerId, pPlayerInventory, this);
    }
}
