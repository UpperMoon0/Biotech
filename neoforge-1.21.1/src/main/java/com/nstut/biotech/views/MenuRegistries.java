package com.nstut.biotech.views;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.views.io_hatches.energy.EnergyInputHatchMenu;
import com.nstut.biotech.views.io_hatches.fluid.FluidInputHatchMenu;
import com.nstut.biotech.views.io_hatches.fluid.FluidOutputHatchMenu;
import com.nstut.biotech.views.io_hatches.item.ItemInputHatchMenu;
import com.nstut.biotech.views.io_hatches.item.ItemOutputHatchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MenuRegistries {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Biotech.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ItemInputHatchMenu>> ITEM_INPUT_HATCH = MENUS.register("item_input_hatch", () -> IMenuTypeExtension.create(ItemInputHatchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemOutputHatchMenu>> ITEM_OUTPUT_HATCH = MENUS.register("item_output_hatch", () -> IMenuTypeExtension.create(ItemOutputHatchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidInputHatchMenu>> FLUID_INPUT_HATCH = MENUS.register("fluid_input_hatch", () -> IMenuTypeExtension.create(FluidInputHatchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidOutputHatchMenu>> FLUID_OUTPUT_HATCH = MENUS.register("fluid_output_hatch", () -> IMenuTypeExtension.create(FluidOutputHatchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<EnergyInputHatchMenu>> ENERGY_INPUT_HATCH = MENUS.register("energy_input_hatch", () -> IMenuTypeExtension.create(EnergyInputHatchMenu::new));

    private MenuRegistries() {
    }
}
