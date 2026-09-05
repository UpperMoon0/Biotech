package com.nstut.biotech.views.io_hatches.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemOutputHatchScreen extends ItemHatchScreen<ItemOutputHatchMenu> {
    public ItemOutputHatchScreen(ItemOutputHatchMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }
}
