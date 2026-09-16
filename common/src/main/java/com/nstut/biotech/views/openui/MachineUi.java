package com.nstut.biotech.views.openui;

import com.nstut.openui.api.UIComponent;
import com.nstut.biotech.machines.MachineRegistries;
import net.minecraft.world.item.ItemStack;
import com.nstut.openui.api.Ui;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import static com.nstut.biotech.views.openui.BiotechWidgets.*;

/** Each machine retains its recipe diagram, using the same live OpenUI widgets. */
public final class MachineUi {
    public enum Kind { MIXER, FERMENTER, BREEDINGCHAMBER, TERRESTRIALHABITAT, GREENHOUSE, SLAUGHTERHOUSE }

    // Shared content grid. Every machine places semantically equivalent content on these anchors.
    private static final int INPUT_X = 28;
    private static final int INPUT_W = 72;
    private static final int OUTPUT_X = 146;
    private static final int OUTPUT_W = 60;
    private static final int PRIMARY_Y = 104;
    private static final int SECONDARY_Y = 151;
    private static final int PROGRESS_X = 109;
    private static final int PROGRESS_Y = 111;
    private static final int PROGRESS_W = 28;
    private static final int PROGRESS_H = 7;

    private MachineUi() { }

    public static UIComponent build(Kind kind, Component title, MachineDisplay data, UIComponent... fluids) {
        var root = Ui.stack(
                at(Ui.icon(new ItemStack(switch (kind) {
                    case MIXER -> MachineRegistries.MIXER.block().get();
                    case FERMENTER -> MachineRegistries.FERMENTER.block().get();
                    case GREENHOUSE -> MachineRegistries.GREENHOUSE.block().get();
                    case BREEDINGCHAMBER -> MachineRegistries.BREEDING_CHAMBER.block().get();
                    case TERRESTRIALHABITAT -> MachineRegistries.TERRESTRIAL_HABITAT.block().get();
                    case SLAUGHTERHOUSE -> MachineRegistries.SLAUGHTERHOUSE.block().get();
                })), 14, 15, 20, 20),
                at(text(title::getString, BiotechStyle.TEXT), 42, 12, 166, 12),
                at(caption("control_panel"), 42, 28, 166, 10),
                at(surface(BiotechStyle.BADGE), 218, 10, 88, 25),
                at(text(() -> Component.translatable(!data.valid().getAsBoolean() ? "ui.biotech.invalid" :
                        data.active() ? "ui.biotech.operating" : "ui.biotech.idle").getString(),
                        BiotechStyle.MINT), 226, 19, 76, 10),
                at(surface(BiotechStyle.CARD), 12, 48, 204, 162),
                at(caption("process"), 24, 58, 176, 10),
                at(caption("inputs"), 26, 81, 76, 10),
                at(caption("outputs"), 144, 81, 64, 10),
                at(surface(BiotechStyle.CARD), 224, 48, 84, 70),
                at(caption("energy"), 234, 58, 64, 10),
                at(text(() -> (data.valid().getAsBoolean() ? data.stored().getAsInt() : 0) + " FE",
                        BiotechStyle.TEXT), 234, 74, 66, 10),
                at(gauge(() -> data.valid().getAsBoolean() ? data.stored().getAsInt() : 0,
                        data.capacity(), false, data::energyTooltip, 0xFFE8BC5A), 234, 93, 62, 7),
                at(text(() -> (data.active() ? data.rate().getAsInt() : 0) + " FE/t",
                        BiotechStyle.MUTED), 234, 104, 62, 10),
                at(surface(BiotechStyle.CARD), 224, 126, 84, 84));

        if (fluids.length > 0) {
            root.addChild(at(caption("fluid"), 234, 136, 62, 10));
            root.addChild(at(fluids[0], 237, 152, 16, 45));
            root.addChild(at(text(() -> fluids[0] instanceof FluidWidget tank ? tank.amountLabel() + " mB" : "",
                    BiotechStyle.TEXT), 258, 169, 44, 10));
        } else {
            root.addChild(at(caption("recipe"), 234, 136, 62, 10));
            root.addChild(at(text(() -> data.active() ? data.cost().getAsInt() + " FE" : "-",
                    BiotechStyle.TEXT), 234, 156, 64, 10));
        }

        // Primary lane is identical for every machine: input(s) -> progress -> output(s).
        int primaryInputCount = switch (kind) {
            case MIXER, FERMENTER -> Integer.MAX_VALUE;
            default -> 1;
        };
        root.addChild(at(items(() -> inputs(data, 0, primaryInputCount), 3, 24, true),
                INPUT_X, PRIMARY_Y, INPUT_W, 40));
        root.addChild(at(items(() -> outputs(data), 2, 30, true),
                OUTPUT_X, PRIMARY_Y, OUTPUT_W, 40));
        progress(root, data, PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H);

        // Optional recipe requirements occupy one shared secondary baseline.
        switch (kind) {
            case MIXER -> { }
            case FERMENTER -> recipeFluid(root, data, fluids, 31, SECONDARY_Y);
            case BREEDINGCHAMBER, TERRESTRIALHABITAT -> {
                root.addChild(at(items(() -> inputs(data, 1, Integer.MAX_VALUE), 3, 24, true),
                        INPUT_X, SECONDARY_Y, INPUT_W, 30));
                recipeFluid(root, data, fluids, 128, SECONDARY_Y);
            }
            case GREENHOUSE -> {
                root.addChild(at(items(() -> inputs(data, 1, Integer.MAX_VALUE), 3, 24, true),
                        INPUT_X, SECONDARY_Y, INPUT_W, 30));
                recipeFluid(root, data, fluids, 104, SECONDARY_Y);
            }
            case SLAUGHTERHOUSE -> recipeFluid(root, data, fluids, 31, SECONDARY_Y);
        }

        root.addChild(at(text(() -> data.active() ? inputName(data) :
                Component.translatable(data.valid().getAsBoolean() ? "ui.biotech.waiting" : "ui.biotech.check_structure").getString(),
                BiotechStyle.MUTED), 24, 192, 180, 10));
        return root;
    }

    private static void progress(UIComponent root, MachineDisplay data, int x, int y, int w, int h) {
        root.addChild(at(gauge(() -> data.active() ? data.consumed().getAsInt() : 0,
                data.cost(), false, data::progressTooltip), x, y, w, h));
        root.addChild(at(text(() -> ">", BiotechStyle.MINT), x + w + 3, y - 1, 8, 10));
        root.addChild(at(label(() -> data.active() ?
                DisplayMath.fill(data.consumed().getAsInt(), data.cost().getAsInt(), 100) + "%" : ""),
                x - 2, y + 12, w + 14, 10));
    }

    private static void recipeFluid(UIComponent root, MachineDisplay data, UIComponent[] fluids, int x, int y) {
        if (fluids.length < 2) return;
        root.addChild(at(fluids[1], x, y, 18, 18));
        root.addChild(at(text(() -> data.active() && data.recipe().get().getFluidIngredients().length > 0
                ? data.recipe().get().getFluidIngredients()[0].getAmount() + " mB" : "",
                BiotechStyle.TEXT), x + 23, y + 5, 52, 10));
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
