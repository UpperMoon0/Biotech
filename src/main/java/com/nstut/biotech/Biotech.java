package com.nstut.biotech;

import com.mojang.logging.LogUtils;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.block_entites.machines.MachineRegistries;
import com.nstut.biotech.blocks.block_entites.BlockEntityRegistries;
import com.nstut.biotech.creative_tabs.CreativeTabRegistries;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.network.PacketRegistries;
import com.nstut.biotech.views.MenuRegistries;
import com.nstut.biotech.views.io_hatches.energy.EnergyInputHatchScreen;
import com.nstut.biotech.views.io_hatches.fluid.FluidInputHatchScreen;
import com.nstut.biotech.views.io_hatches.fluid.FluidOutputHatchScreen;
import com.nstut.biotech.views.io_hatches.item.ItemInputHatchScreen;
import com.nstut.biotech.views.io_hatches.item.ItemOutputHatchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Biotech.MOD_ID)
public class Biotech
{
    public static boolean IS_DEV_ENV;

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "biotech";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Biotech(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register blocks
        BlockRegistries.BLOCKS.register(modEventBus);

        // Register block entities
        BlockEntityRegistries.BLOCK_ENTITIES.register(modEventBus);

        // Register machines
        MachineRegistries.register(modEventBus);

        // Register the Deferred Register to the mod event bus so items get registered
        ItemRegistries.ITEMS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so menus get registered
        MenuRegistries.MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        PacketRegistries.register();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MenuScreens.register(MenuRegistries.ITEM_INPUT_HATCH.get(), ItemInputHatchScreen::new);
            MenuScreens.register(MenuRegistries.ITEM_OUTPUT_HATCH.get(), ItemOutputHatchScreen::new);
            MenuScreens.register(MenuRegistries.FLUID_INPUT_HATCH.get(), FluidInputHatchScreen::new);
            MenuScreens.register(MenuRegistries.FLUID_OUTPUT_HATCH.get(), FluidOutputHatchScreen::new);
            MenuScreens.register(MenuRegistries.ENERGY_INPUT_HATCH.get(), EnergyInputHatchScreen::new);
        }
    }
}
