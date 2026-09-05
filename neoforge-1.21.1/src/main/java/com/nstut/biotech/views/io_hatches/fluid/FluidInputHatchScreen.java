package com.nstut.biotech.views.io_hatches.fluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FluidInputHatchScreen extends FluidHatchScreen<FluidInputHatchMenu> {
    public FluidInputHatchScreen(FluidInputHatchMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }
}
