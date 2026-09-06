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

    public static final DeferredBlock<Block> NET_TRAP = BLOCKS.register("net_trap", NetTrapBlock::new);
    public static final DeferredBlock<Block> BIOTECH_MACHINE_CASING = BLOCKS.register("biotech_machine_casing", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE).strength(2f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> ITEM_INPUT_HATCH = BLOCKS.register("item_input_hatch", () -> new IOHatchBlock(0));
    public static final DeferredBlock<Block> ITEM_OUTPUT_HATCH = BLOCKS.register("item_output_hatch", () -> new IOHatchBlock(1));
    public static final DeferredBlock<Block> FLUID_INPUT_HATCH = BLOCKS.register("fluid_input_hatch", () -> new IOHatchBlock(2));
    public static final DeferredBlock<Block> FLUID_OUTPUT_HATCH = BLOCKS.register("fluid_output_hatch", () -> new IOHatchBlock(3));
    public static final DeferredBlock<Block> ENERGY_INPUT_HATCH = BLOCKS.register("energy_input_hatch", () -> new IOHatchBlock(4));

    private BlockRegistries() {
    }
}
