package com.nstut.biotech.items;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public final class CapturedEntityState {
    private static final int BABY_START_AGE = -24000;

    /**
     * Newborns are new individuals. Only variant/genetic-style fields are inherited from the
     * deterministic donor parent; volatile lifecycle, ownership, inventory and world state are not.
     */
    private static final Set<String> OFFSPRING_INHERITED_KEYS = Set.of(
            "Color",
            "RabbitType",
            "Variant",
            "variant",
            "Type",
            "Markings",
            "Style",
            "Strength",
            "IsScreamingGoat",
            "HasLeftHorn",
            "HasRightHorn",
            "Attributes",
            "attributes",
            "Genes",
            "genes",
            "Genetics",
            "genetics"
    );

    private CapturedEntityState() {
    }

    public static CompoundTag sanitize(CompoundTag source) {
        CompoundTag captured = source.copy();
        captured.remove("UUID");
        captured.remove("Pos");
        captured.remove("Motion");
        captured.remove("Rotation");
        captured.remove("FallDistance");
        captured.remove("PortalCooldown");
        captured.remove("Leash");
        captured.remove("Passengers");
        captured.remove("RootVehicle");
        captured.remove("Dimension");
        return captured;
    }

    public static boolean isOffspringInheritedKey(String key) {
        return OFFSPRING_INHERITED_KEYS.contains(key);
    }

    /** Same individual, raised to adulthood: preserve sanitized state and only finish ageing. */
    public static CompoundTag asAdult(CompoundTag source) {
        CompoundTag adult = sanitize(source);
        adult.putInt("Age", 0);
        adult.putInt("ForcedAge", 0);
        return adult;
    }

    /** New individual: callers first filter the parent state to {@link #isOffspringInheritedKey}. */
    public static CompoundTag asNewborn(CompoundTag inheritedState) {
        CompoundTag newborn = sanitize(inheritedState);
        newborn.putInt("Age", BABY_START_AGE);
        newborn.putInt("ForcedAge", 0);
        newborn.remove("AgeLocked");
        return newborn;
    }
}
