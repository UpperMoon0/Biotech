package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.MixerMenu;
import com.nstut.biotech.views.renderer.BiotechFluidRenderer;
import com.nstut.biotech.views.renderer.BiotechFluidTankRenderer;
import com.nstut.biotech.views.renderer.BiotechItemRenderer;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.ModRecipeData;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MixerScreen extends AbstractContainerScreen<MixerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.MIXER.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 19;
    private static final int PROGRESS_HEIGHT = 11;

    public MixerScreen(MixerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = 142;
        this.imageHeight = 114;
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int energyConsumeRate = 0;

        if (this.menu.getIsOperating()) {
            energyConsumeRate = menu.getEnergyConsumeRate();

            /*
            ModRecipeData recipe = menu.getRecipe();
            String rawOutputName = recipe.getIngredientItems()[0].getItemStack().getDisplayName().getString();
            String outputName = rawOutputName.substring(1, rawOutputName.length() - 1);
            pGuiGraphics.drawCenteredString(font, outputName, 106, 102, 0xFFFFFF);

            String fluidName = recipe.getFluidIngredients()[0].getDisplayName().getString();
            String fluidAmount = recipe.getFluidIngredients()[0].getAmount() + " mB";
            if (isHovering(31, 131, 20, 20, pMouseX, pMouseY)) {
                pGuiGraphics.renderTooltip(
                        font,
                        List.of(
                                Component.literal(fluidName),
                                Component.literal(fluidAmount)
                        ),
                        Optional.empty(),
                        pMouseX - leftPos,
                        pMouseY - topPos
                );
            }
            */
        }

        if (isHovering(7, 35, 8, 28, pMouseX, pMouseY)) {
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

        /*
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
        */

        if (isHovering(84,59, MAX_PROGRESS_WIDTH, PROGRESS_HEIGHT, pMouseX, pMouseY)) {
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

        String machineName = Component.translatable("menu.title.biotech." + MachineRegistries.MIXER.id()).getString();
        int x = 21 - font.width(machineName) / 2;
        pGuiGraphics.drawString(font, machineName, x, 6, 0x3F3F3F, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.getStructureValid())
        {
            if (menu.getEnergyStored() > 0) {
                int energyHeight = getEnergyHeight();
                graphics.blit(TEXTURE, leftPos + 7, topPos + 64 - energyHeight, 142, 40 - energyHeight, 8, energyHeight);
            }
            if (menu.getIsOperating()) {
                graphics.blit(TEXTURE, leftPos + 84, topPos + 59, 142, 0, getProgressWidth() + 1, PROGRESS_HEIGHT);

                IngredientItem[] ingredientItems = menu.getRecipe().getIngredientItems();
                for (int i = 0; i < ingredientItems.length; i++) {
                    BiotechItemRenderer inputItemRenderer = new BiotechItemRenderer(16,16);
                    int x = 22 + (i % 3) * 18;
                    int y = 31 + (i / 3) * 18;
                    inputItemRenderer.render(graphics.pose(), leftPos + x, topPos + y, ingredientItems[i].getItemStack());
                }

                OutputItem outputItem = menu.getRecipe().getOutputItems()[0];
                BiotechItemRenderer outputItemRenderer = new BiotechItemRenderer(16,16);
                outputItemRenderer.render(graphics.pose(), leftPos + 113, topPos + 57, outputItem.getItemStack());

                /*
                FluidStack currentFluid = menu.getRecipe().getFluidIngredients()[0];
                BiotechFluidRenderer fluidRenderer = new BiotechFluidRenderer();
                fluidRenderer.renderFluid(graphics.pose(), leftPos + 31, topPos + 131, 20, 20, currentFluid);
                */
            }
        }
    }

    public int getEnergyHeight()
    {
        int energyHeight = menu.getEnergyStored() * 29 / menu.getEnergyCapacity();

        if (energyHeight == 0 && menu.getEnergyStored() > 0) {
            energyHeight = 1;
        }

        return energyHeight;
    }

    public int getProgressWidth() {
        return menu.getRecipeEnergyCost() == 0? 0 : menu.getEnergyConsumed() * MAX_PROGRESS_WIDTH / menu.getRecipeEnergyCost();
    }
}
