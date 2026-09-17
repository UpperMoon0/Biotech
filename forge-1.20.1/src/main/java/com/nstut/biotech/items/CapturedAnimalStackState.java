package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;

public final class CapturedAnimalStackState {
    private CapturedAnimalStackState() {
    }

    public static CompoundTag read(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            return new CompoundTag();
        }
        return CapturedEntityState.sanitize(root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG));
    }

    public static void write(ItemStack stack, CompoundTag state) {
        stack.getOrCreateTag().put(
                NetTrapBlock.CAPTURED_ENTITY_TAG,
                CapturedEntityState.sanitize(state));
    }

    public static CompoundTag forAdult(ItemStack source) {
        return CapturedEntityState.asAdult(read(source));
    }

    public static CompoundTag forOffspring(ItemStack donorParent) {
        CompoundTag inherited = read(donorParent);
        for (String key : new HashSet<>(inherited.getAllKeys())) {
            if (!CapturedEntityState.isOffspringInheritedKey(key)) {
                inherited.remove(key);
            }
        }
        return CapturedEntityState.asNewborn(inherited);
    }
}
