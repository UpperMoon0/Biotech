package com.nstut.biotech.items;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Set;

public class ItemRegistries {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Biotech.MOD_ID);

    public static final RegistryObject<Item> NET_TRAP_ITEM = ITEMS.register("net_trap", () -> new BlockItem(BlockRegistries.NET_TRAP.get(), new Item.Properties()));
    public static final RegistryObject<Item> BIOTECH_MACHINE_CASING = ITEMS.register("biotech_machine_casing", () -> new BlockItem(BlockRegistries.BIOTECH_MACHINE_CASING.get(), new Item.Properties()));
    public static final RegistryObject<Item> ITEM_INPUT_HATCH = ITEMS.register("item_input_hatch", () -> new BlockItem(BlockRegistries.ITEM_INPUT_HATCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> ITEM_OUTPUT_HATCH = ITEMS.register("item_output_hatch", () -> new BlockItem(BlockRegistries.ITEM_OUTPUT_HATCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLUID_INPUT_HATCH = ITEMS.register("fluid_input_hatch", () -> new BlockItem(BlockRegistries.FLUID_INPUT_HATCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> FLUID_OUTPUT_HATCH = ITEMS.register("fluid_output_hatch", () -> new BlockItem(BlockRegistries.FLUID_OUTPUT_HATCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> ENERGY_INPUT_HATCH = ITEMS.register("energy_input_hatch", () -> new BlockItem(BlockRegistries.ENERGY_INPUT_HATCH.get(), new Item.Properties()));

    public static final RegistryObject<Item> COW = ITEMS.register("cow", () -> new MobItem(1));
    public static final RegistryObject<Item> BABY_COW = ITEMS.register("baby_cow", () -> new MobItem(2));
    public static final RegistryObject<Item> CHICKEN = ITEMS.register("chicken", () -> new MobItem(3));
    public static final RegistryObject<Item> BABY_CHICKEN = ITEMS.register("baby_chicken", () -> new MobItem(4));
    public static final RegistryObject<Item> PIG = ITEMS.register("pig", () -> new MobItem(5));
    public static final RegistryObject<Item> BABY_PIG = ITEMS.register("baby_pig", () -> new MobItem(6));
    public static final RegistryObject<Item> SHEEP = ITEMS.register("sheep", () -> new MobItem(7));
    public static final RegistryObject<Item> BABY_SHEEP = ITEMS.register("baby_sheep", () -> new MobItem(8));
    public static final RegistryObject<Item> RABBIT = ITEMS.register("rabbit", () -> new MobItem(9));
    public static final RegistryObject<Item> BABY_RABBIT = ITEMS.register("baby_rabbit", () -> new MobItem(10));

    public static final RegistryObject<Item> COW_FEED = ITEMS.register("cow_feed", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHICKEN_FEED = ITEMS.register("chicken_feed", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PIG_FEED = ITEMS.register("pig_feed", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHEEP_FEED = ITEMS.register("sheep_feed", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RABBIT_FEED = ITEMS.register("rabbit_feed", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MANURE = ITEMS.register("manure", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PAPER_BAG = ITEMS.register("paper_bag", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FERTILIZER = ITEMS.register("fertilizer", () -> new Item(new Item.Properties()));

    public static final Set<RegistryObject<Item>> ITEM_SET;

    static {
        ITEM_SET = new HashSet<>(ITEMS.getEntries());
    }
}
