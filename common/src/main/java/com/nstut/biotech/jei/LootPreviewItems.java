package com.nstut.biotech.jei;

import com.google.gson.JsonElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Possible item identities, not a loot roll or a promise of quantities/chances. */
public final class LootPreviewItems {
    private LootPreviewItems() {}
    public static List<String> collect(JsonElement table, Function<String, JsonElement> tables,
                                       Function<String, List<String>> tags) {
        Set<String> items = new LinkedHashSet<>();
        visit(table, tables, tags, new LinkedHashSet<>(), items, 0);
        return List.copyOf(items);
    }
    private static void visit(JsonElement node, Function<String, JsonElement> tables,
                              Function<String, List<String>> tags, Set<String> visited,
                              Set<String> items, int depth) {
        if (node == null || depth > 32) return;
        if (node.isJsonArray()) {
            for (var child : node.getAsJsonArray()) visit(child, tables, tags, visited, items, depth + 1);
        } else if (node.isJsonObject()) {
            var object = node.getAsJsonObject();
            String type = object.has("type") ? object.get("type").getAsString() : "";
            if (type.equals("minecraft:item") && object.has("name")) items.add(object.get("name").getAsString());
            if (type.equals("minecraft:tag") && object.has("name")) items.addAll(tags.apply(object.get("name").getAsString()));
            if (type.equals("minecraft:loot_table")) {
                var ref = object.has("value") ? object.get("value") : object.get("name");
                if (ref != null && ref.isJsonPrimitive()) {
                    String id = ref.getAsString();
                    if (visited.add(id)) visit(tables.apply(id), tables, tags, visited, items, depth + 1);
                } else visit(ref, tables, tags, visited, items, depth + 1);
            }
            for (String key : List.of("pools", "entries", "children"))
                if (object.has(key)) visit(object.get(key), tables, tags, visited, items, depth + 1);
        }
    }
}
