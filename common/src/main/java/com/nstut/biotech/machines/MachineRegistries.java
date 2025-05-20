package com.nstut.biotech.machines;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.entites.machines.*;
import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.recipes.*;
import com.nstut.biotech.views.machines.menu.*;
import com.nstut.biotech.views.machines.screen.*;
import com.nstut.nstutlib.blocks.MachineBlock;
import com.nstut.nstutlib.blocks.MachineBlockEntity;
import com.nstut.nstutlib.recipes.ModRecipe;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class MachineRegistries {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Biotech.MOD_ID);
    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Biotech.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Biotech.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Biotech.MOD_ID);
    private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Biotech.MOD_ID);

    private static final List<Runnable> SCREEN_REGISTRIES = new ArrayList<>();

    public static MachineRegistry<BreedingChamberBlockEntity, BreedingChamberMenu, BreedingChamberRecipe> BREEDING_CHAMBER = register(
            "breeding_chamber",
            BreedingChamberBlockEntity.class,
            BreedingChamberMenu::new,
            BreedingChamberScreen::new,
            BreedingChamberRecipe.SERIALIZER
    );

    public static MachineRegistry<TerrestrialHabitatBlockEntity, TerrestrialHabitatMenu, TerrestrialHabitatRecipe> TERRESTRIAL_HABITAT = register(
            "terrestrial_habitat",
            TerrestrialHabitatBlockEntity.class,
            TerrestrialHabitatMenu::new,
            TerrestrialHabitatScreen::new,
            TerrestrialHabitatRecipe.SERIALIZER
    );

    public static MachineRegistry<SlaughterhouseBlockEntity, SlaughterhouseMenu, SlaughterhouseRecipe> SLAUGHTERHOUSE = register(
            "slaughterhouse",
            SlaughterhouseBlockEntity.class,
            SlaughterhouseMenu::new,
            SlaughterhouseScreen::new,
            SlaughterhouseRecipe.SERIALIZER
    );

    public static MachineRegistry<GreenhouseBlockEntity, GreenhouseMenu, GreenhouseRecipe> GREENHOUSE = register(
            "greenhouse",
            GreenhouseBlockEntity.class,
            GreenhouseMenu::new,
            GreenhouseScreen::new,
            GreenhouseRecipe.SERIALIZER
    );

    public static MachineRegistry<FermenterBlockEntity, FermenterMenu, FermenterRecipe> FERMENTER = register(
            "fermenter",
            FermenterBlockEntity.class,
            FermenterMenu::new,
            FermenterScreen::new,
            FermenterRecipe.SERIALIZER
    );

    public static MachineRegistry<MixerBlockEntity, MixerMenu, MixerRecipe> MIXER = register(
            "mixer",
            MixerBlockEntity.class,
            MixerMenu::new,
            MixerScreen::new,
            MixerRecipe.SERIALIZER
    );

    public static <T extends MachineBlockEntity,
            U extends MachineMenu,
            V extends AbstractContainerScreen<U>,
            Y extends ModRecipe<Y>>
    MachineRegistry<T, U, Y> register(
            String id,
            Class<T> blockEntityClass,
            IContainerFactory<U> containerFactory,
            MenuScreens.ScreenConstructor<U, V> screenConstructor,
            RecipeSerializer<Y> recipeSerializer
    )
    {
        BlockEntityType.BlockEntitySupplier<T> blockEntitySupplier = (pos, state) -> {
            try {
                return blockEntityClass.getConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create block entity supplier for " + blockEntityClass.getName(), e);
            }
        };

        RegistryObject<Block> block = BLOCK_REGISTER.register(id, () -> new MachineBlock(blockEntityClass));
        RegistryObject<BlockEntityType<T>> blockEntity = BLOCK_ENTITY_TYPE_REGISTER.register(id, () -> BlockEntityType.Builder.of(blockEntitySupplier, block.get()).build(null));
        RegistryObject<Item> blockItem = ITEM_REGISTER.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        ItemRegistries.ITEM_SET.add(blockItem);
        RegistryObject<MenuType<U>> menu = MENU_REGISTER.register(id, () -> IForgeMenuType.create(containerFactory));
        RegistryObject<RecipeSerializer<Y>> recipeSerializerRegistry =
                RECIPE_SERIALIZER_REGISTER.register(id, () -> recipeSerializer);

        SCREEN_REGISTRIES.add(() -> MenuScreens.register(menu.get(), screenConstructor));

        return new MachineRegistry<>(id, block, blockEntity, blockItem, menu, recipeSerializerRegistry);
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_REGISTER.register(modEventBus);
        BLOCK_ENTITY_TYPE_REGISTER.register(modEventBus);
        ITEM_REGISTER.register(modEventBus);
        MENU_REGISTER.register(modEventBus);
        RECIPE_SERIALIZER_REGISTER.register(modEventBus);

        modEventBus.addListener(MachineRegistries::onClientSetup);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        for (Runnable registration : SCREEN_REGISTRIES) {
            registration.run();
        }
    }

    public record MachineRegistry<T extends MachineBlockEntity, U extends MachineMenu, Y extends ModRecipe<Y>>(
            String id,
            RegistryObject<Block> block,
            RegistryObject<BlockEntityType<T>> blockEntity,
            RegistryObject<Item> blockItem,
            RegistryObject<MenuType<U>> menu,
            RegistryObject<RecipeSerializer<Y>> recipeSerializer
    ) {
    }
}