package com.nstut.biotech.machines;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.entites.machines.BreedingChamberBlockEntity;
import com.nstut.biotech.blocks.entites.machines.FermenterBlockEntity;
import com.nstut.biotech.blocks.entites.machines.GreenhouseBlockEntity;
import com.nstut.biotech.blocks.entites.machines.MixerBlockEntity;
import com.nstut.biotech.blocks.entites.machines.SlaughterhouseBlockEntity;
import com.nstut.biotech.blocks.entites.machines.TerrestrialHabitatBlockEntity;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.recipes.BreedingChamberRecipe;
import com.nstut.biotech.recipes.FermenterRecipe;
import com.nstut.biotech.recipes.GreenhouseRecipe;
import com.nstut.biotech.recipes.MixerRecipe;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.biotech.views.machines.menu.BreedingChamberMenu;
import com.nstut.biotech.views.machines.menu.FermenterMenu;
import com.nstut.biotech.views.machines.menu.GreenhouseMenu;
import com.nstut.biotech.views.machines.menu.MachineMenu;
import com.nstut.biotech.views.machines.menu.MixerMenu;
import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
import com.nstut.biotech.views.machines.menu.TerrestrialHabitatMenu;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.BiFunction;

public final class MachineRegistries {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Biotech.MOD_ID);
    private static final DeferredRegister.Items ITEM_REGISTER = DeferredRegister.createItems(Biotech.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(Registries.MENU, Biotech.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Biotech.MOD_ID);
    private static final DeferredRegister.Blocks BLOCK_REGISTER = DeferredRegister.createBlocks(Biotech.MOD_ID);

    public static final MachineRegistry<BreedingChamberBlockEntity, BreedingChamberMenu, BreedingChamberRecipe> BREEDING_CHAMBER = register("breeding_chamber", BreedingChamberBlockEntity::new, BreedingChamberMenu::new, BreedingChamberRecipe.SERIALIZER);
    public static final MachineRegistry<TerrestrialHabitatBlockEntity, TerrestrialHabitatMenu, TerrestrialHabitatRecipe> TERRESTRIAL_HABITAT = register("terrestrial_habitat", TerrestrialHabitatBlockEntity::new, TerrestrialHabitatMenu::new, TerrestrialHabitatRecipe.SERIALIZER);
    public static final MachineRegistry<SlaughterhouseBlockEntity, SlaughterhouseMenu, SlaughterhouseRecipe> SLAUGHTERHOUSE = register("slaughterhouse", SlaughterhouseBlockEntity::new, SlaughterhouseMenu::new, SlaughterhouseRecipe.SERIALIZER);
    public static final MachineRegistry<GreenhouseBlockEntity, GreenhouseMenu, GreenhouseRecipe> GREENHOUSE = register("greenhouse", GreenhouseBlockEntity::new, GreenhouseMenu::new, GreenhouseRecipe.SERIALIZER);
    public static final MachineRegistry<FermenterBlockEntity, FermenterMenu, FermenterRecipe> FERMENTER = register("fermenter", FermenterBlockEntity::new, FermenterMenu::new, FermenterRecipe.SERIALIZER);
    public static final MachineRegistry<MixerBlockEntity, MixerMenu, MixerRecipe> MIXER = register("mixer", MixerBlockEntity::new, MixerMenu::new, MixerRecipe.SERIALIZER);

    private MachineRegistries() {}

    private static <T extends MachineBlockEntity, U extends MachineMenu, Y extends ModRecipe<Y>> MachineRegistry<T, U, Y> register(
            String id, BiFunction<BlockPos, BlockState, T> blockEntityFactory,
            IContainerFactory<U> containerFactory, RecipeSerializer<Y> recipeSerializer) {
        DeferredBlock<Block> block = BLOCK_REGISTER.<Block>registerBlock(
                id,
                properties -> new MachineBlock(properties, blockEntityFactory),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE).strength(2f).sound(SoundType.METAL));
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity = BLOCK_ENTITY_TYPE_REGISTER.register(
                id, () -> new BlockEntityType<>(blockEntityFactory::apply, Set.of(block.get())));
        DeferredItem<BlockItem> blockItem = ITEM_REGISTER.registerSimpleBlockItem(id, block);
        ItemRegistries.ITEM_SET.add(blockItem);
        DeferredHolder<MenuType<?>, MenuType<U>> menu = MENU_REGISTER.register(id, () -> IMenuTypeExtension.create(containerFactory));
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<Y>> recipeSerializerRegistry = RECIPE_SERIALIZER_REGISTER.register(id, () -> recipeSerializer);
        return new MachineRegistry<>(id, block, blockEntity, blockItem, menu, recipeSerializerRegistry);
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        BLOCK_ENTITY_TYPE_REGISTER.register(modEventBus);
        ITEM_REGISTER.register(modEventBus);
        MENU_REGISTER.register(modEventBus);
        RECIPE_SERIALIZER_REGISTER.register(modEventBus);
    }

    public record MachineRegistry<T extends MachineBlockEntity, U extends MachineMenu, Y extends ModRecipe<Y>>(
            String id, DeferredHolder<Block, Block> block,
            DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity,
            DeferredItem<BlockItem> blockItem, DeferredHolder<MenuType<?>, MenuType<U>> menu,
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<Y>> recipeSerializer) {}
}
