package com.nstut.biotech.blocks;

import com.nstut.biotech.Biotech;
import com.nstut.nstutlib.items.SmartHammer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistries {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Biotech.MOD_ID);

    public static final RegistryObject<Block> NET_TRAP = BLOCKS.register("net_trap", NetTrapBlock::new);
    public static final RegistryObject<Block> BIOTECH_MACHINE_CASING = BLOCKS.register("biotech_machine_casing", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE).strength(2f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> ITEM_INPUT_HATCH = BLOCKS.register("item_input_hatch", () -> new IOHatchBlock(0));
    public static final RegistryObject<Block> ITEM_OUTPUT_HATCH = BLOCKS.register("item_output_hatch", () -> new IOHatchBlock(1));
    public static final RegistryObject<Block> FLUID_INPUT_HATCH = BLOCKS.register("fluid_input_hatch", () -> new IOHatchBlock(2));
    public static final RegistryObject<Block> FLUID_OUTPUT_HATCH = BLOCKS.register("fluid_output_hatch", () -> new IOHatchBlock(3));
    public static final RegistryObject<Block> ENERGY_INPUT_HATCH = BLOCKS.register("energy_input_hatch", () -> new IOHatchBlock(4));
}