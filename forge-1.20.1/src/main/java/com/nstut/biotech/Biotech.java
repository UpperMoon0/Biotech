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
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Biotech.MOD_ID)
public class Biotech {
    public static final String MOD_ID = "biotech";
    public static boolean IS_DEV_ENV;

    public Biotech(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        BlockRegistries.BLOCKS.register(modEventBus);
        BlockEntityRegistries.BLOCK_ENTITIES.register(modEventBus);
        MachineRegistries.register(modEventBus);
        ItemRegistries.ITEMS.register(modEventBus);
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);
        MenuRegistries.MENUS.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PacketRegistries::register);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(MenuRegistries.ITEM_INPUT_HATCH.get(), ItemInputHatchScreen::new);
                MenuScreens.register(MenuRegistries.ITEM_OUTPUT_HATCH.get(), ItemOutputHatchScreen::new);
                MenuScreens.register(MenuRegistries.FLUID_INPUT_HATCH.get(), FluidInputHatchScreen::new);
                MenuScreens.register(MenuRegistries.FLUID_OUTPUT_HATCH.get(), FluidOutputHatchScreen::new);
                MenuScreens.register(MenuRegistries.ENERGY_INPUT_HATCH.get(), EnergyInputHatchScreen::new);

                MenuScreens.register(MachineRegistries.BREEDING_CHAMBER.menu().get(), BreedingChamberScreen::new);
                MenuScreens.register(MachineRegistries.TERRESTRIAL_HABITAT.menu().get(), TerrestrialHabitatScreen::new);
                MenuScreens.register(MachineRegistries.SLAUGHTERHOUSE.menu().get(), SlaughterhouseScreen::new);
                MenuScreens.register(MachineRegistries.GREENHOUSE.menu().get(), GreenhouseScreen::new);
                MenuScreens.register(MachineRegistries.FERMENTER.menu().get(), FermenterScreen::new);
                MenuScreens.register(MachineRegistries.MIXER.menu().get(), MixerScreen::new);
            });
        }
    }
}
