package com.nstut.biotech.views.openui;

import com.nstut.openui.api.UIComponent;
import com.nstut.openui.api.UiRender;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Loader-specific fluid rendering inside an OpenUI component. */
public final class FluidWidget extends LiveTooltipComponent {
    public static FluidStack empty() { return FluidStack.EMPTY; }
    private final Supplier<FluidStack> fluid;
    private final IntSupplier capacity;
    private final BooleanSupplier valid;
    public FluidWidget(Supplier<FluidStack> fluid, IntSupplier capacity, BooleanSupplier valid) {
        this.fluid = fluid; this.capacity = capacity; this.valid = valid;
    }
    @Override public int preferredWidth(Font f) { return 16; }
    @Override public int preferredHeight(Font f) { return 52; }
    public String amountLabel() {
        int amount = valid.getAsBoolean() ? fluid.get().getAmount() : 0;
        return amount >= 10000 ? String.format(java.util.Locale.ROOT, "%.0fk", amount / 1000.0) : Integer.toString(amount);
    }
    @Override protected String tooltipText(int mx, int my) {
        if (!valid.getAsBoolean()) return "Invalid Structure";
        FluidStack value = fluid.get();
        return "Fluid:\n" + (value.isEmpty() ? "Empty" : value.getDisplayName().getString())
            + "\n" + value.getAmount() + " / " + capacity.getAsInt() + " mB";
    }
    @Override public void render(GuiGraphics g, Font f, int mx, int my, float pt) {
        new com.nstut.openui.graphics.UiCanvas(g, f).surface(x - 1, y - 1, width + 2, height + 2, BiotechStyle.WELL);
        if (valid.getAsBoolean() && capacity.getAsInt() > 0) {
            new BiotechFluidTankRenderer(capacity.getAsInt(), width, height).renderFluid(g.pose(), x, y, fluid.get());
        }
    }
}
