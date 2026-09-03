package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.GreenhouseMenu;
import com.nstut.biotech.views.renderer.BiotechFluidRenderer;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.biotech.views.renderer.BiotechItemRenderer;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GreenhouseScreen extends AbstractContainerScreen<GreenhouseMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.GREENHOUSE.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 24;

    public GreenhouseScreen(GreenhouseMenu menu, Inventory inventory, Component component) { super(menu, inventory, component); }
    @Override protected void init() { super.init(); imageWidth = 212; }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        int rate = 0;
        if (menu.getIsOperating()) {
            rate = menu.getEnergyConsumeRate();
            ModRecipeData recipe = menu.getRecipe();
            String raw = recipe.getIngredientItems()[0].getItemStack().getDisplayName().getString();
            g.drawCenteredString(font, raw.substring(1, raw.length() - 1), 106, 25, 0xFFFFFF);
            if (isHovering(55, 55, 12, 12, mouseX, mouseY)) g.renderTooltip(font, List.of(Component.literal(recipe.getFluidIngredients()[0].getDisplayName().getString()), Component.literal(recipe.getFluidIngredients()[0].getAmount() + " mB")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            OutputItem[] outputs = menu.getRecipe().getOutputItems();
            for (int i = 0; i < outputs.length; i++) { String chance = outputs[i].getChance() < 1 ? " (" + (int)(outputs[i].getChance() * 100) + "%)" : ""; g.drawCenteredString(font, outputs[i].getItemStack().getCount() + chance, 55 + i * 28, 136, 0xFFFFFF); }
        }
        if (isHovering(4, 43, 9, 76, mouseX, mouseY)) {
            if (menu.getStructureValid()) g.renderTooltip(font, List.of(Component.literal("Stored Energy:"), Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"), Component.literal("Consuming: "), Component.literal(rate + " FE / t")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            else g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
        }
        if (isHovering(196, 28, 12, 75, mouseX, mouseY)) {
            if (menu.getStructureValid()) { FluidStack stored = menu.getFluidStored(); String name = stored.isEmpty() ? "Empty" : stored.getDisplayName().getString(); g.renderTooltip(font, List.of(Component.literal("Stored Fluid:"), Component.literal(name), Component.literal(stored.getAmount() + " / " + menu.getFluidCapacity() + " mB")), Optional.empty(), mouseX - leftPos, mouseY - topPos); }
            else g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
        }
        if (isHovering(97, 85, 20, 19, mouseX, mouseY)) renderProgressTooltip(g, mouseX, mouseY, rate);
        g.drawCenteredString(font, "Using", 56, 40, 0xFFFFFF);
        String name = Component.translatable("menu.title.biotech." + MachineRegistries.GREENHOUSE.id()).getString();
        g.drawString(font, name, 106 - font.width(name) / 2, 3, 0x3F3F3F, false);
    }

    private void renderProgressTooltip(GuiGraphics g, int mouseX, int mouseY, int rate) {
        if (!menu.getStructureValid()) { g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos); return; }
        if (!menu.getIsOperating()) { g.renderTooltip(font, Component.literal("Not Operating"), mouseX - leftPos, mouseY - topPos); return; }
        int cost = menu.getRecipe().getTotalEnergy(); float total = ((float)cost / rate) / 20; float current = ((float)menu.getEnergyConsumed() / rate) / 20;
        g.renderTooltip(font, List.of(Component.literal("Progress:"), Component.literal(menu.getEnergyConsumed() + " / " + cost + " FE"), Component.literal(String.format("%.1f", current) + " / " + String.format("%.1f", total) + " s")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (!menu.getStructureValid()) return;
        if (menu.getEnergyStored() > 0) { int h = getEnergyHeight(); g.blit(TEXTURE, leftPos + 4, topPos + 119 - h, 212, 100 - h, 9, h); }
        if (!menu.getFluidStored().isEmpty()) new BiotechFluidTankRenderer(menu.getFluidCapacity(), 12, 75).renderFluid(g.pose(), leftPos + 196, topPos + 28, menu.getFluidStored());
        if (!menu.getIsOperating()) return;
        g.blit(TEXTURE, leftPos + 94, topPos + 87, 212, 0, getProgressWidth() + 1, PROGRESS_HEIGHT);
        new BiotechItemRenderer(22,22).render(g.pose(), leftPos + 96, topPos + 49, menu.getRecipe().getIngredientItems()[0].getItemStack());
        OutputItem[] outputs = menu.getRecipe().getOutputItems();
        for (int i = 0; i < outputs.length; i++) new BiotechItemRenderer(16,16).render(g.pose(), leftPos + 47 + 28 * i, topPos + 118, outputs[i].getItemStack());
        new BiotechFluidRenderer().renderFluid(g.pose(), leftPos + 55, topPos + 55, 12, 12, menu.getRecipe().getFluidIngredients()[0]);
        if (menu.getRecipe().getIngredientItems().length > 1) new BiotechItemRenderer(12,12).render(g.pose(), leftPos + 53, topPos + 75, menu.getRecipe().getIngredientItems()[1].getItemStack());
    }

    public int getEnergyHeight() { int h = menu.getEnergyStored() * 76 / menu.getEnergyCapacity(); return h == 0 && menu.getEnergyStored() > 0 ? 1 : h; }
    public int getProgressWidth() { return menu.getRecipeEnergyCost() == 0 ? 0 : menu.getEnergyConsumed() * MAX_PROGRESS_WIDTH / menu.getRecipeEnergyCost(); }
}
