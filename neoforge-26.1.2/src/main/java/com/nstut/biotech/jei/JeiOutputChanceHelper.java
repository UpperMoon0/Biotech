package com.nstut.biotech.jei;

import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

final class JeiOutputChanceHelper {
    private JeiOutputChanceHelper() {
    }

    static void drawItemChances(GuiGraphicsExtractor graphics,
                                List<OutputItem> outputs,
                                int startX,
                                int startY,
                                int columns,
                                int xStep,
                                int yStep) {
        for (int i = 0; i < outputs.size(); i++) {
            float chance = outputs.get(i).getChance();
            if (chance >= 1.0f) {
                continue;
            }
            int x = startX + (i % columns) * xStep;
            int y = startY + (i / columns) * yStep;
            String label = format(chance);
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) x + 1.0f, (float) y + 11.0f);
            graphics.pose().scale(0.5f, 0.5f);
            graphics.text(Minecraft.getInstance().font, label, 0, 0, 0xFFFFFFFF, true);
            graphics.pose().popMatrix();
        }
    }

    static String format(float chance) {
        return BigDecimal.valueOf((double) chance)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }
}
