package com.nstut.biotech.views.machines.screen;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.machines.MachineRegistries;
import com.nstut.biotech.views.machines.menu.MixerMenu;
import com.nstut.biotech.views.renderer.BiotechItemRenderer;
import com.nstut.nstutlib.recipes.IngredientItem;
import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MixerScreen extends AbstractContainerScreen<MixerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/" + MachineRegistries.MIXER.id() + ".png");
    private static final int MAX_PROGRESS_WIDTH = 19;
    private static final int PROGRESS_HEIGHT = 11;

    public MixerScreen(MixerMenu menu, Inventory inventory, Component component) { super(menu, inventory, component); }
    @Override protected void init() { super.init(); imageWidth = 142; imageHeight = 114; }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        int rate = menu.getIsOperating() ? menu.getEnergyConsumeRate() : 0;
        if (isHovering(7, 35, 8, 28, mouseX, mouseY)) {
            if (menu.getStructureValid()) g.renderTooltip(font, List.of(Component.literal("Stored Energy:"), Component.literal(menu.getEnergyStored() + " / " + menu.getEnergyCapacity() + " FE"), Component.literal("Consuming: "), Component.literal(rate + " FE / t")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            else g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
        }
        if (isHovering(84, 59, MAX_PROGRESS_WIDTH, PROGRESS_HEIGHT, mouseX, mouseY)) {
            if (!menu.getStructureValid()) g.renderTooltip(font, Component.literal("Invalid Structure"), mouseX - leftPos, mouseY - topPos);
            else if (!menu.getIsOperating()) g.renderTooltip(font, Component.literal("Not Operating"), mouseX - leftPos, mouseY - topPos);
            else {
                int cost = menu.getRecipe().getTotalEnergy(); float total = ((float)cost / rate) / 20; float current = ((float)menu.getEnergyConsumed() / rate) / 20;
                g.renderTooltip(font, List.of(Component.literal("Progress:"), Component.literal(menu.getEnergyConsumed() + " / " + cost + " FE"), Component.literal(String.format("%.1f", current) + " / " + String.format("%.1f", total) + " s")), Optional.empty(), mouseX - leftPos, mouseY - topPos);
            }
        }
        String name = Component.translatable("menu.title.biotech." + MachineRegistries.MIXER.id()).getString();
        g.drawString(font, name, 21 - font.width(name) / 2, 6, 0x3F3F3F, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        renderBackground(g, mouseX, mouseY, partialTick); g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (!menu.getStructureValid()) return;
        if (menu.getEnergyStored() > 0) { int h = getEnergyHeight(); g.blit(TEXTURE, leftPos + 7, topPos + 64 - h, 142, 40 - h, 8, h); }
        if (!menu.getIsOperating()) return;
        g.blit(TEXTURE, leftPos + 84, topPos + 59, 142, 0, getProgressWidth() + 1, PROGRESS_HEIGHT);
        IngredientItem[] ingredients = menu.getRecipe().getIngredientItems();
        for (int i = 0; i < ingredients.length; i++) new BiotechItemRenderer(16,16).render(g.pose(), leftPos + 22 + (i % 3) * 18, topPos + 31 + (i / 3) * 18, ingredients[i].getItemStack());
        OutputItem output = menu.getRecipe().getOutputItems()[0];
        new BiotechItemRenderer(16,16).render(g.pose(), leftPos + 113, topPos + 57, output.getItemStack());
    }

    public int getEnergyHeight() { int h = menu.getEnergyStored() * 29 / menu.getEnergyCapacity(); return h == 0 && menu.getEnergyStored() > 0 ? 1 : h; }
    public int getProgressWidth() { return menu.getRecipeEnergyCost() == 0 ? 0 : menu.getEnergyConsumed() * MAX_PROGRESS_WIDTH / menu.getRecipeEnergyCost(); }
}
