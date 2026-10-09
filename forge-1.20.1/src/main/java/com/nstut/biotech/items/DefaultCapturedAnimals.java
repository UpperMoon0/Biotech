package com.nstut.biotech.items;

import com.nstut.biotech.data.TerrestrialAnimalCatalog;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.List;

public final class DefaultCapturedAnimals {
    private DefaultCapturedAnimals() {}
    public static List<ItemStack> stacks() {
        List<ItemStack> result = new ArrayList<>();
        for (var animal : TerrestrialAnimalCatalog.extras(0)) {
            result.add(stack(animal.id(), false));
            if (animal.baby()) result.add(stack(animal.id(), true));
        }
        return result;
    }
    private static ItemStack stack(String type, boolean baby) {
        ItemStack stack = new ItemStack(ItemRegistries.CAPTURED_ANIMAL.get());
        CompoundTag state = new CompoundTag(); state.putInt("Age", baby ? -24000 : 0);
        CapturedAnimalStackState.writeCapture(stack, state, "minecraft:" + type, -1);
        return stack;
    }
}
