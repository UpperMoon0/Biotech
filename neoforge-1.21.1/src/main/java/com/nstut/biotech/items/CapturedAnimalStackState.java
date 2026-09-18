package com.nstut.biotech.items;

import com.nstut.biotech.blocks.NetTrapBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
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

    public static String entityTypeId(ItemStack stack) {
        if (stack.getItem() instanceof MobItem mobItem && mobItem.entityType() != null) {
            return EntityType.getKey(mobItem.entityType()).toString();
        }
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return root.contains(CapturedAnimalItem.ENTITY_TYPE_TAG)
                ? root.getString(CapturedAnimalItem.ENTITY_TYPE_TAG)
                : "";
    }

    public static void writeDerived(ItemStack target, ItemStack source, CompoundTag state) {
        String entityTypeId = entityTypeId(source);
        if (entityTypeId.isEmpty()) {
            write(target, state);
            return;
        }
        CompoundTag sourceRoot = source.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int sheepColor = state.contains("Color")
                ? state.getByte("Color")
                : sourceRoot.contains("SheepColor") ? sourceRoot.getInt("SheepColor") : -1;
        writeCapture(target, state, entityTypeId, sheepColor);
        CustomData.update(DataComponents.CUSTOM_DATA, target,
                root -> root.remove(CapturedAnimalItem.RECIPE_LIFECYCLE_TAG));
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
