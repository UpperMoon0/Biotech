package com.nstut.biotech.views.io_hatches.energy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnergyInputHatchScreen extends EnergyHatchScreen<EnergyInputHatchMenu> {
    public EnergyInputHatchScreen(EnergyInputHatchMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }
}
