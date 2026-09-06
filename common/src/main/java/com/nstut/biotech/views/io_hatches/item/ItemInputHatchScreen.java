package com.nstut.biotech.views.io_hatches.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemInputHatchScreen extends ItemHatchScreen<ItemInputHatchMenu> {
    public ItemInputHatchScreen(ItemInputHatchMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }
}
