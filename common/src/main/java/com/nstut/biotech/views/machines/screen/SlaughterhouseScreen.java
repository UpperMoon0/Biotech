package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.SlaughterhouseMenu;
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

public class SlaughterhouseScreen extends AbstractContainerScreen<SlaughterhouseMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.SLAUGHTERHOUSE.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 24;

    public SlaughterhouseScreen(SlaughterhouseMenu menu, Inventory inventory, Component component) {
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
            String animalRawName = recipe.getIngredientItems()[0].getItemStack().getDisplayName().getString();
            String animalName = animalRawName.substring(1, animalRawName.length() - 1);
            pGuiGraphics.drawCenteredString(font, animalName, 70, 76, 0xFFFFFF);

            String fluidName = recipe.getFluidIngredients()[0].getDisplayName().getString();
            if (isHovering(31, 100, 20, 20, pMouseX, pMouseY)) {
                pGuiGraphics.renderTooltip(font, List.of(Component.literal(fluidName)), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
            }
            int fluidAmount = recipe.getFluidIngredients()[0].getAmount();
            pGuiGraphics.drawCenteredString(font, fluidAmount + " mB", 95, 104, 0xFFFFFF);

            OutputItem[] outputItems = menu.getRecipe().getOutputItems();

            for (int i = 0; i < outputItems.length; i++) {
                String chance = "";
                if (outputItems[i].getChance() < 1) {
                    chance = " (" + (int) (outputItems[i].getChance() * 100) + "%)";
                }
                pGuiGraphics.drawString(font, outputItems[i].getItemStack().getCount() + chance, 143, 29 + 18 * i, 0xFFFFFF);
            }
        }

        if (isHovering(4, 43, 9, 76, pMouseX, pMouseY)) {
            if (menu.getStructureValid()) {
                pGuiGraphics.renderTooltip(
                        font,
                        List.of(
                                Component.literal("Stored Energy:"),
                                Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"),
                                Component.literal("Consuming: "),
                                Component.literal(energyConsumeRate + " FE / t")
                        ),
                        Optional.empty(),
                        pMouseX - leftPos,
                        pMouseY - topPos
                );
            }
            else {
                pGuiGraphics.renderTooltip(font, Component.literal("Invalid Structure"), pMouseX - leftPos, pMouseY - topPos);
            }
        }

        if (isHovering(196, 28, 12, 75, pMouseX, pMouseY)) {
            if (menu.getStructureValid()) {
                FluidStack storedFluid = menu.getFluidStored();
                String fluidName = storedFluid.isEmpty()? "Empty" : storedFluid.getDisplayName().getString();
                pGuiGraphics.renderTooltip(font, List.of(Component.literal("Stored Fluid:"), Component.literal(fluidName), Component.literal( storedFluid.getAmount() + " / " + menu.getFluidCapacity() + " mB")), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
            }
            else {
                pGuiGraphics.renderTooltip(font, Component.literal("Invalid Structure"), pMouseX - leftPos, pMouseY - topPos);
            }
        }

        if (isHovering(88,28, 19, 19, pMouseX, pMouseY)) {
            if (menu.getStructureValid()) {
                if (menu.getIsOperating()) {
                    int energyCost = menu.getRecipe().getTotalEnergy();
                    float totalTimeProgress = ((float) energyCost / energyConsumeRate) / 20;
                    float currentTimeProgress = ((float) menu.getEnergyConsumed() / energyConsumeRate) / 20;
                    String formattedTotalTimeProgress = String.format("%.1f", totalTimeProgress);
                    String formattedCurrentTimeProgress = String.format("%.1f", currentTimeProgress);
                    pGuiGraphics.renderTooltip(font, List.of(
                            Component.literal("Progress:"),
                            Component.literal(menu.getEnergyConsumed() + " / " + energyCost + " FE"),
                            Component.literal(formattedCurrentTimeProgress + " / " + formattedTotalTimeProgress + " s")
                    ), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
                }
                else {
                    pGuiGraphics.renderTooltip(font, Component.literal("Not Operating"), pMouseX - leftPos, pMouseY - topPos);
                }
            }
            else {
                pGuiGraphics.renderTooltip(font, Component.literal("Invalid Structure"), pMouseX - leftPos, pMouseY - topPos);
            }
        }

        String machineName = Component.translatable("menu.title.biotech."+ MachineRegistries.SLAUGHTERHOUSE.id()).getString();
        int x = 106 - font.width(machineName) / 2;
        pGuiGraphics.drawString(font, machineName, x, 3, 0x3F3F3F, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.getStructureValid())
        {
            if (menu.getEnergyStored() > 0) {
                int energyHeight = getEnergyHeight();
                graphics.blit(TEXTURE, leftPos + 4, topPos + 119 - energyHeight, 212, 100 - energyHeight, 9, energyHeight);
            }
            if (!menu.getFluidStored().isEmpty())
            {
                BiotechFluidTankRenderer fTankRenderer = new BiotechFluidTankRenderer(menu.getFluidCapacity(), 12, 75);
                fTankRenderer.renderFluid(graphics.pose() , leftPos + 196, topPos + 28, menu.getFluidStored());
            }
            if (menu.getIsOperating()) {
                graphics.blit(TEXTURE, leftPos + 86, topPos + 26, 212, 0, getProgressWidth() + 1, PROGRESS_HEIGHT);

                BiotechItemRenderer animalItemRenderer = new BiotechItemRenderer(48, 48);
                ItemStack currentAnimal = menu.getRecipe().getIngredientItems()[0].getItemStack();
                animalItemRenderer.render(graphics.pose(), leftPos + 46, topPos + 35, currentAnimal);

                OutputItem[] outputItems = menu.getRecipe().getOutputItems();
                for (int i = 0; i < outputItems.length; i++) {
                    BiotechItemRenderer outputItemRenderer = new BiotechItemRenderer(16,16);
                    outputItemRenderer.render(graphics.pose(), leftPos + 123, topPos + 24 + 18 * i, outputItems[i].getItemStack());
                }

                FluidStack currentFluid = menu.getRecipe().getFluidIngredients()[0];
                BiotechFluidRenderer fluidRenderer = new BiotechFluidRenderer();
                fluidRenderer.renderFluid(graphics.pose(), leftPos + 31, topPos + 98, 20, 20, currentFluid);
            }
        }
    }

    public int getEnergyHeight()
    {
        int energyHeight = menu.getEnergyStored() * 76 / menu.getEnergyCapacity();

        if (energyHeight == 0 && menu.getEnergyStored() > 0) {
            energyHeight = 1;
        }

        return energyHeight;
    }

    public int getProgressWidth() {
        return menu.getRecipeEnergyCost() == 0? 0 : menu.getEnergyConsumed() * MAX_PROGRESS_WIDTH / menu.getRecipeEnergyCost();
    }
}