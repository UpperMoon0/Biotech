package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapturedAnimalCaptureStateTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
    @Test
    void captureStorageSanitizesBeforePersistingAndRoundTrips() {
        CompoundTag source = new CompoundTag();
        source.putString("CustomName", "Bessie");
        source.putInt("Age", -1200);
        source.putInt("UUID", 1);
        source.putInt("Pos", 1);
        source.putInt("Motion", 1);
        source.putInt("Rotation", 1);
        source.putInt("Leash", 1);

        ItemStack stack = new ItemStack(Items.PAPER);
        CapturedAnimalStackState.writeCapture(stack, source, "minecraft:cow", -1);

        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag stored = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG);
        assertEquals("Bessie", stored.getString("CustomName"));
        assertEquals(-1200, stored.getInt("Age"));
        assertFalse(stored.contains("UUID"));
        assertFalse(stored.contains("Pos"));
        assertFalse(stored.contains("Motion"));
        assertFalse(stored.contains("Rotation"));
        assertFalse(stored.contains("Leash"));
        assertEquals("minecraft:cow", root.getString(CapturedAnimalItem.ENTITY_TYPE_TAG));
        assertEquals(stored, CapturedAnimalStackState.read(stack));
        assertTrue(source.contains("UUID"), "capture storage must not mutate the source entity NBT");
    }
}