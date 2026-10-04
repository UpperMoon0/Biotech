package com.nstut.biotech.jei;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JeiChancePresentationTest {
    @Test void tooltipRetainsEveryRecipeDecimalRatherThanRoundingToTwoPlaces() {
        assertEquals("0.001%", JeiChancePresentation.exactPercent(0.00001f));
        assertEquals("12.3456%", JeiChancePresentation.exactPercent(0.123456f));
        assertEquals("99.9999%", JeiChancePresentation.exactPercent(0.999999f));
        assertEquals("12.5%", JeiChancePresentation.exactPercent(0.125f));
        assertEquals("0%", JeiChancePresentation.exactPercent(0));
        assertEquals("100%", JeiChancePresentation.exactPercent(1));
    }

    @Test void compactLabelsDistinguishApproximationAndNeverLieAboutCertainty() {
        assertEquals("<0.1%", JeiChancePresentation.compactPercent(0.00001f));
        assertEquals("~12.3%", JeiChancePresentation.compactPercent(0.123456f));
        assertEquals(">99.9%", JeiChancePresentation.compactPercent(0.999999f));
        assertEquals("12.5%", JeiChancePresentation.compactPercent(0.125f));
        assertEquals("0%", JeiChancePresentation.compactPercent(0));
        assertEquals("100%", JeiChancePresentation.compactPercent(1));
        assertEquals("<0.1%", JeiChancePresentation.compactPercent(Float.MIN_VALUE));
        assertEquals(">99.9%", JeiChancePresentation.compactPercent(Math.nextDown(1.0f)));
        for (int i = 1; i < 100_000; i++) {
            String label = JeiChancePresentation.compactPercent(i / 100_000f);
            assertNotEquals("0%", label);
            assertNotEquals("100%", label);
            assertTrue(label.length() <= 6, label);
        }
    }

    @Test void guaranteedOutputsHaveNoChanceBadgeAndInvalidProbabilitiesFailClearly() {
        assertTrue(JeiChancePresentation.needsLabel(0));
        assertTrue(JeiChancePresentation.needsLabel(0.5f));
        assertFalse(JeiChancePresentation.needsLabel(1));
        for (float invalid : new float[]{-0.1f, 1.1f, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> JeiChancePresentation.exactPercent(invalid));
        }
    }
}
