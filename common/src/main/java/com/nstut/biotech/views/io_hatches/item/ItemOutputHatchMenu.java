package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.views.MenuRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class ItemOutputHatchMenu extends ItemHatchMenu {
    public ItemOutputHatchMenu(int pContainerId, Inventory inventory, FriendlyByteBuf friendlyByteBuf) {
        this(pContainerId, inventory, Objects.requireNonNull(inventory.player.level().getBlockEntity(friendlyByteBuf.readBlockPos())));
    }
    public ItemOutputHatchMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity) {
        super(MenuRegistries.ITEM_INPUT_HATCH.get(), pContainerId, inventory, blockEntity);
    }
}
