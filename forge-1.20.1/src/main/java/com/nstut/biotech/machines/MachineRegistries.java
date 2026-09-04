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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;

public final class MachineRegistries {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Biotech.MOD_ID);
    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Biotech.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Biotech.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Biotech.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Biotech.MOD_ID);
    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Biotech.MOD_ID);

    public static final MachineRegistry<BreedingChamberBlockEntity, BreedingChamberMenu, BreedingChamberRecipe> BREEDING_CHAMBER = register(
            "breeding_chamber", BreedingChamberBlockEntity::new, BreedingChamberMenu::new, BreedingChamberRecipe.SERIALIZER, BreedingChamberRecipe.TYPE);
    public static final MachineRegistry<TerrestrialHabitatBlockEntity, TerrestrialHabitatMenu, TerrestrialHabitatRecipe> TERRESTRIAL_HABITAT = register(
            "terrestrial_habitat", TerrestrialHabitatBlockEntity::new, TerrestrialHabitatMenu::new, TerrestrialHabitatRecipe.SERIALIZER, TerrestrialHabitatRecipe.TYPE);
    public static final MachineRegistry<SlaughterhouseBlockEntity, SlaughterhouseMenu, SlaughterhouseRecipe> SLAUGHTERHOUSE = register(
            "slaughterhouse", SlaughterhouseBlockEntity::new, SlaughterhouseMenu::new, SlaughterhouseRecipe.SERIALIZER, SlaughterhouseRecipe.TYPE);
    public static final MachineRegistry<GreenhouseBlockEntity, GreenhouseMenu, GreenhouseRecipe> GREENHOUSE = register(
            "greenhouse", GreenhouseBlockEntity::new, GreenhouseMenu::new, GreenhouseRecipe.SERIALIZER, GreenhouseRecipe.TYPE);
    public static final MachineRegistry<FermenterBlockEntity, FermenterMenu, FermenterRecipe> FERMENTER = register(
            "fermenter", FermenterBlockEntity::new, FermenterMenu::new, FermenterRecipe.SERIALIZER, FermenterRecipe.TYPE);
    public static final MachineRegistry<MixerBlockEntity, MixerMenu, MixerRecipe> MIXER = register(
            "mixer", MixerBlockEntity::new, MixerMenu::new, MixerRecipe.SERIALIZER, MixerRecipe.TYPE);

    private MachineRegistries() {
    }

    private static <T extends MachineBlockEntity, U extends MachineMenu, Y extends ModRecipe<Y>>
    MachineRegistry<T, U, Y> register(String id,
                                      BiFunction<BlockPos, BlockState, T> blockEntityFactory,
                                      IContainerFactory<U> containerFactory,
                                      RecipeSerializer<Y> recipeSerializer,
                                      RecipeType<Y> recipeType) {
        RegistryObject<Block> block = BLOCK_REGISTER.register(id, () -> new MachineBlock(blockEntityFactory));
        RegistryObject<BlockEntityType<T>> blockEntity = BLOCK_ENTITY_TYPE_REGISTER.register(
                id,
                () -> BlockEntityType.Builder.of(blockEntityFactory::apply, block.get()).build(null));
        RegistryObject<Item> blockItem = ITEM_REGISTER.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        ItemRegistries.ITEM_SET.add(blockItem);
        RegistryObject<MenuType<U>> menu = MENU_REGISTER.register(id, () -> IForgeMenuType.create(containerFactory));
        RegistryObject<RecipeSerializer<Y>> recipeSerializerRegistry = RECIPE_SERIALIZER_REGISTER.register(id, () -> recipeSerializer);
        RegistryObject<RecipeType<Y>> recipeTypeRegistry = RECIPE_TYPE_REGISTER.register(id, () -> recipeType);
        return new MachineRegistry<>(id, block, blockEntity, blockItem, menu, recipeSerializerRegistry, recipeTypeRegistry);
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        BLOCK_ENTITY_TYPE_REGISTER.register(modEventBus);
        ITEM_REGISTER.register(modEventBus);
        MENU_REGISTER.register(modEventBus);
        RECIPE_SERIALIZER_REGISTER.register(modEventBus);
        RECIPE_TYPE_REGISTER.register(modEventBus);
    }

    public record MachineRegistry<T extends MachineBlockEntity, U extends MachineMenu, Y extends ModRecipe<Y>>(
            String id,
            RegistryObject<Block> block,
            RegistryObject<BlockEntityType<T>> blockEntity,
            RegistryObject<Item> blockItem,
            RegistryObject<MenuType<U>> menu,
            RegistryObject<RecipeSerializer<Y>> recipeSerializer,
            RegistryObject<RecipeType<Y>> recipeType) {
    }
}
