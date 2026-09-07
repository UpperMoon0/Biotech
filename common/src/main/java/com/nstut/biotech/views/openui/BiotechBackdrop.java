package com.nstut.biotech.views.openui;

import com.nstut.openui.graphics.UiDrawContext;

/** Background-only composition, painted before vanilla inventory slots. */
public final class BiotechBackdrop {
    private BiotechBackdrop() { }
    public static void paint(UiDrawContext canvas, int x, int y, int width, int height) {
        if (width == 176) {
            canvas.surface(x - 8, y - 32, width + 16, height + 40, BiotechStyle.SHELL);
            canvas.surface(x, y, 176, 71, BiotechStyle.CARD);
            canvas.surface(x, y + 74, 176, 90, BiotechStyle.CARD);
        } else canvas.surface(x, y, width, height, BiotechStyle.SHELL);
    }
}
