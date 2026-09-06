package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.Biotech;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public abstract class ItemHatchScreen<T extends ItemHatchMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/item_hatch.png");

    public ItemHatchScreen(T menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(pGuiGraphics, pMouseX, pMouseY);
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack itemstack = hoveredSlot.getItem();
            pGuiGraphics.renderTooltip(font, getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, pMouseX - leftPos, pMouseY - topPos);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
