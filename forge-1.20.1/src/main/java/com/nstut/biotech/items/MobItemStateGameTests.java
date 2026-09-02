package com.nstut.biotech.items;

import com.nstut.biotech.Biotech;
import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Biotech.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MobItemStateGameTests {
    private MobItemStateGameTests() {
    }

    @GameTest(templateNamespace = "forge", template = "empty3x3x3", timeoutTicks = 40)
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

        CompoundTag sanitized = MobItem.sanitizeCapturedEntityTag(source);

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
}
