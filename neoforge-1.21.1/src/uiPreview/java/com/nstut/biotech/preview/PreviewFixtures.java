package com.nstut.biotech.preview;

import com.nstut.biotech.items.ItemRegistries;
import com.nstut.biotech.views.openui.*;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.Ui;
import com.nstut.openui.api.UiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Fixed sample data: previews exercise production layouts without needing a world or server. */
final class PreviewFixtures {
    record Preview(String name, int width, int height, Supplier<UIComponent> content) { }
    private record SlotPreview(int x, int y, ItemStack stack) { }
    private PreviewFixtures() { }

    static List<Preview> all() {
        List<Preview> result = new ArrayList<>();
        for (var kind : MachineUi.Kind.values()) {
            for (String state : List.of("active", "idle", "invalid")) {
                result.add(new Preview(machineId(kind) + "-" + state, 240, 184,
                        () -> machine(kind, state)));
            }
        }
        for (String id : List.of("item_input_hatch", "item_output_hatch", "fluid_input_hatch",
                "fluid_output_hatch", "energy_input_hatch")) {
            for (boolean filled : List.of(false, true)) {
                result.add(new Preview(id + (filled ? "-filled" : "-empty"), 176, 166,
                        () -> hatch(id, filled)));
            }
        }
        return List.copyOf(result);
    }

    private static String machineId(MachineUi.Kind kind) {
        return switch (kind) {
            case MIXER -> "mixer";
            case FERMENTER -> "fermenter";
            case GREENHOUSE -> "greenhouse";
            case BREEDINGCHAMBER -> "breeding_chamber";
            case TERRESTRIALHABITAT -> "terrestrial_habitat";
            case SLAUGHTERHOUSE -> "slaughterhouse";
        };
    }

    private static Component title(String id) { return Component.translatable("menu.title.biotech." + id); }

    private static UIComponent machine(MachineUi.Kind kind, String state) {
        ModRecipeData recipe = recipe(kind);
        boolean valid = !state.equals("invalid"), active = state.equals("active");
        MachineDisplay data = new MachineDisplay(() -> valid, () -> active, () -> 307200,
                () -> 614400, () -> recipe.getTotalEnergy() / 2, recipe::getTotalEnergy,
                () -> 64, () -> active ? recipe : null);
        if (kind == MachineUi.Kind.MIXER) return MachineUi.build(kind, title(machineId(kind)), data);
        return MachineUi.build(kind, title(machineId(kind)), data,
                new FluidWidget(() -> water(16000), () -> 32000, () -> valid),
                new FluidWidget(() -> active ? recipe.getFluidIngredients()[0] : FluidStack.EMPTY,
                        () -> active ? recipe.getFluidIngredients()[0].getAmount() : 0, () -> valid));
    }

    private static ModRecipeData recipe(MachineUi.Kind kind) {
        IngredientItem[] inputs;
        OutputItem[] outputs;
        switch (kind) {
            case MIXER -> {
                inputs = new IngredientItem[]{input(Items.WHEAT, 4), input(Items.CARROT, 2), input(Items.POTATO, 2)};
                outputs = new OutputItem[]{output(ItemRegistries.COW_FEED.get(), 4, 1)};
            }
            case FERMENTER -> {
                inputs = new IngredientItem[]{input(ItemRegistries.MANURE.get(), 8)};
                outputs = new OutputItem[]{output(ItemRegistries.FERTILIZER.get(), 2, 1)};
            }
            case GREENHOUSE -> {
                inputs = new IngredientItem[]{input(Items.WHEAT_SEEDS, 1), input(ItemRegistries.FERTILIZER.get(), 1)};
                outputs = new OutputItem[]{output(Items.WHEAT, 8, 1), output(Items.WHEAT_SEEDS, 2, .25f)};
            }
            case BREEDINGCHAMBER -> {
                inputs = new IngredientItem[]{new IngredientItem(new ItemStack(ItemRegistries.COW.get(), 2), false), input(Items.WHEAT, 2)};
                outputs = new OutputItem[]{output(ItemRegistries.BABY_COW.get(), 1, 1)};
            }
            case TERRESTRIALHABITAT -> {
                inputs = new IngredientItem[]{input(ItemRegistries.BABY_COW.get(), 1), input(Items.WHEAT, 2)};
                outputs = new OutputItem[]{output(ItemRegistries.COW.get(), 1, 1), output(ItemRegistries.MANURE.get(), 2, 1)};
            }
            case SLAUGHTERHOUSE -> {
                inputs = new IngredientItem[]{input(ItemRegistries.COW.get(), 1)};
                outputs = new OutputItem[]{output(Items.BEEF, 8, 1), output(Items.LEATHER, 3, .75f)};
            }
            default -> throw new IllegalArgumentException("Missing fixture for " + kind);
        }
        return new ModRecipeData(inputs, outputs, new FluidStack[]{water(200)}, new FluidStack[0], 20000);
    }

    private static IngredientItem input(Item item, int count) { return new IngredientItem(new ItemStack(item, count), true); }
    private static OutputItem output(Item item, int count, float chance) { return new OutputItem(new ItemStack(item, count), chance); }
    private static FluidStack water(int amount) { return new FluidStack(Fluids.WATER, amount); }

    private static UIComponent hatch(String id, boolean filled) {
        List<SlotPreview> slots = new ArrayList<>();
        UIComponent content;
        if (id.startsWith("item_")) {
            content = HatchUi.items(title(id));
            for (int i = 0; i < 9; i++) slots.add(new SlotPreview(62 + (i % 3) * 18, 17 + (i / 3) * 18,
                    filled && i == 0 ? new ItemStack(Items.WHEAT, 32) : ItemStack.EMPTY));
        } else if (id.startsWith("fluid_")) {
            content = HatchUi.fluid(title(id), new FluidWidget(() -> filled ? water(16000) : FluidStack.EMPTY, () -> 32000, () -> true));
            slots.add(new SlotPreview(98, 17, filled ? new ItemStack(Items.WATER_BUCKET) : ItemStack.EMPTY));
            slots.add(new SlotPreview(98, 53, filled ? new ItemStack(Items.BUCKET) : ItemStack.EMPTY));
        } else {
            content = HatchUi.energy(title(id), () -> filled ? 307200 : 0, () -> 614400);
        }
        // These are display fixtures at the production menus' slot coordinates, not live inventories.
        for (int i = 0; i < 27; i++) slots.add(new SlotPreview(8 + (i % 9) * 18, 84 + (i / 9) * 18, ItemStack.EMPTY));
        for (int i = 0; i < 9; i++) slots.add(new SlotPreview(8 + i * 18, 142,
                filled && i == 0 ? new ItemStack(Items.CARROT, 16) : ItemStack.EMPTY));
        UIComponent inventory = new UIComponent() {
            @Override public int preferredWidth(Font font) { return 176; }
            @Override public int preferredHeight(Font font) { return 166; }
            @Override public void render(GuiGraphics g, Font font, int mx, int my, float pt) {
                for (SlotPreview slot : slots) {
                    UiRender.slot(g, x + slot.x() - 1, y + slot.y() - 1, 18, 18);
                    if (!slot.stack().isEmpty()) {
                        g.renderItem(slot.stack(), x + slot.x(), y + slot.y());
                        g.renderItemDecorations(font, slot.stack(), x + slot.x(), y + slot.y());
                    }
                }
            }
        };
        return Ui.stack(inventory, content);
    }
}
