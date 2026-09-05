package com.nstut.biotech;

import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.BlockEntityRegistries;
import com.nstut.biotech.creative_tabs.CreativeTabRegistries;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.views.MenuRegistries;
import com.nstut.biotech.views.io_hatches.energy.EnergyInputHatchScreen;
import com.nstut.biotech.views.io_hatches.fluid.FluidInputHatchScreen;
import com.nstut.biotech.views.io_hatches.fluid.FluidOutputHatchScreen;
import com.nstut.biotech.views.io_hatches.item.ItemInputHatchScreen;
import com.nstut.biotech.views.io_hatches.item.ItemOutputHatchScreen;
import com.nstut.biotech.views.machines.screen.BreedingChamberScreen;
import com.nstut.biotech.views.machines.screen.FermenterScreen;
import com.nstut.biotech.views.machines.screen.GreenhouseScreen;
import com.nstut.biotech.views.machines.screen.MixerScreen;
import com.nstut.biotech.views.machines.screen.SlaughterhouseScreen;
import com.nstut.biotech.views.machines.screen.TerrestrialHabitatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(Biotech.MOD_ID)
public class Biotech {
    public static final String MOD_ID = "biotech";
    public static boolean IS_DEV_ENV;

    public Biotech(IEventBus modEventBus, ModContainer modContainer) {
        BlockRegistries.BLOCKS.register(modEventBus);
        BlockEntityRegistries.BLOCK_ENTITIES.register(modEventBus);
        MachineRegistries.register(modEventBus);
        ItemRegistries.ITEMS.register(modEventBus);
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);
        MenuRegistries.MENUS.register(modEventBus);

        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(PacketRegistries::register);
        modEventBus.addListener(BlockEntityRegistries::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(MenuRegistries.ITEM_INPUT_HATCH.get(), ItemInputHatchScreen::new);
            event.register(MenuRegistries.ITEM_OUTPUT_HATCH.get(), ItemOutputHatchScreen::new);
            event.register(MenuRegistries.FLUID_INPUT_HATCH.get(), FluidInputHatchScreen::new);
            event.register(MenuRegistries.FLUID_OUTPUT_HATCH.get(), FluidOutputHatchScreen::new);
            event.register(MenuRegistries.ENERGY_INPUT_HATCH.get(), EnergyInputHatchScreen::new);

            event.register(MachineRegistries.BREEDING_CHAMBER.menu().get(), BreedingChamberScreen::new);
            event.register(MachineRegistries.TERRESTRIAL_HABITAT.menu().get(), TerrestrialHabitatScreen::new);
            event.register(MachineRegistries.SLAUGHTERHOUSE.menu().get(), SlaughterhouseScreen::new);
            event.register(MachineRegistries.GREENHOUSE.menu().get(), GreenhouseScreen::new);
            event.register(MachineRegistries.FERMENTER.menu().get(), FermenterScreen::new);
            event.register(MachineRegistries.MIXER.menu().get(), MixerScreen::new);
        }
    }
}
