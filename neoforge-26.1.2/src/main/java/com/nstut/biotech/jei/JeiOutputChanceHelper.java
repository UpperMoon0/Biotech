package com.nstut.biotech.jei;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Slot-owned overlays move with JEI's scroll grids; their tooltips retain full precision. */
final class JeiOutputChanceHelper {
    private JeiOutputChanceHelper() { }

    static void addChance(IRecipeSlotBuilder slot, float chance) {
        if (!JeiChancePresentation.needsLabel(chance)) return;
        slot.setOverlay(label(JeiChancePresentation.compactPercent(chance), 0xFFFFFFFF), 0, 0);
        slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                "jei.biotech.chance.exact", JeiChancePresentation.exactPercent(chance), Float.toString(chance))));
    }

    static IDrawable label(String text, int color) {
        return new IDrawable() {
            @Override public int getWidth() { return 16; }
            @Override public int getHeight() { return 5; }
            @Override public void draw(GuiGraphicsExtractor graphics, int x, int y) {
                var font = Minecraft.getInstance().font;
                float scale = Math.min(0.5f, 16.0f / Math.max(1, font.width(text)));
                graphics.pose().pushMatrix();
                graphics.pose().translate((float) x, (float) y);
                graphics.pose().scale(scale, scale);
                graphics.text(font, text, 0, 0, color, true);
                graphics.pose().popMatrix();
            }
        };
    }
}
