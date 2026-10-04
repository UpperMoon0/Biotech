package com.nstut.biotech.jei;

import com.nstut.biotech.blocks.entites.hatches.EnergyInputHatchBlockEntity;
import com.nstut.biotech.items.CapturedAnimalItem;
import com.nstut.biotech.items.MobItem;
import com.nstut.biotech.recipes.SlaughterhouseLootPreparation;
import com.nstut.biotech.recipes.SlaughterhouseRecipe;
import com.nstut.biotech.recipes.TerrestrialHabitatRecipe;
import com.nstut.nstutlib.recipes.ModRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

/** Shared JEI API wiring. Item and fluid slots stay searchable, even outside the scrolling window. */
final class JeiMachineRecipeLayout {
    private static final int THROUGHPUT = EnergyInputHatchBlockEntity.ENERGY_THROUGHPUT;
    private JeiMachineRecipeLayout() { }

    static void addSlots(IRecipeLayoutBuilder builder, ModRecipe<?> recipe) {
        for (var planned : JeiRecipeLayout.slots(recipe.getItemIngredients().size(), recipe.getFluidIngredients().size())) {
            int i = planned.index();
            if (planned.kind() == JeiRecipeLayout.Kind.ITEM) {
                var ingredient = recipe.getItemIngredients().get(i);
                var stack = ingredient.getItemStack();
                var slot = builder.addSlot(ingredient.isConsumable() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.CATALYST)
                        .setSlotName("input-item-" + i).setStandardSlotBackground().addItemStack(stack);
                slot.addRichTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable(ingredient.isConsumable()
                            ? "jei.biotech.input.consumed" : "jei.biotech.input.catalyst"));
                    if (stack.getItem() instanceof MobItem mob) {
                        tooltip.add(Component.translatable(mob.isBabyVariant()
                                ? "tooltip.biotech.captured_animal.baby" : "tooltip.biotech.captured_animal.adult"));
                    } else if (stack.getItem() instanceof CapturedAnimalItem) {
                        Component lifecycle = CapturedAnimalItem.lifecycleTooltip(stack);
                        if (lifecycle != null) tooltip.add(lifecycle);
                    }
                });
                if (!ingredient.isConsumable()) {
                    slot.setOverlay(JeiOutputChanceHelper.label("R", 0xFF55FF55), 0, 0);
                }
            } else {
                var fluid = recipe.getFluidIngredients().get(i);
                builder.addSlot(RecipeIngredientRole.INPUT).setSlotName("input-fluid-" + i)
                        .setStandardSlotBackground().addFluidStack(fluid.getFluid(), fluid.getAmount())
                        .setFluidRenderer(fluid.getAmount(), false, 16, 16);
            }
        }
        for (var planned : JeiRecipeLayout.slots(recipe.getItemOutputs().size(), recipe.getFluidOutputs().size())) {
            int i = planned.index();
            if (planned.kind() == JeiRecipeLayout.Kind.ITEM) {
                var output = recipe.getItemOutputs().get(i);
                var slot = builder.addSlot(RecipeIngredientRole.OUTPUT).setSlotName("output-item-" + i)
                        .setStandardSlotBackground().addItemStack(output.getItemStack());
                JeiOutputChanceHelper.addChance(slot, output.getChance());
                addRate(slot, output.getItemStack().getCount(), output.getChance(), recipe.getTotalEnergy(), false);
            } else {
                var fluid = recipe.getFluidOutputs().get(i);
                var slot = builder.addSlot(RecipeIngredientRole.OUTPUT).setSlotName("output-fluid-" + i)
                        .setStandardSlotBackground().addFluidStack(fluid.getFluid(), fluid.getAmount())
                        .setFluidRenderer(fluid.getAmount(), false, 16, 16);
                addRate(slot, fluid.getAmount(), 1.0f, recipe.getTotalEnergy(), true);
            }
        }
    }

    private static void addRate(IRecipeSlotBuilder slot, int count, float chance, int energy, boolean fluid) {
        slot.addRichTooltipCallback((view, tooltip) -> {
            tooltip.add(Component.translatable(fluid ? "jei.biotech.output.fluid_per_cycle" : "jei.biotech.output.per_cycle", count));
            tooltip.add(Component.translatable(fluid ? "jei.biotech.output.fluid_rate" : "jei.biotech.output.rate",
                    JeiProductionMath.expectedPerMinute(count, chance, energy, THROUGHPUT)));
            tooltip.add(Component.translatable("jei.biotech.rate.conditions"));
        });
    }

    static void addExtras(IRecipeExtrasBuilder builder, ModRecipe<?> recipe) {
        var slots = builder.getRecipeSlots().getSlots();
        var inputs = slots.stream().filter(slot -> slot.getRole() != RecipeIngredientRole.OUTPUT).toList();
        var outputs = slots.stream().filter(slot -> slot.getRole() == RecipeIngredientRole.OUTPUT).toList();
        if (!inputs.isEmpty()) builder.addScrollGridWidget(inputs, JeiRecipeLayout.COLUMNS, JeiRecipeLayout.VISIBLE_ROWS)
                .setPosition(JeiRecipeLayout.INPUT_X, JeiRecipeLayout.GRID_Y);
        if (!outputs.isEmpty()) builder.addScrollGridWidget(outputs, JeiRecipeLayout.COLUMNS, JeiRecipeLayout.VISIBLE_ROWS)
                .setPosition(JeiRecipeLayout.OUTPUT_X, JeiRecipeLayout.GRID_Y);
        builder.addRecipeArrow().setPosition(78, 31);
        builder.addText(Component.translatable("jei.biotech.inputs"), 70, 10).setPosition(0, 0);
        builder.addText(Component.translatable("jei.biotech.outputs"), 70, 10).setPosition(110, 0);
        text(builder, Component.translatable("jei.biotech.energy", recipe.getTotalEnergy()), JeiRecipeLayout.ENERGY_Y, 10);
        text(builder, Component.translatable("jei.biotech.cycle",
                JeiProductionMath.seconds(recipe.getTotalEnergy(), THROUGHPUT), THROUGHPUT), JeiRecipeLayout.CYCLE_Y, 20);
        if (recipe.getItemIngredients().stream().anyMatch(ingredient -> !ingredient.isConsumable())) {
            text(builder, Component.translatable("jei.biotech.catalyst.legend"), JeiRecipeLayout.CATALYST_Y, 10);
        }
        if (recipe instanceof SlaughterhouseRecipe slaughter && slaughter.usesEntityLoot()) {
            text(builder, Component.translatable("jei.biotech.slaughterhouse.dynamic_loot",
                    SlaughterhouseLootPreparation.getYieldMultiplier()), JeiRecipeLayout.DETAILS_Y, 20);
        } else if (recipe instanceof TerrestrialHabitatRecipe) {
            text(builder, Component.translatable("jei.biotech.habitat.production"), JeiRecipeLayout.DETAILS_Y, 20);
        }
    }

    private static void text(IRecipeExtrasBuilder builder, Component text, int y, int height) {
        // JEI wraps/truncates long translations and exposes their full text on hover.
        builder.addText(text, JeiRecipeLayout.WIDTH, height).setPosition(0, y);
    }
}
