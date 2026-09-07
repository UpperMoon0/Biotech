package com.nstut.biotech.views.openui;

import com.nstut.openui.minecraft.UiContainerScreen;
import com.nstut.openui.api.UiRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** OpenUI owns the display; vanilla retains all inventory and slot interaction. */
public abstract class BiotechContainerScreen<T extends AbstractContainerMenu> extends UiContainerScreen<T> {
    private final int panelWidth, panelHeight;
    protected BiotechContainerScreen(T menu, Inventory inventory, Component title, int w, int h) {
        super(menu, inventory, title);
        imageWidth = w; imageHeight = h;
        panelWidth = w; panelHeight = h;
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) { }
    @Override protected void renderBackgroundLayer(GuiGraphics g, float pt, int mx, int my) {
        UiRender.roundedOutline(g, leftPos, topPos, panelWidth, panelHeight, 6, 0xFF192622, 0xFF517464);
        for (var slot : menu.slots) {
            UiRender.slot(g, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18);
        }
    }
}
