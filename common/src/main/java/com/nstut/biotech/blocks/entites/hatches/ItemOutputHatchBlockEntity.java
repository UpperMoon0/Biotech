package com.nstut.biotech.blocks.entites.hatches;

import com.nstut.biotech.blocks.entites.BlockEntityRegistries;
import com.nstut.biotech.views.io_hatches.item.ItemOutputHatchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ItemOutputHatchBlockEntity extends ItemHatchBlockEntity {
    public ItemOutputHatchBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistries.ITEM_OUTPUT_HATCH.get(), pos, state);
    }

    @Override
    protected boolean isInputHatch() {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.title.biotech.item_output_hatch");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ItemOutputHatchMenu(containerId, inventory, this);
    }
}
