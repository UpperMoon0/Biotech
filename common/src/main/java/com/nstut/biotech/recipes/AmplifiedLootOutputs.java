package com.nstut.biotech.recipes;

import com.nstut.nstutlib.recipes.OutputItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Normalize post-roll yields before they enter the persistent machine transaction. */
public final class AmplifiedLootOutputs {
    // The provider recipe network/snapshot contract permits at most 256 output entries.
    private static final int MAX_OUTPUTS = 256;

    private AmplifiedLootOutputs() {}

    public static OutputItem[] split(List<ItemStack> rolled, int multiplier) {
        if (multiplier < 1 || multiplier > 64) {
            throw new IllegalArgumentException("Slaughterhouse yield multiplier must be between 1 and 64");
        }
        List<OutputItem> outputs = new ArrayList<>();
        for (ItemStack stack : rolled) {
            if (stack.isEmpty()) continue;
            long remaining = (long) stack.getCount() * multiplier;
            // Modern ItemStack/ItemStackTemplate persistence accepts only counts 1..99.
            int stackLimit = Math.min(99, stack.getMaxStackSize());
            while (remaining > 0) {
                if (outputs.size() >= MAX_OUTPUTS) {
                    // Fail before consumption rather than creating an unsavable transaction.
                    throw new IllegalStateException("Amplified loot exceeds the 256-stack transaction limit");
                }
                int count = (int) Math.min(remaining, stackLimit);
                ItemStack output = stack.copy();
                output.setCount(count);
                outputs.add(new OutputItem(output, 1.0f));
                remaining -= count;
            }
        }
        return outputs.toArray(OutputItem[]::new);
    }
}
