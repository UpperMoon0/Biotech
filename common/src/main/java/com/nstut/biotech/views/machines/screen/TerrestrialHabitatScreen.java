package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.views.machines.menu.TerrestrialHabitatMenu;
import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TerrestrialHabitatScreen extends BiotechContainerScreen<TerrestrialHabitatMenu> {
    public TerrestrialHabitatScreen(TerrestrialHabitatMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, BiotechStyle.MACHINE_WIDTH, BiotechStyle.MACHINE_HEIGHT); }
    @Override protected UIComponent buildUI() {
        MachineDisplay display = new MachineDisplay(menu::getStructureValid, menu::getIsOperating,
                menu::getEnergyStored, menu::getEnergyCapacity, menu::getEnergyConsumed,
                menu::getRecipeEnergyCost, menu::getEnergyConsumeRate, menu::getRecipe);
        return MachineUi.build(MachineUi.Kind.TERRESTRIALHABITAT, title, display,
                new FluidWidget(menu::getFluidStored, menu::getFluidCapacity, menu::getStructureValid),
                new FluidWidget(() -> display.active() && menu.getRecipe().getFluidIngredients().length > 0
                    ? menu.getRecipe().getFluidIngredients()[0] : FluidWidget.empty(),
                    () -> display.active() && menu.getRecipe().getFluidIngredients().length > 0
                        ? menu.getRecipe().getFluidIngredients()[0].getAmount() : 0, menu::getStructureValid));
    }
}
