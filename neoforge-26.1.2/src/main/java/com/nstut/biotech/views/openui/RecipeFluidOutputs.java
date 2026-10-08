package com.nstut.biotech.views.openui;

import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.openui.api.UiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Live recipe products, distinct from stored water and recipe fluid inputs. */
public final class RecipeFluidOutputs extends LiveTooltipComponent {
    private final MachineDisplay data;
    public RecipeFluidOutputs(MachineDisplay data) { this.data = data; }
    @Override public int preferredWidth(Font font) { return 60; }
    @Override public int preferredHeight(Font font) { return 40; }
    public String productDescription() {
        if (!data.active()) return "";
        StringBuilder products = new StringBuilder();
        for (var fluid : data.recipe().get().getFluidOutputs()) {
            if (fluid.isEmpty()) continue;
            if (!products.isEmpty()) products.append("\n");
            products.append(fluid.getHoverName().getString()).append(": ")
                    .append(fluid.getAmount()).append(" mB per cycle");
        }
        return products.toString();
    }
    @Override protected String tooltipText(int mx, int my) {
        String products = productDescription();
        return products.isEmpty() ? null : products;
    }
    @Override public void render(GuiGraphicsExtractor g, Font font, int mx, int my, float pt) {
        if (!data.active()) return;
        var fluids = data.recipe().get().getFluidOutputs();
        for (int i = 0; i < fluids.length && (i + 1) * 20 <= height; i++) {
            var fluid = fluids[i];
            if (fluid.isEmpty()) continue;
            int rowY = y + i * 20;
            new com.nstut.openui.graphics.UiCanvas(g, font).surface(x - 1, rowY - 1, 18, 18, BiotechStyle.WELL);
            new BiotechFluidTankRenderer(fluid.getAmount(), 16, 16).renderFluid(g, x, rowY, fluid);
            UiRender.text(g, font, font.plainSubstrByWidth(fluid.getAmount() + " mB", width - 18),
                    x + 18, rowY + 5, BiotechStyle.TEXT);
        }
    }
}
