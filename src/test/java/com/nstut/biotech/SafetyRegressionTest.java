package com.nstut.biotech;

import com.nstut.biotech.network.PacketRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafetyRegressionTest {
    @Test
    void networkProtocolIsExplicitlyVersioned() {
        assertEquals("2", PacketRegistries.PROTOCOL_VERSION);
    }
}
