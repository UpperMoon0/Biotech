package com.nstut.biotech.views.openui;

/** One geometry contract for item/chance rows followed by fluid rows inside a clipped viewport. */
public final class ControllerOutputLayout {
    public static final int COLUMNS = 2;
    public static final int ROW_HEIGHT = 34;
    public static final int VIEWPORT_X = 146;
    public static final int VIEWPORT_Y = 104;
    public static final int VIEWPORT_WIDTH = 60;
    public static final int VIEWPORT_HEIGHT = 86;
    private ControllerOutputLayout() { }
    public static int itemRows(int items) { return (items + COLUMNS - 1) / COLUMNS; }
    public static int itemX(int index) { return 2 + (index % COLUMNS) * 28; }
    public static int itemY(int index) { return 2 + (index / COLUMNS) * ROW_HEIGHT; }
    public static int fluidY(int items, int index) { return 2 + (itemRows(items) + index) * ROW_HEIGHT; }
    public static int contentHeight(int items, int fluids) { return 4 + (itemRows(items) + fluids) * ROW_HEIGHT; }
}
