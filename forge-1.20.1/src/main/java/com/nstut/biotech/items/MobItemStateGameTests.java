package com.nstut.biotech.items;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MobItemStateGameTests {
    private MobItemStateGameTests() {
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void sanitizerPreservesGameplayStateWithoutWorldIdentity(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putString("CustomName", "Bessie");
        source.putInt("Age", -24000);
        source.putUUID("UUID", UUID.randomUUID());
        source.putString("Pos", "old-pos");
        source.putString("Motion", "old-motion");
        source.putString("Rotation", "old-rotation");
        source.putFloat("FallDistance", 4.0F);
        source.putInt("PortalCooldown", 40);
        source.putString("Leash", "old-leash");

        CompoundTag sanitized = CapturedEntityState.sanitize(source);

        helper.assertTrue("Bessie".equals(sanitized.getString("CustomName")) && sanitized.getInt("Age") == -24000,
                "Gameplay state must survive capture sanitization");
        helper.assertTrue(!sanitized.contains("UUID") && !sanitized.contains("Pos") && !sanitized.contains("Motion")
                        && !sanitized.contains("Rotation") && !sanitized.contains("FallDistance")
                        && !sanitized.contains("PortalCooldown") && !sanitized.contains("Leash"),
                "World identity and transform state must be stripped before release");
        helper.assertTrue(source.contains("UUID") && source.contains("Pos") && source.contains("Leash"),
                "Sanitizing a captured payload must not mutate the original tag");
        helper.assertTrue("CapturedEntity".equals(NetTrapBlock.CAPTURED_ENTITY_TAG),
                "Captured entity payload key must remain migration-compatible");

        helper.succeed();
    }

    @GameTest(templateNamespace = Biotech.MOD_ID, template = "empty", timeoutTicks = 40)
    public static void captureStorageRoundTripsSanitizedState(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putString("CustomName", "Bessie");
        source.putInt("Age", -1200);
        source.putUUID("UUID", UUID.randomUUID());
        source.putString("Pos", "old-pos");
        source.putString("Motion", "old-motion");
        source.putString("Leash", "old-leash");

        ItemStack captured = new ItemStack(Items.PAPER);
        CapturedAnimalStackState.writeCapture(captured, source, "minecraft:cow", -1);

        CompoundTag root = captured.getTag();
        helper.assertTrue(root != null && root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG),
                "Capture must write an entity payload to the item");
        CompoundTag stored = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG);
        helper.assertTrue("Bessie".equals(stored.getString("CustomName")) && stored.getInt("Age") == -1200,
                "Persistent entity state must survive capture storage");
        helper.assertTrue(!stored.contains("UUID") && !stored.contains("Pos")
                        && !stored.contains("Motion") && !stored.contains("Leash"),
                "Transient world identity must be removed before the payload is stored");
        helper.assertTrue("minecraft:cow".equals(root.getString(CapturedAnimalItem.ENTITY_TYPE_TAG)),
                "Capture metadata must survive alongside the sanitized payload");
        helper.assertTrue(stored.equals(CapturedAnimalStackState.read(captured)),
                "Reading the captured stack must round-trip the already-sanitized stored state");
        helper.assertTrue(source.contains("UUID") && source.contains("Pos"),
                "Writing a capture must not mutate the entity's source NBT");
        helper.succeed();
    }}
