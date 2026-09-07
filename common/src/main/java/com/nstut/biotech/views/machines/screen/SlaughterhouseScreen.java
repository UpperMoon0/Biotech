package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SlaughterhouseScreen extends BiotechContainerScreen<SlaughterhouseMenu> {
    public SlaughterhouseScreen(SlaughterhouseMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 240, 184); }
    @Override protected UIComponent buildUI() {
        MachineDisplay display = new MachineDisplay(menu::getStructureValid, menu::getIsOperating,
                menu::getEnergyStored, menu::getEnergyCapacity, menu::getEnergyConsumed,
                menu::getRecipeEnergyCost, menu::getEnergyConsumeRate, menu::getRecipe);
        return MachineUi.build(MachineUi.Kind.SLAUGHTERHOUSE, title, display,
                new FluidWidget(menu::getFluidStored, menu::getFluidCapacity, menu::getStructureValid),
                new FluidWidget(() -> display.active() && menu.getRecipe().getFluidIngredients().length > 0
                    ? menu.getRecipe().getFluidIngredients()[0] : FluidWidget.empty(),
                    () -> display.active() && menu.getRecipe().getFluidIngredients().length > 0
                        ? menu.getRecipe().getFluidIngredients()[0].getAmount() : 0, menu::getStructureValid));
    }
}
