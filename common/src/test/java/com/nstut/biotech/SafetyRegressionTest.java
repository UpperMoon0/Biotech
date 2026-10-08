package com.nstut.biotech;

import com.nstut.biotech.network.PacketRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafetyRegressionTest {
    @Test
    void newControllerDataRequiresProtocolThreeOnEveryTarget() {
        assertEquals("3", PacketRegistries.PROTOCOL_VERSION, "Older clients have no controller status/mode data slots and must be rejected at handshake");
    }
}
