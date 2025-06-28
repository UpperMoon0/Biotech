package com.nstut.biotech.blocks.entites;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry; 
import net.minecraft.resources.ResourceKey;

@SuppressWarnings("ConstantConditions")
public class BlockEntityRegistries {
    // Corrected DeferredRegister creation with explicit cast for ResourceKey
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Biotech.MOD_ID, (ResourceKey<Registry<BlockEntityType<?>>>) BuiltInRegistries.BLOCK_ENTITY_TYPE.key());

    // Changed RegistryObject to RegistrySupplier
    public static final RegistrySupplier<BlockEntityType<ItemInputHatchBlockEntity>> ITEM_INPUT_HATCH = BLOCK_ENTITIES.register("item_input_hatch", () -> BlockEntityType.Builder.of(ItemInputHatchBlockEntity::new, BlockRegistries.ITEM_INPUT_HATCH.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<ItemOutputHatchBlockEntity>> ITEM_OUTPUT_HATCH = BLOCK_ENTITIES.register("item_output_hatch", () -> BlockEntityType.Builder.of(ItemOutputHatchBlockEntity::new, BlockRegistries.ITEM_OUTPUT_HATCH.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<FluidInputHatchBlockEntity>> FLUID_INPUT_HATCH = BLOCK_ENTITIES.register("fluid_input_hatch", () -> BlockEntityType.Builder.of(FluidInputHatchBlockEntity::new, BlockRegistries.FLUID_INPUT_HATCH.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<FluidOutputHatchBlockEntity>> FLUID_OUTPUT_HATCH = BLOCK_ENTITIES.register("fluid_output_hatch", () -> BlockEntityType.Builder.of(FluidOutputHatchBlockEntity::new, BlockRegistries.FLUID_OUTPUT_HATCH.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<EnergyInputHatchBlockEntity>> ENERGY_INPUT_HATCH = BLOCK_ENTITIES.register("energy_input_hatch", () -> BlockEntityType.Builder.of(EnergyInputHatchBlockEntity::new, BlockRegistries.ENERGY_INPUT_HATCH.get()).build(null));

    // Call this method from your main mod class (e.g., Biotech.init() in common, or BiotechForge constructor)
    public static void register() {
        BLOCK_ENTITIES.register();
    }
}