package com.nstut.biotech.jei;

import com.google.gson.JsonParser;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Connection-scoped server estimates. Contains no machine transaction state. */
public final class SlaughterhouseLootPreview {
    private static Map<String, List<ItemStack>> outputs = Map.of();
    private static Map<String, List<Double>> estimates = Map.of();
    private static Runnable refresh = () -> {};
    private SlaughterhouseLootPreview() {}
    public static List<ItemStack> outputs(String recipe) {
        return outputs.getOrDefault(recipe, List.of()).stream().map(ItemStack::copy).toList();
    }
    public static void receive(String json) {
        Map<String, List<ItemStack>> updated = new HashMap<>();
        Map<String, List<Double>> updatedEstimates = new HashMap<>();
        for (var entry : JsonParser.parseString(json).getAsJsonObject().entrySet()) {
            var stacks = new java.util.ArrayList<ItemStack>();
            var means = new java.util.ArrayList<Double>();
            for (var data : entry.getValue().getAsJsonArray()) {
                var object = data.getAsJsonObject();
                var item = LootPreviewRegistry.item(object.get("item").getAsString());
                if (item != null) {
                    stacks.add(new ItemStack(item));
                    means.add(object.get("mean").getAsDouble());
                }
            }
            updated.put(entry.getKey(), List.copyOf(stacks));
            updatedEstimates.put(entry.getKey(), List.copyOf(means));
        }
        outputs = Map.copyOf(updated);
        estimates = Map.copyOf(updatedEstimates);
        refresh.run();
    }
    public static double estimate(String recipe, int index) {
        var values = estimates.getOrDefault(recipe, List.of());
        return index < values.size() ? values.get(index) : 0;
    }
    public static void onRefresh(Runnable callback) { refresh = callback; }
    public static void clear() { outputs = Map.of(); estimates = Map.of(); refresh = () -> {}; }
}
