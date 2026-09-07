package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.Ui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import static com.nstut.biotech.views.openui.BiotechWidgets.*;

public abstract class ItemHatchScreen<T extends ItemHatchMenu> extends BiotechContainerScreen<T> {
    public ItemHatchScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title, 176, 166); }
    @Override protected UIComponent buildUI() {
        return Ui.stack(at(Ui.text(title), 8, 5, 160, 12),
                at(Ui.text(Component.translatable("container.inventory")), 8, 72, 160, 12));
    }
}
