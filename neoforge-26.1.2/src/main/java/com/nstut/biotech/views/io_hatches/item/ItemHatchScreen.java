package com.nstut.biotech.views.io_hatches.item;

import com.nstut.biotech.Biotech;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public abstract class ItemHatchScreen<T extends ItemHatchMenu> extends AbstractContainerScreen<T> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Biotech.MOD_ID, "textures/gui/item_hatch.png");

    public ItemHatchScreen(T menu, Inventory inventory, Component component) {
        super(menu, inventory, component, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}
