package com.nstut.biotech.items;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class ItemRegistries {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Biotech.MOD_ID);

    public static final DeferredItem<BlockItem> NET_TRAP_ITEM = ITEMS.registerSimpleBlockItem(BlockRegistries.NET_TRAP);
    public static final DeferredItem<BlockItem> BIOTECH_MACHINE_CASING = ITEMS.registerSimpleBlockItem(BlockRegistries.BIOTECH_MACHINE_CASING);
    public static final DeferredItem<BlockItem> ITEM_INPUT_HATCH = ITEMS.registerSimpleBlockItem(BlockRegistries.ITEM_INPUT_HATCH);
    public static final DeferredItem<BlockItem> ITEM_OUTPUT_HATCH = ITEMS.registerSimpleBlockItem(BlockRegistries.ITEM_OUTPUT_HATCH);
    public static final DeferredItem<BlockItem> FLUID_INPUT_HATCH = ITEMS.registerSimpleBlockItem(BlockRegistries.FLUID_INPUT_HATCH);
    public static final DeferredItem<BlockItem> FLUID_OUTPUT_HATCH = ITEMS.registerSimpleBlockItem(BlockRegistries.FLUID_OUTPUT_HATCH);
    public static final DeferredItem<BlockItem> ENERGY_INPUT_HATCH = ITEMS.registerSimpleBlockItem(BlockRegistries.ENERGY_INPUT_HATCH);

    public static final DeferredItem<MobItem> COW = ITEMS.registerItem("cow", properties -> new MobItem(properties, 1));
    public static final DeferredItem<MobItem> BABY_COW = ITEMS.registerItem("baby_cow", properties -> new MobItem(properties, 2));
    public static final DeferredItem<MobItem> CHICKEN = ITEMS.registerItem("chicken", properties -> new MobItem(properties, 3));
    public static final DeferredItem<MobItem> BABY_CHICKEN = ITEMS.registerItem("baby_chicken", properties -> new MobItem(properties, 4));
    public static final DeferredItem<MobItem> PIG = ITEMS.registerItem("pig", properties -> new MobItem(properties, 5));
    public static final DeferredItem<MobItem> BABY_PIG = ITEMS.registerItem("baby_pig", properties -> new MobItem(properties, 6));
    public static final DeferredItem<MobItem> SHEEP = ITEMS.registerItem("sheep", properties -> new MobItem(properties, 7));
    public static final DeferredItem<MobItem> BABY_SHEEP = ITEMS.registerItem("baby_sheep", properties -> new MobItem(properties, 8));
    public static final DeferredItem<MobItem> RABBIT = ITEMS.registerItem("rabbit", properties -> new MobItem(properties, 9));
    public static final DeferredItem<MobItem> BABY_RABBIT = ITEMS.registerItem("baby_rabbit", properties -> new MobItem(properties, 10));

    public static final DeferredItem<Item> COW_FEED = ITEMS.registerSimpleItem("cow_feed");
    public static final DeferredItem<Item> CHICKEN_FEED = ITEMS.registerSimpleItem("chicken_feed");
    public static final DeferredItem<Item> PIG_FEED = ITEMS.registerSimpleItem("pig_feed");
    public static final DeferredItem<Item> SHEEP_FEED = ITEMS.registerSimpleItem("sheep_feed");
    public static final DeferredItem<Item> RABBIT_FEED = ITEMS.registerSimpleItem("rabbit_feed");

    public static final DeferredItem<Item> MANURE = ITEMS.registerSimpleItem("manure");
    public static final DeferredItem<Item> PAPER_BAG = ITEMS.registerSimpleItem("paper_bag");
    public static final DeferredItem<Item> FERTILIZER = ITEMS.registerSimpleItem("fertilizer");

    public static final Set<Supplier<? extends Item>> ITEM_SET = new HashSet<>(ITEMS.getEntries());

    private ItemRegistries() {
    }
}
