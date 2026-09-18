package com.nstut.biotech.recipes;

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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public final class SlaughterhouseLootPreparation {
    public static final int YIELD_MULTIPLIER = 2;

    private SlaughterhouseLootPreparation() {
    }

    public static SlaughterhouseRecipe prepare(SlaughterhouseRecipe recipe,
                                               IItemHandler inputs,
                                               ServerLevel level,
                                               BlockPos machinePos) {
        ItemStack donor = findAnimalInput(recipe, inputs);
        LivingEntity entity = createEntity(level, donor);
        if (entity == null) {
            return recipe;
        }

        entity.moveTo(Vec3.atCenterOf(machinePos));
        ResourceKey<LootTable> lootTableId = entity.getLootTable();
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableId);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                .create(LootContextParamSets.ENTITY);

        List<ItemStack> rolled = lootTable.getRandomItems(params);
        OutputItem[] outputs = rolled.stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    ItemStack amplified = stack.copy();
                    amplified.setCount(Math.multiplyExact(stack.getCount(), YIELD_MULTIPLIER));
                    return new OutputItem(amplified, 1.0f);
                })
                .toArray(OutputItem[]::new);

        ModRecipeData source = recipe.getRecipe();
        ModRecipeData prepared = new ModRecipeData(
                source.getIngredientItems(),
                outputs,
                source.getFluidIngredients(),
                source.getFluidOutputs(),
                source.getTotalEnergy());
        return recipe.create(recipe.getId(), prepared);
    }

    private static ItemStack findAnimalInput(AnimalMobRecipe<?> recipe, IItemHandler inputs) {
        for (var ingredient : recipe.getItemIngredients()) {
            ItemStack required = ingredient.getItemStack();
            if (!(required.getItem() instanceof MobItem) && !(required.getItem() instanceof CapturedAnimalItem)) {
                continue;
            }
            for (int slot = 0; slot < inputs.getSlots(); slot++) {
                ItemStack present = inputs.getStackInSlot(slot);
                if (recipe.matchesAnimalInput(required, present)) {
                    return present.copy();
                }
            }
        }
        return ItemStack.EMPTY;
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
