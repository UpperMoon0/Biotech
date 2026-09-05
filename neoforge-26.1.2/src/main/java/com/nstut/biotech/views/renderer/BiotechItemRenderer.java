package com.nstut.biotech.views.renderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class BiotechItemRenderer {
    private final float width;
    private final float height;

    public BiotechItemRenderer(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphicsExtractor graphics, int x, int y, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(width / 16.0F, height / 16.0F);
        graphics.item(itemStack, 0, 0);
        graphics.pose().popMatrix();
    }
}
