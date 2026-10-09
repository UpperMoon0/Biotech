package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.RecipeTransactionException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Empty outputs request live mature-block harvests; authored outputs remain authoritative. */
public final class GreenhouseHarvestPreparation {
    private GreenhouseHarvestPreparation() {}

    public record Harvest(BlockState state, int count) {}

    public static Harvest harvest(GreenhouseRecipe recipe) {
        BlockState state = null;
        int plants = 0, fertilizer = 0;
        for (var input : recipe.getItemIngredients()) {
            if (!input.isConsumable()) continue;
            ItemStack stack = input.getItemStack();
            String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (id.equals("biotech:fertilizer")) { fertilizer += stack.getCount(); continue; }
            if (state != null) throw new RecipeTransactionException("Live greenhouse requires one crop ingredient");
            Block block = stack.is(Items.MELON_SEEDS) ? Blocks.MELON
                    : stack.is(Items.PUMPKIN_SEEDS) ? Blocks.PUMPKIN : Block.byItem(stack.getItem());
            if (block == Blocks.AIR || block.defaultBlockState().hasBlockEntity()) {
                throw new RecipeTransactionException("Live greenhouse requires a plant block item or seed");
            }
            // Only known plants are inferred. Ordinary block inputs must keep explicit outputs.
            if (!(block instanceof CropBlock) && block != Blocks.MELON && block != Blocks.PUMPKIN
                    && block != Blocks.CACTUS && block != Blocks.SUGAR_CANE && block != Blocks.NETHER_WART) {
                throw new RecipeTransactionException("Unsupported live greenhouse crop; supply explicit outputs");
            }
            state = block.defaultBlockState();
            if (block instanceof CropBlock crop) state = crop.getStateForAge(crop.getMaxAge());
            else for (var property : state.getProperties()) {
                if (property instanceof IntegerProperty age && property.getName().equals("age")) {
                    state = state.setValue(age, java.util.Collections.max(age.getPossibleValues()));
                }
            }
            plants = stack.getCount();
            // A mature cactus/cane plant contributes its three harvestable stem blocks.
            if (block == Blocks.CACTUS || block == Blocks.SUGAR_CANE) plants *= 3;
        }
        int count = plants + (fertilizer > 0 ? plants / 2 : 0);
        if (state == null || count < 1 || count > 256) throw new RecipeTransactionException("Invalid greenhouse harvest count");
        return new Harvest(state, count);
    }

    public static String tableId(BlockState state) { return state.getBlock().getLootTable().toString(); }

    public static List<ItemStack> roll(ServerLevel level, BlockPos pos, BlockState state, long seed) {
        String id = tableId(state);
        var table = level.getServer().getLootData().getLootTable(new net.minecraft.resources.ResourceLocation(id));
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .create(LootContextParamSets.BLOCK);
        return table.getRandomItems(params, seed);
    }

    public static GreenhouseRecipe prepare(GreenhouseRecipe recipe, ServerLevel level, BlockPos pos) {
        if (!recipe.usesBlockLoot()) return recipe;
        Harvest harvest = harvest(recipe);
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < harvest.count(); i++) drops.addAll(roll(level, pos, harvest.state(), level.getRandom().nextLong()));
        var data = recipe.getRecipe();
        return recipe.create(recipe.getId(), new ModRecipeData(data.getIngredientItems(), AmplifiedLootOutputs.split(drops, 1),
                data.getFluidIngredients(), data.getFluidOutputs(), data.getTotalEnergy()));
    }
}
