package com.nstut.biotech.views.io_hatches.energy;

import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnergyHatchScreen<T extends EnergyHatchMenu> extends BiotechContainerScreen<T> {
    public EnergyHatchScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title, 176, 166); }
    @Override protected UIComponent buildUI() {
        return HatchUi.energy(title, menu::getEnergy, () -> menu.getBlockEntity().ENERGY_CAPACITY);
    }
}
