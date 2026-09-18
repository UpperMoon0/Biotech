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
        if (!root.contains(NetTrapBlock.CAPTURED_ENTITY_TAG)) {
            return new CompoundTag();
        }
        return CapturedEntityState.sanitize(root.getCompound(NetTrapBlock.CAPTURED_ENTITY_TAG));
    }

    public static void write(ItemStack stack, CompoundTag state) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root ->
                root.put(NetTrapBlock.CAPTURED_ENTITY_TAG, CapturedEntityState.sanitize(state)));
    }

    public static void writeCapture(ItemStack stack, CompoundTag state, String entityTypeId, int sheepColor) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            root.put(NetTrapBlock.CAPTURED_ENTITY_TAG, CapturedEntityState.sanitize(state));
            root.putString(CapturedAnimalItem.ENTITY_TYPE_TAG, entityTypeId);
            if (sheepColor >= 0) {
                root.putInt("SheepColor", sheepColor);
            } else {
                root.remove("SheepColor");
            }
        });
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
