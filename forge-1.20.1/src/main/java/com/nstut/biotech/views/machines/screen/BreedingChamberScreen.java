package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.BreedingChamberMenu;
import com.nstut.biotech.views.renderer.BiotechFluidRenderer;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.biotech.views.renderer.BiotechItemRenderer;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BreedingChamberScreen extends AbstractContainerScreen<BreedingChamberMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.BREEDING_CHAMBER.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 19;
    private static final int PROGRESS_HEIGHT = 19;

    public BreedingChamberScreen(BreedingChamberMenu menu, Inventory inventory, Component component) { super(menu, inventory, component); }
    @Override protected void init() { super.init(); imageWidth = 212; }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        int rate = 0;
        if (menu.getIsOperating()) {
            rate = menu.getEnergyConsumeRate();
            ModRecipeData recipe = menu.getRecipe();
            String animalName = recipe.getIngredientItems()[0].getItemStack().getHoverName().getString();
            g.drawCenteredString(font, animalName, 106, 76, 0xFFFFFF);
            String foodName = recipe.getIngredientItems()[1].getItemStack().getHoverName().getString();
            if (isHovering(31, 98, 20, 20, mouseX, mouseY)) g.renderTooltip(font, List.of(Component.literal(foodName)), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            g.drawCenteredString(font, String.valueOf(recipe.getIngredientItems()[1].getItemStack().getCount()), 95, 104, 0xFFFFFF);
            String fluidName = recipe.getFluidIngredients()[0].getDisplayName().getString();
            if (isHovering(31, 131, 20, 20, mouseX, mouseY)) g.renderTooltip(font, List.of(Component.literal(fluidName)), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            g.drawCenteredString(font, recipe.getFluidIngredients()[0].getAmount() + " mB", 95, 137, 0xFFFFFF);
        }

        if (isHovering(0, 39, 17, 84, mouseX, mouseY)) {
            if (menu.getStructureValid()) {
                List<Component> energyTooltip = menu.getIsOperating()
                        ? List.of(
                                Component.literal("Stored Energy:"),
                                Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"),
                                Component.literal("Recipe Energy:"),
                                Component.literal(menu.getEnergyConsumed() + " / " + menu.getRecipeEnergyCost() + " FE"),
                                Component.literal("Rate: " + rate + " FE / t"))
                        : List.of(
                                Component.literal("Stored Energy:"),
                                Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"),
                                Component.literal("Rate: 0 FE / t"));
                g.renderTooltip(font, energyTooltip, Optional.empty(), mouseX - leftPos, mouseY - topPos);
            } else g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
        }

        if (isHovering(196, 28, 12, 75, mouseX, mouseY)) {
            if (menu.getStructureValid()) {
                FluidStack storedFluid = menu.getFluidStored();
                String fluidName = storedFluid.isEmpty() ? "Empty" : storedFluid.getDisplayName().getString();
                g.renderTooltip(font, List.of(Component.literal("Stored Fluid:"), Component.literal(fluidName), Component.literal(storedFluid.getAmount() + " / " + menu.getFluidCapacity() + " mB")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            } else g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
        }

        if (isHovering(88, 28, 19, 19, mouseX, mouseY)) {
            if (!menu.getStructureValid()) g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
            else if (!menu.getIsOperating()) g.renderTooltip(font, Component.literal("Not Operating"), mouseX - leftPos, mouseY - topPos);
            else {
                int cost = menu.getRecipe().getTotalEnergy();
                float total = ((float) cost / rate) / 20;
                float current = ((float) menu.getEnergyConsumed() / rate) / 20;
                g.renderTooltip(font, List.of(Component.literal("Progress:"), Component.literal(menu.getEnergyConsumed() + " / " + cost + " FE"), Component.literal(String.format("%.1f", current) + " / " + String.format("%.1f", total) + " s")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            }
        }

        String name = Component.translatable("menu.title.biotech." + MachineRegistries.BREEDING_CHAMBER.id()).getString();
        g.drawString(font, name, 106 - font.width(name) / 2, 3, 0x3F3F3F, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        renderBackground(g);
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (!menu.getStructureValid()) return;
        if (menu.getEnergyStored() > 0) { int h = getEnergyHeight(); g.blit(TEXTURE, leftPos + 4, topPos + 119 - h, 212, 95 - h, 9, h); }
        if (!menu.getFluidStored().isEmpty()) new BiotechFluidTankRenderer(menu.getFluidCapacity(), 12, 75).renderFluid(g.pose(), leftPos + 196, topPos + 28, menu.getFluidStored());
        if (!menu.getIsOperating()) return;
        g.blit(TEXTURE, leftPos + 88, topPos + 28, 212, 0, getProgressWidth() + 1, PROGRESS_HEIGHT);
        new BiotechItemRenderer(32, 32).render(g.pose(), leftPos + 46, topPos + 35, menu.getRecipe().getIngredientItems()[0].getItemStack());
        new BiotechItemRenderer(20, 20).render(g.pose(), leftPos + 33, topPos + 100, menu.getRecipe().getIngredientItems()[1].getItemStack());
        new BiotechItemRenderer(32, 32).render(g.pose(), leftPos + 142, topPos + 33, menu.getRecipe().getOutputItems()[0].getItemStack());
        new BiotechFluidRenderer().renderFluid(g.pose(), leftPos + 31, topPos + 131, 20, 20, menu.getRecipe().getFluidIngredients()[0]);
    }

    public int getEnergyHeight() { int h = menu.getEnergyStored() * 76 / menu.getEnergyCapacity(); return h == 0 && menu.getEnergyStored() > 0 ? 1 : h; }
    public int getProgressWidth() { return menu.getRecipeEnergyCost() == 0 ? 0 : menu.getEnergyConsumed() * MAX_PROGRESS_WIDTH / menu.getRecipeEnergyCost(); }
}
