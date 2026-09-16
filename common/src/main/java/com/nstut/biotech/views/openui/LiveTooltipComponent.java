package com.nstut.biotech.views.openui;

import com.nstut.openui.api.UIComponent;
import com.nstut.openui.controls.Tooltip;
import com.nstut.openui.overlay.OverlayHandle;
import com.nstut.openui.overlay.OverlayLayer;

/** Refreshes live menu values even when the pointer remains stationary. */
public abstract class LiveTooltipComponent extends UIComponent {
    private OverlayHandle handle;
    private Tooltip overlay;
    private String previous;
    private long hoverStarted;
    private int lastMouseX = Integer.MIN_VALUE, lastMouseY = Integer.MIN_VALUE;

    protected abstract String tooltipText(int mouseX, int mouseY);

    @Override public void preRender(int mouseX, int mouseY) {
        super.preRender(mouseX, mouseY);
        String next = isHovered() ? tooltipText(mouseX, mouseY) : null;
        if (next == null || runtime() == null || runtime().overlays().hasBlockingOverlay()) {
            closeTooltip();
            hoverStarted = 0;
            return;
        }
        if (hoverStarted == 0) hoverStarted = System.nanoTime();
        if (System.nanoTime() - hoverStarted < 350_000_000L) return;
        if (!next.equals(previous)) {
            closeTooltip();
            overlay = new Tooltip(next);
            handle = runtime().overlays().show(OverlayLayer.TOOLTIP, overlay);
            previous = next;
        }
        if (overlay != null && (mouseX != lastMouseX || mouseY != lastMouseY)) {
            overlay.setPosition(mouseX, mouseY);
            runtime().requestOverlayLayout();
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    private void closeTooltip() {
        if (handle != null) handle.close();
        handle = null;
        overlay = null;
        previous = null;
        lastMouseX = Integer.MIN_VALUE;
        lastMouseY = Integer.MIN_VALUE;
    }

    @Override protected void onUnmount() {
        closeTooltip();
        hoverStarted = 0;
    }
}
