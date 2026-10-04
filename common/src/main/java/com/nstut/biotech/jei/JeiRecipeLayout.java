package com.nstut.biotech.jei;

import java.util.ArrayList;
import java.util.List;

/** Geometry and ordering used by all machine JEI categories and their native scrolling grids. */
public final class JeiRecipeLayout {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 137;
    public static final int COLUMNS = 3;
    public static final int VISIBLE_ROWS = 3;
    public static final int SLOT_PITCH = 18;
    public static final int INPUT_X = 0;
    public static final int OUTPUT_X = 110;
    public static final int GRID_Y = 12;
    public static final int ENERGY_Y = 70;
    public static final int CYCLE_Y = 81;
    public static final int CATALYST_Y = 103;
    public static final int DETAILS_Y = 115;

    private JeiRecipeLayout() { }

    public enum Kind { ITEM, FLUID }
    public record Slot(Kind kind, int index) { }
    public record Rect(int x, int y, int width, int height) {
        public boolean overlaps(Rect other) {
            return x < other.x + other.width && x + width > other.x
                    && y < other.y + other.height && y + height > other.y;
        }
    }

    /** Fluids follow the items in the same grid instead of competing for fixed rows. */
    public static List<Slot> slots(int items, int fluids) {
        if (items < 0 || fluids < 0) throw new IllegalArgumentException("Negative ingredient count");
        List<Slot> result = new ArrayList<>();
        for (int i = 0; i < items; i++) result.add(new Slot(Kind.ITEM, i));
        for (int i = 0; i < fluids; i++) result.add(new Slot(Kind.FLUID, i));
        return List.copyOf(result);
    }

    public static int hiddenRows(int size) {
        return Math.max(0, (size + COLUMNS - 1) / COLUMNS - VISIBLE_ROWS);
    }

    /** Native JEI grids scroll by whole rows; this is the visible slot/background footprint. */
    public static List<Rect> visibleSlots(int size, int firstRow, boolean output) {
        if (size < 0 || firstRow < 0 || firstRow > hiddenRows(size)) {
            throw new IllegalArgumentException("Invalid scrolling window");
        }
        List<Rect> result = new ArrayList<>();
        int count = Math.min(COLUMNS * VISIBLE_ROWS, size - firstRow * COLUMNS);
        for (int i = 0; i < count; i++) {
            result.add(new Rect((output ? OUTPUT_X : INPUT_X) + i % COLUMNS * SLOT_PITCH,
                    GRID_Y + i / COLUMNS * SLOT_PITCH, SLOT_PITCH, SLOT_PITCH));
        }
        return List.copyOf(result);
    }
}
