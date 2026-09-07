package com.nstut.biotech.views.openui;

import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.Ui;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import static com.nstut.biotech.views.openui.BiotechWidgets.*;

/** Each machine retains its recipe diagram, using the same live OpenUI widgets. */
public final class MachineUi {
    public enum Kind { MIXER, FERMENTER, BREEDINGCHAMBER, TERRESTRIALHABITAT, GREENHOUSE, SLAUGHTERHOUSE }
    private MachineUi() { }

    public static UIComponent build(Kind kind, Component title, MachineDisplay data, UIComponent... fluids) {
        var root = Ui.stack(
                at(Ui.text(title).centered(), 24, 7, 192, 14),
                at(label(data::status), 24, 167, 192, 12),
                at(Ui.text("FE").centered(), 5, 25, 18, 10),
                at(gauge(() -> data.valid().getAsBoolean() ? data.stored().getAsInt() : 0,
                        data.capacity(), true, data::energyTooltip), 8, 39, 12, 76));
        if (fluids.length > 0) {
            root.addChild(at(fluids[0], 220, 28, 12, 75));
            root.addChild(at(Ui.text("mB").centered(), 217, 107, 18, 10));
        }

        switch (kind) {
            case MIXER -> {
                root.addChild(at(items(() -> inputs(data, 0, Integer.MAX_VALUE), 3, 20, false), 36, 43, 62, 100));
                root.addChild(at(items(() -> outputs(data), 1, 20, false), 162, 57, 52, 80));
                progress(root, data, 113, 61, 28, 12);
            }
            case FERMENTER -> {
                root.addChild(at(items(() -> inputs(data, 0, Integer.MAX_VALUE), 3, 20, false), 42, 40, 62, 60));
                root.addChild(at(items(() -> outputs(data), 1, 20, false), 168, 52, 46, 42));
                root.addChild(at(label(() -> inputName(data)), 30, 104, 180, 14));
                progress(root, data, 120, 53, 28, 12);
                recipeFluid(root, data, fluids, 45, 130);
            }
            case BREEDINGCHAMBER, TERRESTRIALHABITAT -> {
                root.addChild(at(items(() -> inputs(data, 0, 1), 1, 20, false), 66, 42, 20, 20));
                root.addChild(at(items(() -> outputs(data), 2, 30, false), 154, 40, 60, 40));
                root.addChild(at(label(() -> inputName(data)), 28, 80, 186, 14));
                root.addChild(at(items(() -> inputs(data, 1, Integer.MAX_VALUE), 4, 24, false), 45, 102, 154, 24));
                progress(root, data, 104, 44, 28, 12);
                recipeFluid(root, data, fluids, 45, 133);
            }
            case GREENHOUSE -> {
                root.addChild(at(label(() -> inputName(data)), 28, 26, 184, 14));
                root.addChild(at(items(() -> inputs(data, 0, 1), 1, 20, false), 112, 50, 20, 20));
                root.addChild(at(items(() -> inputs(data, 1, Integer.MAX_VALUE), 2, 22, false), 67, 78, 44, 24));
                root.addChild(at(items(() -> outputs(data), 6, 28, true), 28, 122, 184, 40));
                root.addChild(at(Ui.text("Using").centered(), 32, 42, 60, 12));
                if (fluids.length > 1) root.addChild(at(fluids[1], 60, 58, 12, 12));
                progress(root, data, 110, 91, 26, 14);
            }
            case SLAUGHTERHOUSE -> {
                root.addChild(at(items(() -> inputs(data, 0, 1), 1, 20, false), 56, 44, 20, 22));
                root.addChild(at(label(() -> inputName(data)), 26, 78, 100, 14));
                root.addChild(at(items(() -> outputs(data), 1, 20, false), 144, 28, 70, 132));
                progress(root, data, 100, 43, 28, 12);
                recipeFluid(root, data, fluids, 45, 107);
            }
        }
        return root;
    }

    private static void progress(UIComponent root, MachineDisplay data, int x, int y, int w, int h) {
        root.addChild(at(gauge(() -> data.active() ? data.consumed().getAsInt() : 0,
                data.cost(), false, data::progressTooltip), x, y, w, h));
    }

    private static void recipeFluid(UIComponent root, MachineDisplay data, UIComponent[] fluids, int x, int y) {
        if (fluids.length < 2) return;
        root.addChild(at(fluids[1], x, y, 18, 18));
        root.addChild(at(label(() -> data.active() && data.recipe().get().getFluidIngredients().length > 0
                ? data.recipe().get().getFluidIngredients()[0].getAmount() + " mB" : ""), x + 24, y + 5, 76, 12));
    }

    private static String inputName(MachineDisplay data) {
        return data.active() && data.recipe().get().getIngredientItems().length > 0
                ? data.recipe().get().getIngredientItems()[0].getItemStack().getHoverName().getString() : "";
    }

    private static List<Entry> inputs(MachineDisplay data, int from, int count) {
        if (!data.active()) return List.of();
        var source = data.recipe().get().getIngredientItems();
        List<Entry> result = new ArrayList<>();
        for (int i = from; i < source.length && i - from < count; i++) {
            var stack = source[i].getItemStack();
            result.add(new Entry(stack, ""));
        }
        return result;
    }

    private static List<Entry> outputs(MachineDisplay data) {
        if (!data.active()) return List.of();
        List<Entry> result = new ArrayList<>();
        for (var output : data.recipe().get().getOutputItems()) {
            result.add(new Entry(output.getItemStack(), output.getChance() < 1
                    ? (int) (output.getChance() * 100) + "%" : ""));
        }
        return result;
    }
}
