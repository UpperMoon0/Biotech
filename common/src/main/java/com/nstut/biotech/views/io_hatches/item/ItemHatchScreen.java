package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class ItemHatchScreen<T extends ItemHatchMenu> extends BiotechContainerScreen<T> {
    public ItemHatchScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title, 176, 166); }
    @Override protected UIComponent buildUI() {
        return HatchUi.items(title);
    }
}
