package com.nstut.biotech.views.io_hatches.fluid;

import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class FluidHatchScreen<T extends FluidHatchMenu> extends BiotechContainerScreen<T> {
    public FluidHatchScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title, 176, 166); }
    @Override protected UIComponent buildUI() {
        return HatchUi.fluid(title, new FluidWidget(menu::getFluidStack,
                () -> menu.getFluidHatchBlockEntity().TANK_CAPACITY, () -> true));
    }
}
