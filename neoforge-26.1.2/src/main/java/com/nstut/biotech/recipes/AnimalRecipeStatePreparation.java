package com.nstut.biotech.recipes;

import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.CapturedAnimalStackState;
import com.nstut.biotech.items.MobItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public final class AnimalRecipeStatePreparation {
    private AnimalRecipeStatePreparation() {
    }

    /** Resolve vanilla offspring once, before committing the persisted transaction. */
    public static BreedingChamberRecipe prepareBreeding(BreedingChamberRecipe recipe, IItemHandler inputs,
                                                        net.minecraft.server.level.ServerLevel level) {
        PreparedAnimalInputs.Selection selection = PreparedAnimalInputs.select(recipe, inputs);
        List<ItemStack> parents = new ArrayList<>();
        for (IngredientItem selected : selection.animals()) {
            for (int i = 0; i < selected.getItemStack().getCount(); i++) {
                ItemStack individual = selected.getItemStack().copy();
                individual.setCount(1);
                parents.add(individual);
                if (parents.size() > 2) throw new com.nstut.nstutlib.recipes.RecipeTransactionException("Breeding requires exactly two parents");
            }
        }
        if (parents.size() != 2) throw new com.nstut.nstutlib.recipes.RecipeTransactionException("Breeding requires two parents");
        var first = breedingParent(level, parents.get(0));
        var second = breedingParent(level, parents.get(1));
        if (first == null || second == null || first.isBaby() || second.isBaby()) {
            throw new com.nstut.nstutlib.recipes.RecipeTransactionException("Breeding requires two compatible adult animals");
        }
        // Machine recipes replace the natural cooldown/feeding step, not species mating rules.
        first.setAge(0); second.setAge(0);
        first.setInLoveTime(600); second.setInLoveTime(600);
        if (!first.canMate(second) || !second.canMate(first)) {
            throw new com.nstut.nstutlib.recipes.RecipeTransactionException("Parents do not satisfy vanilla mating requirements");
        }
        ModRecipeData prepared = selection.applyTo(recipe.getRecipe());
        List<OutputItem> outputs = new ArrayList<>();
        for (int index : recipe.rollItemOutputIndexes()) {
            ItemStack template = prepared.getOutputItems()[index].getItemStack();
            if (!(template.getItem() instanceof MobItem) && !(template.getItem() instanceof CapturedAnimalItem)) {
                appendResolvedOutput(outputs, template);
                continue;
            }
            for (int i = 0; i < template.getCount(); i++) {
                var child = first.getBreedOffspring(level, second);
                if (child == null || (!CapturedAnimalStackState.entityTypeId(template).isEmpty()
                        && !CapturedAnimalStackState.entityTypeId(template).equals(net.minecraft.world.entity.EntityType.getKey(child.getType()).toString()))) {
                    throw new com.nstut.nstutlib.recipes.RecipeTransactionException("Vanilla breeding did not produce a matching offspring");
                }
                try {
                    var saved = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
                    child.saveWithoutId(saved);
                    CompoundTag state = saved.buildResult();
                    ItemStack newborn = template.copy();
                    newborn.setCount(1);
                    ItemStack childIdentity = template.copy();
                    CapturedAnimalStackState.writeCapture(childIdentity, state, net.minecraft.world.entity.EntityType.getKey(child.getType()).toString(), -1);
                    CapturedAnimalStackState.writeDerived(newborn, childIdentity, com.nstut.biotech.items.CapturedEntityState.asNewborn(state));
                    appendResolvedOutput(outputs, newborn);
                } finally { child.discard(); }
            }
        }
        first.discard();
        second.discard();
        return recipe.create(recipe.getId(), new ModRecipeData(prepared.getIngredientItems(), outputs.toArray(OutputItem[]::new),
                prepared.getFluidIngredients(), prepared.getFluidOutputs(), prepared.getTotalEnergy()));
    }

    private static net.minecraft.world.entity.animal.Animal breedingParent(net.minecraft.server.level.ServerLevel level, ItemStack stack) {
        var entity = stack.getItem() instanceof MobItem mob ? mob.createMob(level, stack)
                : stack.getItem() instanceof CapturedAnimalItem captured ? captured.createCapturedEntity(level, stack) : null;
        return entity instanceof net.minecraft.world.entity.animal.Animal animal ? animal : null;
    }

    public static TerrestrialHabitatRecipe prepareGrowth(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        return prepareHabitat(recipe, inputs);
    }

    public static TerrestrialHabitatRecipe prepareHabitat(TerrestrialHabitatRecipe recipe, IItemHandler inputs) {
        PreparedAnimalInputs.Selection selection = PreparedAnimalInputs.select(recipe, inputs);
        if (selection.animals().isEmpty()) return recipe;
        ModRecipeData prepared = selection.applyTo(recipe.getRecipe());
        List<ItemStack> individuals = new ArrayList<>();
        for (IngredientItem selected : selection.animals()) {
            if (!selected.isConsumable()) continue;
            for (int count = 0; count < selected.getItemStack().getCount(); count++) {
                ItemStack individual = selected.getItemStack().copy();
                individual.setCount(1);
                individuals.add(individual);
            }
        }
        // Each consumed individual can donate its full payload once. In particular a count-three
        // output must not clone the first lamb's name, variant, or inventory over all three adults.
        List<OutputItem> outputs = new ArrayList<>();
        // Resolve the original output groups once, before splitting their distinct state payloads.
        // All members of a probabilistic batch therefore still succeed or fail together, and the
        // snapshot contains the decision so blocked ticks and reloads cannot reroll it.
        for (int outputIndex : recipe.rollItemOutputIndexes()) {
            OutputItem output = prepared.getOutputItems()[outputIndex];
            ItemStack template = output.getItemStack();
            if (!(template.getItem() instanceof MobItem) && !(template.getItem() instanceof CapturedAnimalItem)) {
                appendResolvedOutput(outputs, template);
                continue;
            }
            for (int count = 0; count < template.getCount(); count++) {
                ItemStack grown = template.copy();
                grown.setCount(1);
                for (int index = 0; index < individuals.size(); index++) {
                    ItemStack source = individuals.get(index);
                    if (!isAnimalOutputFor(grown, CapturedAnimalStackState.entityTypeId(source))) continue;
                    CapturedAnimalStackState.writeDerived(grown, source, CapturedAnimalStackState.forAdult(source));
                    individuals.remove(index);
                    break;
                }
                appendResolvedOutput(outputs, grown);
            }
        }
        prepared = new ModRecipeData(prepared.getIngredientItems(), outputs.toArray(OutputItem[]::new),
                prepared.getFluidIngredients(), prepared.getFluidOutputs(), prepared.getTotalEnergy());
        ItemStack donor = selection.animals().get(0).getItemStack();
        applySheepWoolColor(prepared, donor, CapturedAnimalStackState.read(donor));
        return recipe.create(recipe.getId(), prepared);
    }

    /** Merge only identical complete payloads after resolving their original chance groups. */
    private static void appendResolvedOutput(List<OutputItem> outputs, ItemStack stack) {
        ItemStack remaining = stack.copy();
        int limit = Math.min(99, remaining.getMaxStackSize());
        for (OutputItem output : outputs) {
            ItemStack existing = output.getItemStack();
            if (!ItemStack.isSameItemSameComponents(existing, remaining)) continue;
            int moved = Math.min(remaining.getCount(), Math.max(0, limit - existing.getCount()));
            ItemStack merged = existing.copy();
            merged.grow(moved);
            // 26.1 RecipeItem also caches a serialization template; always refresh via its setter.
            output.setItemStack(merged);
            remaining.shrink(moved);
            if (remaining.isEmpty()) return;
        }
        while (!remaining.isEmpty()) {
            if (outputs.size() >= 256) {
                // The provider persists at most 256 entries. Abort before startRecipe/consumption
                // rather than create an in-flight transaction that cannot survive a save/reload.
                throw new com.nstut.nstutlib.recipes.RecipeTransactionException("Prepared animal outputs exceed the snapshot entry limit");
            }
            ItemStack part = remaining.copy();
            part.setCount(Math.min(limit, remaining.getCount()));
            outputs.add(new OutputItem(part, 1.0f));
            remaining.shrink(part.getCount());
        }
    }

    private static void applySheepWoolColor(ModRecipeData prepared, ItemStack donor, CompoundTag state) {
        if (!"minecraft:sheep".equals(CapturedAnimalStackState.entityTypeId(donor))) {
            return;
        }
        int colorId = sheepColorId(donor, state);
        Item wool = woolFor(DyeColor.byId(colorId));
        for (OutputItem output : prepared.getOutputItems()) {
            ItemStack stack = output.getItemStack();
            if (!stack.is(Items.WHITE_WOOL)) {
                continue;
            }
            ItemStack recolored = new ItemStack(wool, stack.getCount());
            // Keep recipe-authored values and removals while using the new wool item's defaults.
            recolored.applyComponents(stack.getComponentsPatch());
            output.setItemStack(recolored);
        }
    }

    /** JEI alternatives mirror runtime recoloring, including authored metadata and count. */
    public static List<ItemStack> habitatOutputVariants(TerrestrialHabitatRecipe recipe, ItemStack authored) {
        if (!authored.is(Items.WHITE_WOOL) || recipe.getItemIngredients().stream().noneMatch(
                input -> "minecraft:sheep".equals(CapturedAnimalStackState.entityTypeId(input.getItemStack())))) {
            return List.of(authored.copy());
        }
        List<ItemStack> variants = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            ItemStack variant = new ItemStack(woolFor(color), authored.getCount());
            variant.applyComponents(authored.getComponentsPatch());
            variants.add(variant);
        }
        return variants;
    }

    private static int sheepColorId(ItemStack donor, CompoundTag state) {
        int capturedColor = state.getByte("Color").map(Byte::toUnsignedInt).orElse(-1);
        if (capturedColor >= 0) {
            return capturedColor;
        }
        CompoundTag root = donor.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        return root.getInt("SheepColor").orElse(0);
    }

    private static Item woolFor(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
        };
    }

    private static boolean isAnimalOutputFor(ItemStack stack, String donorType) {
        if (stack.getItem() instanceof MobItem mobItem) {
            return mobItem.entityType() != null
                    && donorType.equals(net.minecraft.world.entity.EntityType.getKey(mobItem.entityType()).toString());
        }
        if (stack.getItem() instanceof CapturedAnimalItem) {
            String outputType = CapturedAnimalStackState.entityTypeId(stack);
            return outputType.isEmpty() || outputType.equals(donorType);
        }
        return false;
    }
}
