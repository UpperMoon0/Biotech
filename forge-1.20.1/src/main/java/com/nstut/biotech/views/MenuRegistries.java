package com.nstut.biotech.views;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.views.io_hatches.energy.EnergyInputHatchMenu;
import com.nstut.biotech.views.io_hatches.fluid.FluidInputHatchMenu;
import com.nstut.biotech.views.io_hatches.fluid.FluidOutputHatchMenu;
import com.nstut.biotech.views.io_hatches.item.ItemInputHatchMenu;
import com.nstut.biotech.views.io_hatches.item.ItemOutputHatchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistries {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Biotech.MOD_ID);

    public static final RegistryObject<MenuType<ItemInputHatchMenu>> ITEM_INPUT_HATCH = MENUS.register("item_input_hatch", () -> IForgeMenuType.create(ItemInputHatchMenu::new));
    public static final RegistryObject<MenuType<ItemOutputHatchMenu>> ITEM_OUTPUT_HATCH = MENUS.register("item_output_hatch", () -> IForgeMenuType.create(ItemOutputHatchMenu::new));
    public static final RegistryObject<MenuType<FluidInputHatchMenu>> FLUID_INPUT_HATCH = MENUS.register("fluid_input_hatch", () -> IForgeMenuType.create(FluidInputHatchMenu::new));
    public static final RegistryObject<MenuType<FluidOutputHatchMenu>> FLUID_OUTPUT_HATCH = MENUS.register("fluid_output_hatch", () -> IForgeMenuType.create(FluidOutputHatchMenu::new));
    public static final RegistryObject<MenuType<EnergyInputHatchMenu>> ENERGY_INPUT_HATCH = MENUS.register("energy_input_hatch", () -> IForgeMenuType.create(EnergyInputHatchMenu::new));
}