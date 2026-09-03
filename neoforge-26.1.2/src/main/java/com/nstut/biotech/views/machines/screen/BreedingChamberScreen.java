package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.BreedingChamberMenu;
import com.nstut.biotech.views.renderer.BiotechFluidRenderer;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.biotech.views.renderer.BiotechItemRenderer;
import com.nstut.nstutlib.recipes.ModRecipeData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BreedingChamberScreen extends AbstractContainerScreen<BreedingChamberMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.BREEDING_CHAMBER.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 19;
    private static final int PROGRESS_HEIGHT = 19;

    public BreedingChamberScreen(BreedingChamberMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component, 212, 166);
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (menu.getIsOperating()) {
            ModRecipeData recipe = menu.getRecipe();
            String animal = recipe.getIngredientItems()[0].getItemStack().getDisplayName().getString();
            g.centeredText(font, animal.substring(1, animal.length() - 1), 106, 76, 0xFFFFFFFF);
            g.centeredText(font, String.valueOf(recipe.getIngredientItems()[1].getItemStack().getCount()), 95, 104, 0xFFFFFFFF);
            g.centeredText(font, recipe.getFluidIngredients()[0].getAmount() + " mB", 95, 137, 0xFFFFFFFF);
        }
        String name = Component.translatable("menu.title.biotech." + MachineRegistries.BREEDING_CHAMBER.id()).getString();
        g.text(font, name, 106 - font.width(name) / 2, 3, 0xFF3F3F3F, false);
    }

    @Override
    protected void extractTooltip(@NotNull GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractTooltip(g, mouseX, mouseY);
        int rate = menu.getIsOperating() ? menu.getEnergyConsumeRate() : 0;
        if (menu.getIsOperating()) {
            ModRecipeData recipe = menu.getRecipe();
            if (isHovering(31, 98, 20, 20, mouseX, mouseY)) {
                String food = recipe.getIngredientItems()[1].getItemStack().getDisplayName().getString();
                g.setTooltipForNextFrame(Component.literal(food.substring(1, food.length() - 1)), mouseX, mouseY);
            }
            if (isHovering(31, 131, 20, 20, mouseX, mouseY)) g.setTooltipForNextFrame(Component.literal(recipe.getFluidIngredients()[0].getDisplayName().getString()), mouseX, mouseY);
        }
        if (isHovering(4, 43, 9, 76, mouseX, mouseY)) {
            if (menu.getStructureValid()) g.setTooltipForNextFrame(font, List.of(Component.literal("Stored Energy:"), Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"), Component.literal("Consuming: "), Component.literal(rate + " FE / t")), Optional.empty(), mouseX, mouseY);
            else g.setTooltipForNextFrame(Component.literal("Invalid Structure"), mouseX, mouseY);
        }
        if (isHovering(196, 28, 12, 75, mouseX, mouseY)) {
            if (menu.getStructureValid()) {
                FluidStack stored = menu.getFluidStored();
                String name = stored.isEmpty() ? "Empty" : stored.getDisplayName().getString();
                g.setTooltipForNextFrame(font, List.of(Component.literal("Stored Fluid:"), Component.literal(name), Component.literal(stored.getAmount() + " / " + menu.getFluidCapacity() + " mB")), Optional.empty(), mouseX, mouseY);
            } else g.setTooltipForNextFrame(Component.literal("Invalid Structure"), mouseX, mouseY);
        }
        if (isHovering(88, 28, 19, 19, mouseX, mouseY)) {
            if (!menu.getStructureValid()) g.setTooltipForNextFrame(Component.literal("Invalid Structure"), mouseX, mouseY);
            else if (!menu.getIsOperating()) g.setTooltipForNextFrame(Component.literal("Not Operating"), mouseX, mouseY);
            else {
                int cost = menu.getRecipe().getTotalEnergy();
                float total = ((float) cost / rate) / 20;
                float current = ((float) menu.getEnergyConsumed() / rate) / 20;
                g.setTooltipForNextFrame(font, List.of(Component.literal("Progress:"), Component.literal(menu.getEnergyConsumed() + " / " + cost + " FE"), Component.literal(String.format("%.1f", current) + " / " + String.format("%.1f", total) + " s")), Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void extractBackground(@NotNull GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (!menu.getStructureValid()) return;
        if (menu.getEnergyStored() > 0) { int h = getEnergyHeight(); g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 4, topPos + 119 - h, 212, 95 - h, 9, h, 256, 256); }
        if (!menu.getFluidStored().isEmpty()) new BiotechFluidTankRenderer(menu.getFluidCapacity(), 12, 75).renderFluid(g, leftPos + 196, topPos + 28, menu.getFluidStored());
        if (!menu.getIsOperating()) return;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 88, topPos + 28, 212, 0, getProgressWidth() + 1, PROGRESS_HEIGHT, 256, 256);
        new BiotechItemRenderer(32, 32).render(g, leftPos + 46, topPos + 35, menu.getRecipe().getIngredientItems()[0].getItemStack());
        new BiotechItemRenderer(20, 20).render(g, leftPos + 33, topPos + 100, menu.getRecipe().getIngredientItems()[1].getItemStack());
        new BiotechItemRenderer(32, 32).render(g, leftPos + 142, topPos + 33, menu.getRecipe().getOutputItems()[0].getItemStack());
        new BiotechFluidRenderer().renderFluid(g, leftPos + 31, topPos + 131, 20, 20, menu.getRecipe().getFluidIngredients()[0]);
    }

    public int getEnergyHeight() { int h = menu.getEnergyStored() * 76 / menu.getEnergyCapacity(); return h == 0 && menu.getEnergyStored() > 0 ? 1 : h; }
    public int getProgressWidth() { return menu.getRecipeEnergyCost() == 0 ? 0 : menu.getEnergyConsumed() * MAX_PROGRESS_WIDTH / menu.getRecipeEnergyCost(); }
}
