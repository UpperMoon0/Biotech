package com.nstut.biotech.blocks.entites;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.BlockRegistries;
import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.FluidOutputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemInputHatchBlockEntity;
import com.nstut.biotech.blocks.entites.hatches.ItemOutputHatchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockEntityRegistries {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Biotech.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemInputHatchBlockEntity>> ITEM_INPUT_HATCH = BLOCK_ENTITIES.register("item_input_hatch", () -> BlockEntityType.Builder.of(ItemInputHatchBlockEntity::new, BlockRegistries.ITEM_INPUT_HATCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemOutputHatchBlockEntity>> ITEM_OUTPUT_HATCH = BLOCK_ENTITIES.register("item_output_hatch", () -> BlockEntityType.Builder.of(ItemOutputHatchBlockEntity::new, BlockRegistries.ITEM_OUTPUT_HATCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidInputHatchBlockEntity>> FLUID_INPUT_HATCH = BLOCK_ENTITIES.register("fluid_input_hatch", () -> BlockEntityType.Builder.of(FluidInputHatchBlockEntity::new, BlockRegistries.FLUID_INPUT_HATCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidOutputHatchBlockEntity>> FLUID_OUTPUT_HATCH = BLOCK_ENTITIES.register("fluid_output_hatch", () -> BlockEntityType.Builder.of(FluidOutputHatchBlockEntity::new, BlockRegistries.FLUID_OUTPUT_HATCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyInputHatchBlockEntity>> ENERGY_INPUT_HATCH = BLOCK_ENTITIES.register("energy_input_hatch", () -> BlockEntityType.Builder.of(EnergyInputHatchBlockEntity::new, BlockRegistries.ENERGY_INPUT_HATCH.get()).build(null));

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ITEM_INPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getItemCapability(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ITEM_OUTPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getItemCapability(side));

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FLUID_INPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getItemCapability(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FLUID_INPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getFluidCapability(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FLUID_OUTPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getItemCapability(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FLUID_OUTPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getFluidCapability(side));

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ENERGY_INPUT_HATCH.get(),
                (blockEntity, side) -> blockEntity.getEnergyCapability(side));
    }

    private BlockEntityRegistries() {
    }
}
