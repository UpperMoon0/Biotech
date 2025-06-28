package com.nstut.biotech.blocks;

import com.nstut.biotech.Biotech;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries; 
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;

public class BlockRegistries {
    // Changed to use Architectury DeferredRegister and Registries
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Biotech.MOD_ID, BuiltInRegistries.BLOCK.key());

    // Changed RegistryObject to RegistrySupplier
    public static final RegistrySupplier<Block> NET_TRAP = BLOCKS.register("net_trap", NetTrapBlock::new);
    public static final RegistrySupplier<Block> BIOTECH_MACHINE_CASING = BLOCKS.register("biotech_machine_casing", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE).strength(2f).sound(SoundType.METAL)));

    // Call this method from your main mod class (e.g., Biotech.init() in common, or BiotechForge constructor)
    public static void register() {
        BLOCKS.register();
    }
}