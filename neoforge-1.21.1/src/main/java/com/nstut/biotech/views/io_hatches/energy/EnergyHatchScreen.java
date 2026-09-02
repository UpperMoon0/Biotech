package com.nstut.biotech.views.io_hatches.energy;

import com.nstut.biotech.Biotech;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class EnergyHatchScreen<T extends EnergyHatchMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/energy_hatch.png");
    public EnergyHatchScreen(T menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        if (isHovering(80, 17, 16, 52, pMouseX, pMouseY)) {
            pGuiGraphics.renderTooltip(font, List.of(Component.literal(menu.getEnergy() + " / " + menu.getBlockEntity().ENERGY_CAPACITY + " FE")), Optional.empty(), pMouseX - leftPos, pMouseY - topPos);
        }
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack itemstack = hoveredSlot.getItem();
            pGuiGraphics.renderTooltip(font, getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, pMouseX - leftPos, pMouseY - topPos);
        }
    }
    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getEnergy() > 0) {
            graphics.blit(TEXTURE, leftPos + 80, topPos + 69 - menu.getEnergyHeight(), 176, 52 - menu.getEnergyHeight(), 16, menu.getEnergyHeight());
        }
    }
}
