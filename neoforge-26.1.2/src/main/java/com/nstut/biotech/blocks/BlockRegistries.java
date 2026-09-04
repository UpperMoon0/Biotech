package com.nstut.biotech.blocks;

import com.nstut.biotech.Biotech;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BlockRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Biotech.MOD_ID);

    public static final DeferredBlock<Block> NET_TRAP = BLOCKS.<Block>registerBlock(
            "net_trap", NetTrapBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final DeferredBlock<Block> BIOTECH_MACHINE_CASING = BLOCKS.registerBlock(
            "biotech_machine_casing", Block::new, BlockRegistries::machineProperties);
    public static final DeferredBlock<Block> ITEM_INPUT_HATCH = BLOCKS.<Block>registerBlock(
            "item_input_hatch", properties -> new IOHatchBlock(properties, 0), BlockRegistries::machineProperties);
    public static final DeferredBlock<Block> ITEM_OUTPUT_HATCH = BLOCKS.<Block>registerBlock(
            "item_output_hatch", properties -> new IOHatchBlock(properties, 1), BlockRegistries::machineProperties);
    public static final DeferredBlock<Block> FLUID_INPUT_HATCH = BLOCKS.<Block>registerBlock(
            "fluid_input_hatch", properties -> new IOHatchBlock(properties, 2), BlockRegistries::machineProperties);
    public static final DeferredBlock<Block> FLUID_OUTPUT_HATCH = BLOCKS.<Block>registerBlock(
            "fluid_output_hatch", properties -> new IOHatchBlock(properties, 3), BlockRegistries::machineProperties);
    public static final DeferredBlock<Block> ENERGY_INPUT_HATCH = BLOCKS.<Block>registerBlock(
            "energy_input_hatch", properties -> new IOHatchBlock(properties, 4), BlockRegistries::machineProperties);

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE).strength(2f).sound(SoundType.METAL);
    }

    private BlockRegistries() {
    }
}
