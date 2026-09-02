package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobItemStateTest {
    @Test
    void capturedEntityStateKeepsGameplayDataButDropsWorldIdentity() {
        CompoundTag captured = new CompoundTag();
        captured.putString("CustomName", "test-animal");
        captured.putInt("Age", -1200);
        captured.putInt("UUID", 1);
        captured.putInt("Pos", 1);
        captured.putInt("Motion", 1);
        captured.putInt("Rotation", 1);
        captured.putInt("FallDistance", 1);
        captured.putInt("PortalCooldown", 1);
        captured.putInt("Leash", 1);

        CompoundTag sanitized = MobItem.sanitizeCapturedEntityTag(captured);

        assertEquals("test-animal", sanitized.getString("CustomName"));
        assertEquals(-1200, sanitized.getInt("Age"));
        assertFalse(sanitized.contains("UUID"));
        assertFalse(sanitized.contains("Pos"));
        assertFalse(sanitized.contains("Motion"));
        assertFalse(sanitized.contains("Rotation"));
        assertFalse(sanitized.contains("FallDistance"));
        assertFalse(sanitized.contains("PortalCooldown"));
        assertFalse(sanitized.contains("Leash"));
        assertTrue(captured.contains("UUID"), "sanitizing must not mutate the stored capture payload");
        assertEquals("CapturedEntity", NetTrapBlock.CAPTURED_ENTITY_TAG);
    }
}
