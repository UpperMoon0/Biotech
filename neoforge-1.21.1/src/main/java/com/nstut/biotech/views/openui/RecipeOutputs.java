package com.nstut.biotech.views.openui;

import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.openui.api.UiRender;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Live item and fluid products share one scrolling content region. */
public final class RecipeOutputs extends LiveTooltipComponent {
    private final MachineDisplay data;
    private ModRecipeData previous;
    public RecipeOutputs(MachineDisplay data) { this.data = data; }
    private ModRecipeData recipe() { return data.active() ? data.recipe().get() : null; }
    @Override public int preferredWidth(Font font) { return 55; }
    @Override public int preferredHeight(Font font) {
        var recipe = recipe();
        return recipe == null ? 4 : ControllerOutputLayout.contentHeight(recipe.getOutputItems().length, recipe.getFluidOutputs().length);
    }
    @Override public void preRender(int mx, int my) {
        var recipe = recipe();
        if (previous != recipe) { previous = recipe; invalidateLayout(); }
        super.preRender(mx, my);
    }
    public String productDescription() {
        var recipe = recipe();
        if (recipe == null) return "";
        StringBuilder products = new StringBuilder();
        for (var fluid : recipe.getFluidOutputs()) {
            if (fluid.isEmpty()) continue;
            if (!products.isEmpty()) products.append("\n");
            products.append(fluid.getHoverName().getString()).append(": ")
                    .append(fluid.getAmount()).append(" mB per cycle");
        }
        return products.toString();
    }
    public String productAt(int mx, int my) {
        var recipe = recipe();
        if (recipe == null) return null;
        // Content may extend outside its parent while scrolled; hover must respect the viewport.
        if (parent != null && (mx < parent.getX() || mx >= parent.getX() + parent.getWidth()
                || my < parent.getY() || my >= parent.getY() + parent.getHeight())) return null;
        var items = recipe.getOutputItems();
        for (int i = 0; i < items.length; i++) {
            int cx = x + ControllerOutputLayout.itemX(i), cy = y + ControllerOutputLayout.itemY(i);
            if (mx >= cx - 2 && mx < cx + 22 && my >= cy - 2 && my < cy + 30) {
                var output = items[i];
                return output.getItemStack().getHoverName().getString() + "\n" + output.getItemStack().getCount()
                        + (output.getChance() < 1 ? " (" + (int)(output.getChance() * 100) + "%)" : "");
            }
        }
        var fluids = recipe.getFluidOutputs();
        for (int i = 0; i < fluids.length; i++) {
            int cy = y + ControllerOutputLayout.fluidY(items.length, i);
            if (mx >= x && mx < x + width && my >= cy - 2 && my < cy + 30 && !fluids[i].isEmpty()) {
                return fluids[i].getHoverName().getString() + ": " + fluids[i].getAmount() + " mB per cycle";
            }
        }
        return null;
    }
    @Override protected String tooltipText(int mx, int my) { return productAt(mx, my); }
    private boolean visibleRow(int cy) {
        return parent == null || (cy + 30 >= parent.getY() && cy - 2 < parent.getY() + parent.getHeight());
    }
    @Override public void render(GuiGraphics g, Font font, int mx, int my, float pt) {
        var recipe = recipe();
        if (recipe == null) return;
        var items = recipe.getOutputItems();
        var canvas = new com.nstut.openui.graphics.UiCanvas(g, font);
        for (int i = 0; i < items.length; i++) {
            int cx = x + ControllerOutputLayout.itemX(i), cy = y + ControllerOutputLayout.itemY(i);
            if (!visibleRow(cy)) continue;
            var output = items[i];
            canvas.surface(cx - 2, cy - 2, 22, 22, BiotechStyle.WELL);
            g.renderItem(output.getItemStack(), cx + 1, cy + 1);
            g.renderItemDecorations(font, output.getItemStack(), cx + 1, cy + 1);
            if (output.getChance() < 1) UiRender.text(g, font, (int)(output.getChance() * 100) + "%", cx, cy + 20, BiotechStyle.TEXT);
        }
        var fluids = recipe.getFluidOutputs();
        for (int i = 0; i < fluids.length; i++) {
            var fluid = fluids[i];
            if (fluid.isEmpty()) continue;
            int cy = y + ControllerOutputLayout.fluidY(items.length, i);
            if (!visibleRow(cy)) continue;
            canvas.surface(x, cy - 2, 22, 22, BiotechStyle.WELL);
            new BiotechFluidTankRenderer(fluid.getAmount(), 16, 16).renderFluid(g.pose(), x + 3, cy + 1, fluid);
            UiRender.text(g, font, font.plainSubstrByWidth(Integer.toString(fluid.getAmount()), width - 24), x + 24, cy + 2, BiotechStyle.TEXT);
            UiRender.text(g, font, "mB", x + 24, cy + 12, BiotechStyle.MUTED);
        }
    }
}
