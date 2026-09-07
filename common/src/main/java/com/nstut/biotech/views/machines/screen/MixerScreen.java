package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.views.machines.menu.MixerMenu;
import com.nstut.biotech.views.openui.*;
import com.nstut.openui.api.UIComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MixerScreen extends BiotechContainerScreen<MixerMenu> {
    public MixerScreen(MixerMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, BiotechStyle.MACHINE_WIDTH, BiotechStyle.MACHINE_HEIGHT); }
    @Override protected UIComponent buildUI() {
        MachineDisplay display = new MachineDisplay(menu::getStructureValid, menu::getIsOperating,
                menu::getEnergyStored, menu::getEnergyCapacity, menu::getEnergyConsumed,
                menu::getRecipeEnergyCost, menu::getEnergyConsumeRate, menu::getRecipe);
        return MachineUi.build(MachineUi.Kind.MIXER, title, display);
    }
}
