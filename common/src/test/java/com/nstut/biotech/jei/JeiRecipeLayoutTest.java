package com.nstut.biotech.jei;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class JeiRecipeLayoutTest {
    @Test void fluidsFollowItemsWithoutDroppingOrMergingAnyOutputs() {
        for (int items : new int[]{0, 1, 2, 3, 9, 10, 31, 256}) {
            for (int fluids : new int[]{0, 1, 2, 3, 9, 10, 31, 256}) {
                var slots = JeiRecipeLayout.slots(items, fluids);
                assertEquals(items + fluids, slots.size());
                assertEquals(slots.size(), new HashSet<>(slots).size());
                for (int i = 0; i < items; i++) assertEquals(new JeiRecipeLayout.Slot(JeiRecipeLayout.Kind.ITEM, i), slots.get(i));
                for (int i = 0; i < fluids; i++) assertEquals(new JeiRecipeLayout.Slot(JeiRecipeLayout.Kind.FLUID, i), slots.get(items + i));
            }
        }
    }

    @Test void everySupportedCardinalityHasNonoverlappingVisibleSlotsAndEverySlotIsReachable() {
        // Recipe codecs support up to 256 items plus 256 fluids on either side.
        for (int size = 0; size <= 512; size++) {
            var seen = new HashSet<Integer>();
            for (int row = 0; row <= JeiRecipeLayout.hiddenRows(size); row++) {
                var left = JeiRecipeLayout.visibleSlots(size, row, false);
                var right = JeiRecipeLayout.visibleSlots(size, row, true);
                for (int i = 0; i < left.size(); i++) {
                    var rect = left.get(i);
                    seen.add(row * JeiRecipeLayout.COLUMNS + i);
                    assertTrue(rect.y() + rect.height() <= JeiRecipeLayout.ENERGY_Y);
                    for (int j = i + 1; j < left.size(); j++) assertFalse(rect.overlaps(left.get(j)));
                    for (var output : right) {
                        assertFalse(rect.overlaps(output));
                        assertTrue(output.x() + output.width() <= JeiRecipeLayout.WIDTH);
                    }
                }
            }
            assertEquals(size, seen.size(), "All ingredients must remain accessible at size " + size);
        }
    }

    @Test void scrollingKeepsReservedTextAndArrowAreasClear() {
        var leftGrid = new JeiRecipeLayout.Rect(0, 12, 70, 54); // 3*18 plus JEI's 16px scrollbar
        var rightGrid = new JeiRecipeLayout.Rect(110, 12, 70, 54);
        var arrow = new JeiRecipeLayout.Rect(78, 31, 24, 17);
        assertFalse(leftGrid.overlaps(rightGrid));
        assertFalse(leftGrid.overlaps(arrow));
        assertFalse(rightGrid.overlaps(arrow));
        assertEquals(JeiRecipeLayout.WIDTH, rightGrid.x() + rightGrid.width());
        assertTrue(JeiRecipeLayout.DETAILS_Y + 20 <= JeiRecipeLayout.HEIGHT);
    }

    @Test void badCountsOrScrollPositionsFailRatherThanPlaceSlotsOutsideTheCard() {
        assertThrows(IllegalArgumentException.class, () -> JeiRecipeLayout.slots(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> JeiRecipeLayout.visibleSlots(3, 1, false));
    }
}
