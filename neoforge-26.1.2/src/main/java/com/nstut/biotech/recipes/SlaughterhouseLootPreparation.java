package com.nstut.biotech.recipes;

import com.nstut.biotech.Config;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SlaughterhouseLootPreparation {
    /** Default retained for source compatibility. Use getYieldMultiplier() for the active setting. */
    public static final int YIELD_MULTIPLIER = 2;

    public static int getYieldMultiplier() {
        return Config.slaughterhouseYieldMultiplier;
    }

    private SlaughterhouseLootPreparation() {
    }

    public static SlaughterhouseRecipe prepare(SlaughterhouseRecipe recipe,
                                               IItemHandler inputs,
                                               ServerLevel level,
                                               BlockPos machinePos) {
        if (!recipe.usesEntityLoot()) {
            return recipe;
        }
        // Bind before resolving loot, so every consumed individual contributes its own state.
        // Retained animal catalysts never yield death loot.
        PreparedAnimalInputs.Selection selection = PreparedAnimalInputs.select(recipe, inputs);
        ModRecipeData source = recipe.getRecipe();
        List<ItemStack> rolled = new ArrayList<>();
        boolean hasConsumedAnimal = false;
        for (var ingredient : selection.animals()) {
            ItemStack donor = ingredient.getItemStack();
            if (!ingredient.isConsumable()
                    || (!(donor.getItem() instanceof MobItem) && !(donor.getItem() instanceof CapturedAnimalItem))) {
                continue;
            }
            LivingEntity entity = createEntity(level, donor);
            if (entity == null) {
                throw new IllegalStateException("Dynamic slaughter requires reconstructible living animal inputs");
            }
            hasConsumedAnimal = true;
            entity.snapTo(machinePos.getX() + 0.5, machinePos.getY() + 0.5, machinePos.getZ() + 0.5, 0.0f, 0.0f);
            Optional<ResourceKey<LootTable>> lootTableId = entity.getLootTable();
            if (lootTableId.isEmpty()) continue;
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableId.get());
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, entity)
                    .withParameter(LootContextParams.ORIGIN, entity.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            for (int individual = 0; individual < donor.getCount(); individual++) {
                lootTable.getRandomItems(params, entity.getLootTableSeed(), rolled::add);
            }
        }
        if (!hasConsumedAnimal) {
            throw new IllegalStateException("Dynamic slaughter requires at least one consumed animal");
        }
        OutputItem[] outputs = AmplifiedLootOutputs.split(rolled, getYieldMultiplier());
        ModRecipeData prepared = new ModRecipeData(
                source.getIngredientItems(), outputs, source.getFluidIngredients(),
                source.getFluidOutputs(), source.getTotalEnergy());
        return recipe.create(recipe.getId(), selection.applyTo(prepared));
    }

    private static LivingEntity createEntity(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Entity entity = stack.getItem() instanceof MobItem mobItem
                ? mobItem.createMob(level, stack)
                : stack.getItem() instanceof CapturedAnimalItem captured ? captured.createCapturedEntity(level, stack) : null;
        return entity instanceof LivingEntity living ? living : null;
    }
}
