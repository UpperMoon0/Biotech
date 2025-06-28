package com.nstut.biotech.items;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries; // Keep this for ITEM key
import net.minecraft.core.Registry; // Import for Registry

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemRegistries {

    // Corrected DeferredRegister creation
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Biotech.MOD_ID, BuiltInRegistries.ITEM.key());

    // Changed RegistryObject to RegistrySupplier
    public static final RegistrySupplier<Item> NET_TRAP_ITEM = ITEMS.register("net_trap", () -> new BlockItem(BlockRegistries.NET_TRAP.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> BIOTECH_MACHINE_CASING = ITEMS.register("biotech_machine_casing", () -> new BlockItem(BlockRegistries.BIOTECH_MACHINE_CASING.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> ITEM_INPUT_HATCH = ITEMS.register("item_input_hatch", () -> new BlockItem(BlockRegistries.ITEM_INPUT_HATCH.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> ITEM_OUTPUT_HATCH = ITEMS.register("item_output_hatch", () -> new BlockItem(BlockRegistries.ITEM_OUTPUT_HATCH.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> FLUID_INPUT_HATCH = ITEMS.register("fluid_input_hatch", () -> new BlockItem(BlockRegistries.FLUID_INPUT_HATCH.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> FLUID_OUTPUT_HATCH = ITEMS.register("fluid_output_hatch", () -> new BlockItem(BlockRegistries.FLUID_OUTPUT_HATCH.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> ENERGY_INPUT_HATCH = ITEMS.register("energy_input_hatch", () -> new BlockItem(BlockRegistries.ENERGY_INPUT_HATCH.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> COW = ITEMS.register("cow", () -> new MobItem(1));
    public static final RegistrySupplier<Item> BABY_COW = ITEMS.register("baby_cow", () -> new MobItem(2));
    public static final RegistrySupplier<Item> CHICKEN = ITEMS.register("chicken", () -> new MobItem(3));
    public static final RegistrySupplier<Item> BABY_CHICKEN = ITEMS.register("baby_chicken", () -> new MobItem(4));
    public static final RegistrySupplier<Item> PIG = ITEMS.register("pig", () -> new MobItem(5));
    public static final RegistrySupplier<Item> BABY_PIG = ITEMS.register("baby_pig", () -> new MobItem(6));
    public static final RegistrySupplier<Item> SHEEP = ITEMS.register("sheep", () -> new MobItem(7));
    public static final RegistrySupplier<Item> BABY_SHEEP = ITEMS.register("baby_sheep", () -> new MobItem(8));
    public static final RegistrySupplier<Item> RABBIT = ITEMS.register("rabbit", () -> new MobItem(9));
    public static final RegistrySupplier<Item> BABY_RABBIT = ITEMS.register("baby_rabbit", () -> new MobItem(10));

    public static final RegistrySupplier<Item> COW_FEED = ITEMS.register("cow_feed", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> CHICKEN_FEED = ITEMS.register("chicken_feed", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> PIG_FEED = ITEMS.register("pig_feed", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> SHEEP_FEED = ITEMS.register("sheep_feed", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> RABBIT_FEED = ITEMS.register("rabbit_feed", () -> new Item(new Item.Properties()));

    public static final RegistrySupplier<Item> MANURE = ITEMS.register("manure", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> PAPER_BAG = ITEMS.register("paper_bag", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> FERTILIZER = ITEMS.register("fertilizer", () -> new Item(new Item.Properties()));

    public static Set<Item> ITEM_SET;

    public static void register() {
        ITEMS.register();
        // Corrected ITEM_SET population
        // We need to access the registered items from the actual registry after they are registered.
        // This approach might vary slightly based on Architectury version and how DeferredRegister populates.
        // For now, let's assume direct iteration over suppliers if getEntries() isn't available.
        // A more robust way would be to query the registry itself if needed immediately.
        ITEM_SET = new HashSet<>();
        ITEMS.getSuppliers().forEach(supplier -> ITEM_SET.add(supplier.get()));
    }
}
