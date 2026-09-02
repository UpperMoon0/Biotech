package com.nstut.biotech.blocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetTrapStateTest {
    @Test
    void capturedEntityNbtUsesStableKey() {
        assertEquals("CapturedEntity", NetTrapBlock.CAPTURED_ENTITY_TAG);
    }
}
