package com.nstut.biotech;

import com.nstut.biotech.views.openui.ControllerOutputLayout;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControllerOutputLayoutTest {
    @Test void threeItemsAndMilkSeparateTheChanceRowFromTheFluidWell() {
        int chanceBottom = ControllerOutputLayout.itemY(2) + 29;
        int milkWellTop = ControllerOutputLayout.fluidY(3, 0) - 2;
        assertTrue(milkWellTop > chanceBottom, "Fluid well must start below the last item chance label");
        assertTrue(ControllerOutputLayout.contentHeight(3, 1) > ControllerOutputLayout.VIEWPORT_HEIGHT,
                "Overflow must scroll rather than escape the output viewport");
    }
    @Test void everyCodecSupportedCardinalityKeepsProductsDisjointAndReachable() {
        for (int items = 0; items <= 256; items++) {
            for (int fluids = 0; fluids <= 256; fluids++) {
                int contentBottom = ControllerOutputLayout.contentHeight(items, fluids);
                if (items > 0) assertTrue(ControllerOutputLayout.itemY(items - 1) + 30 <= contentBottom);
                if (fluids > 0) {
                    if (items > 0) assertTrue(ControllerOutputLayout.fluidY(items, 0) - 2 > ControllerOutputLayout.itemY(items - 1) + 29);
                    int last = ControllerOutputLayout.fluidY(items, fluids - 1);
                    assertTrue(last + 30 <= contentBottom);
                    int offset = Math.max(0, contentBottom - ControllerOutputLayout.VIEWPORT_HEIGHT);
                    assertTrue(last + 30 - offset <= ControllerOutputLayout.VIEWPORT_HEIGHT,
                            "Final fluid must be completely reachable at the scroll limit");
                }
            }
        }
    }
}
