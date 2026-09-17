package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashSet;

public final class CapturedAnimalStackState {
    private CapturedAnimalStackState() {
    }

    public static CompoundTag read(ItemStack stack) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag captured = root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG).orElse(null);
        return captured == null ? new CompoundTag() : CapturedEntityState.sanitize(captured);
    }

    public static void write(ItemStack stack, CompoundTag state) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root ->
                root.put(NetTrapBlock.CAPTURED_ENTITY_TAG, CapturedEntityState.sanitize(state)));
    }

    public static CompoundTag forAdult(ItemStack source) {
        return CapturedEntityState.asAdult(read(source));
    }

    public static CompoundTag forOffspring(ItemStack donorParent) {
        CompoundTag inherited = read(donorParent);
        for (String key : new HashSet<>(inherited.keySet())) {
            if (!CapturedEntityState.isOffspringInheritedKey(key)) {
                inherited.remove(key);
            }
        }
        return CapturedEntityState.asNewborn(inherited);
    }
}
