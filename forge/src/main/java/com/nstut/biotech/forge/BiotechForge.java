package com.nstut.biotech.forge;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.Config;
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

@Mod(Biotech.MOD_ID)
public class BiotechForge {
    public BiotechForge() {
        // Initialize common mod logic
        Biotech.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register blocks
        BlockRegistries.BLOCKS.register(modEventBus);

        // Register block entities
        BlockEntityRegistries.BLOCK_ENTITIES.register(modEventBus);

        // Register machines
        MachineRegistries.register(modEventBus);

        // Register items
        ItemRegistries.ITEMS.register(modEventBus);

        // Register creative tabs
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);

        // Register menus
        MenuRegistries.MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketRegistries.register();
            // Any other common setup tasks that need to run on the main thread
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Server starting logic
    }

    @Mod.EventBusSubscriber(modid = Biotech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(MenuRegistries.ITEM_INPUT_HATCH.get(), ItemInputHatchScreen::new);
                MenuScreens.register(MenuRegistries.ITEM_OUTPUT_HATCH.get(), ItemOutputHatchScreen::new);
                MenuScreens.register(MenuRegistries.FLUID_INPUT_HATCH.get(), FluidInputHatchScreen::new);
                MenuScreens.register(MenuRegistries.FLUID_OUTPUT_HATCH.get(), FluidOutputHatchScreen::new);
                MenuScreens.register(MenuRegistries.ENERGY_INPUT_HATCH.get(), EnergyInputHatchScreen::new);
                // Register other client-side screens here
            });
        }
    }
}
