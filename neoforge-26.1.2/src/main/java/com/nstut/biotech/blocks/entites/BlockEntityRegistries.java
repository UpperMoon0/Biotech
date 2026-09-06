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

import java.util.Set;

public final class BlockEntityRegistries {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Biotech.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemInputHatchBlockEntity>> ITEM_INPUT_HATCH = BLOCK_ENTITIES.register("item_input_hatch", () -> new BlockEntityType<>(ItemInputHatchBlockEntity::new, Set.of(BlockRegistries.ITEM_INPUT_HATCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemOutputHatchBlockEntity>> ITEM_OUTPUT_HATCH = BLOCK_ENTITIES.register("item_output_hatch", () -> new BlockEntityType<>(ItemOutputHatchBlockEntity::new, Set.of(BlockRegistries.ITEM_OUTPUT_HATCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidInputHatchBlockEntity>> FLUID_INPUT_HATCH = BLOCK_ENTITIES.register("fluid_input_hatch", () -> new BlockEntityType<>(FluidInputHatchBlockEntity::new, Set.of(BlockRegistries.FLUID_INPUT_HATCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidOutputHatchBlockEntity>> FLUID_OUTPUT_HATCH = BLOCK_ENTITIES.register("fluid_output_hatch", () -> new BlockEntityType<>(FluidOutputHatchBlockEntity::new, Set.of(BlockRegistries.FLUID_OUTPUT_HATCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyInputHatchBlockEntity>> ENERGY_INPUT_HATCH = BLOCK_ENTITIES.register("energy_input_hatch", () -> new BlockEntityType<>(EnergyInputHatchBlockEntity::new, Set.of(BlockRegistries.ENERGY_INPUT_HATCH.get())));

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ITEM_INPUT_HATCH.get(), (be, side) -> be.getItemCapability(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ITEM_OUTPUT_HATCH.get(), (be, side) -> be.getItemCapability(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, FLUID_INPUT_HATCH.get(), (be, side) -> be.getItemCapability(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, FLUID_INPUT_HATCH.get(), (be, side) -> be.getFluidCapability(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, FLUID_OUTPUT_HATCH.get(), (be, side) -> be.getItemCapability(side));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, FLUID_OUTPUT_HATCH.get(), (be, side) -> be.getFluidCapability(side));
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ENERGY_INPUT_HATCH.get(), (be, side) -> be.getEnergyCapability(side));
    }

    private BlockEntityRegistries() {}
}
