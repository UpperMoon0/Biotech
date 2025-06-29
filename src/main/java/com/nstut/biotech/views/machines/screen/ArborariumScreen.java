package com.nstut.biotech.views.machines.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nstut.biotech.Biotech;
import com.nstut.biotech.views.machines.menu.ArborariumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ArborariumScreen extends AbstractContainerScreen<ArborariumMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Biotech.MOD_ID, "textures/gui/arborarium_gui.png");

    public ArborariumScreen(ArborariumMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000; // Hide player inventory label
        this.titleLabelY = 10000; // Hide machine title label
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        // Render custom labels here if needed
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}