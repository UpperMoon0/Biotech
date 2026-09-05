package com.nstut.biotech.items;

import net.minecraft.nbt.CompoundTag;

final class CapturedEntityState {
    private CapturedEntityState() {}

    static CompoundTag sanitize(CompoundTag source) {
        CompoundTag captured = source.copy();
        captured.remove("UUID");
        captured.remove("Pos");
        captured.remove("Motion");
        captured.remove("Rotation");
        captured.remove("FallDistance");
        captured.remove("PortalCooldown");
        captured.remove("Leash");
        return captured;
    }
}
