package com.nstut.biotech.items;

import net.minecraft.world.entity.Entity;

/**
 * Removes world-only presentation state from the ephemeral entity used to render an animal item.
 * The captured ItemStack/NBT is not modified; releasing the animal still restores its saved state.
 */
public final class AnimalItemPreviewPresentation {
    private AnimalItemPreviewPresentation() {
    }

    public static void suppressWorldPresentation(Entity entity) {
        entity.setCustomNameVisible(false);
        entity.clearFire();
        entity.setGlowingTag(false);
    }
}
