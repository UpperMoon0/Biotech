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
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GreenhouseScreen extends AbstractContainerScreen<GreenhouseMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.GREENHOUSE.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 24;
    private static final int OUTPUT_CENTER_X = 106;
    private static final int OUTPUT_SPACING = 28;
    private static final int OUTPUT_ITEM_SIZE = 16;

    public GreenhouseScreen(GreenhouseMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = 212;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int energyConsumeRate = 0;

        if (this.menu.getIsOperating()) {
            energyConsumeRate = menu.getEnergyConsumeRate();

            ModRecipeData recipe = menu.getRecipe();
            String seedName = recipe.getIngredientItems()[0].getItemStack().getHoverName().getString();
            pGuiGraphics.drawCenteredString(font, seedName, 106, 25, 0xFFFFFF);

            String fluidName = recipe.getFluidIngredients()[0].getDisplayName().getString();
            String fluidAmount = recipe.getFluidIngredients()[0].getAmount() + " mB";
            if (isHovering(55, 55, 12, 12, pMouseX, pMouseY)) {
                pGuiGraphics.renderTooltip(
                        font,
                        List.of(Component.literal(fluidName), Component.literal(fluidAmount)),
                        Optional.empty(),
                        pMouseX - leftPos,
                        pMouseY - topPos);
            }

            OutputItem[] outputItems = menu.getRecipe().getOutputItems();
            for (int i = 0; i < outputItems.length; i++) {
                String chance = outputItems[i].getChance() < 1
                        ? " (" + (int) (outputItems[i].getChance() * 100) + "%)"
                        : "";
                pGuiGraphics.drawCenteredString(font, outputItems[i].getItemStack().getCount() + chance, getFirstOutputCenter(outputItems.length) + i * OUTPUT_SPACING, 136, 0xFFFFFF);
            }
        }

        if (isHovering(0, 39, 17, 84, pMouseX, pMouseY)) {
            if (menu.getStructureValid()) {
                List<Component> energyTooltip = menu.getIsOperating()
                        ? List.of(
                                Component.literal("Stored Energy:"),
                                Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"),
                                Component.literal("Recipe Energy:"),
                                Component.literal(menu.getEnergyConsumed() + " / " + menu.getRecipeEnergyCost() + " FE"),
                                Component.literal("Rate: " + energyConsumeRate + " FE / t"))
                        : List.of(
                                Component.literal("Stored Energy:"),
                                Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"),
                                Component.literal("Rate: 0 FE / t"));
                pGuiGraphics.renderTooltip(font, energyTooltip, Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
            } else {
                pGuiGraphics.renderTooltip(font, Component.literal("Invalid Structure"), pMouseX - leftPos, pMouseY - topPos);
            }
        }

        if (isHovering(196, 28, 12, 75, pMouseX, pMouseY)) {
            if (menu.getStructureValid()) {
                FluidStack storedFluid = menu.getFluidStored();
                String fluidName = storedFluid.isEmpty() ? "Empty" : storedFluid.getDisplayName().getString();
                pGuiGraphics.renderTooltip(font, List.of(Component.literal("Stored Fluid:"), Component.literal(fluidName), Component.literal(storedFluid.getAmount() + " / " + menu.getFluidCapacity() + " mB")), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
            } else {
                pGuiGraphics.renderTooltip(font, Component.literal("Invalid Structure"), pMouseX - leftPos, pMouseY - topPos);
            }
        }

        if (isHovering(94, 87, MAX_PROGRESS_WIDTH + 1, PROGRESS_HEIGHT, pMouseX, pMouseY)) {
            if (menu.getStructureValid()) {
                if (menu.getIsOperating()) {
                    int energyCost = menu.getRecipe().getTotalEnergy();
                    float totalTimeProgress = MachineScreenMath.secondsForEnergy(energyCost, energyConsumeRate);
                    float currentTimeProgress = MachineScreenMath.secondsForEnergy(menu.getEnergyConsumed(), energyConsumeRate);
                    pGuiGraphics.renderTooltip(font, List.of(
                            Component.literal("Progress:"),
                            Component.literal(menu.getEnergyConsumed() + " / " + energyCost + " FE"),
                            Component.literal(String.format("%.1f", currentTimeProgress) + " / " + String.format("%.1f", totalTimeProgress) + " s")
                    ), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
                } else {
                    pGuiGraphics.renderTooltip(font, Component.literal("Not Operating"), pMouseX - leftPos, pMouseY - topPos);
                }
            } else {
                pGuiGraphics.renderTooltip(font, Component.literal("Invalid Structure"), pMouseX - leftPos, pMouseY - topPos);
            }
        }

        pGuiGraphics.drawCenteredString(font, "Using", 61, 40, 0xFFFFFF);

        String machineName = Component.translatable("menu.title.biotech." + MachineRegistries.GREENHOUSE.id()).getString();
        int x = 106 - font.width(machineName) / 2;
        pGuiGraphics.drawString(font, machineName, x, 3, 0x3F3F3F, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.getStructureValid()) {
            if (menu.getEnergyStored() > 0) {
                int energyHeight = getEnergyHeight();
                graphics.blit(TEXTURE, leftPos + 4, topPos + 119 - energyHeight, 212, 100 - energyHeight, 9, energyHeight);
            }
            if (!menu.getFluidStored().isEmpty()) {
                BiotechFluidTankRenderer fTankRenderer = new BiotechFluidTankRenderer(menu.getFluidCapacity(), 12, 75);
                fTankRenderer.renderFluid(graphics.pose(), leftPos + 196, topPos + 28, menu.getFluidStored());
            }
            if (menu.getIsOperating()) {
                graphics.blit(TEXTURE, leftPos + 94, topPos + 87, 212, 0, getProgressWidth() + 1, PROGRESS_HEIGHT);

                BiotechItemRenderer seedItemRenderer = new BiotechItemRenderer(22, 22);
                ItemStack currentSeed = menu.getRecipe().getIngredientItems()[0].getItemStack();
                seedItemRenderer.render(graphics.pose(), leftPos + 96, topPos + 49, currentSeed);

                OutputItem[] outputItems = menu.getRecipe().getOutputItems();
                for (int i = 0; i < outputItems.length; i++) {
                    BiotechItemRenderer outputItemRenderer = new BiotechItemRenderer(OUTPUT_ITEM_SIZE, OUTPUT_ITEM_SIZE);
                    outputItemRenderer.render(graphics.pose(), leftPos + getFirstOutputCenter(outputItems.length) - OUTPUT_ITEM_SIZE / 2 + OUTPUT_SPACING * i, topPos + 118, outputItems[i].getItemStack());
                }

                FluidStack currentFluid = menu.getRecipe().getFluidIngredients()[0];
                BiotechFluidRenderer fluidRenderer = new BiotechFluidRenderer();
                fluidRenderer.renderFluid(graphics.pose(), leftPos + 55, topPos + 55, 12, 12, currentFluid);

                if (menu.getRecipe().getIngredientItems().length > 1) {
                    BiotechItemRenderer fertilizerItemRenderer = new BiotechItemRenderer(12, 12);
                    ItemStack currentFertilizer = menu.getRecipe().getIngredientItems()[1].getItemStack();
                    fertilizerItemRenderer.render(graphics.pose(), leftPos + 55, topPos + 75, currentFertilizer);
                }
            }
        }
    }
    private int getFirstOutputCenter(int outputCount) {
        return OUTPUT_CENTER_X - ((Math.max(outputCount, 1) - 1) * OUTPUT_SPACING) / 2;
    }


    public int getEnergyHeight() {
        int energyHeight = menu.getEnergyStored() * 76 / menu.getEnergyCapacity();
        if (energyHeight == 0 && menu.getEnergyStored() > 0) energyHeight = 1;
        return energyHeight;
    }

    public int getProgressWidth() {
        return MachineScreenMath.progressWidth(menu.getEnergyConsumed(), menu.getRecipeEnergyCost(), MAX_PROGRESS_WIDTH);
    }
}
