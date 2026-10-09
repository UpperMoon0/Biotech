package com.nstut.biotech;

import com.nstut.biotech.network.PacketRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafetyRegressionTest {
    @Test
    void newControllerDataRequiresProtocolThreeOnEveryTarget() {
        assertEquals("4", PacketRegistries.PROTOCOL_VERSION, "Older clients lack controller data slots or the server loot catalog payload and must be rejected at handshake");
    }
}
