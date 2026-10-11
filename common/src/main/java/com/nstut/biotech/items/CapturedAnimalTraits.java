package com.nstut.biotech.items;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Readable vanilla trait fields; no raw NBT, entity IDs or owner UUIDs in player tooltips. */
public final class CapturedAnimalTraits {
    private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private CapturedAnimalTraits() {}
    public static List<Component> describe(String type, JsonObject state) {
        List<Component> lines = new ArrayList<>();
        if (state.has("Age")) add(lines, "age", state.get("Age").getAsInt() < 0 ? "Baby" : "Adult");
        if (type.equals("minecraft:sheep")) color(lines, state, "Color", "wool");
        color(lines, state, "CollarColor", "collar");
        if (type.equals("minecraft:horse") && state.has("Variant")) {
            int variant = state.get("Variant").getAsInt();
            add(lines, "coat", index(new String[]{"White", "Creamy", "Chestnut", "Brown", "Black", "Gray", "Dark brown"}, variant & 255));
            add(lines, "markings", index(new String[]{"None", "White", "White field", "White spots", "Black spots"}, (variant >> 8) & 255));
        } else if (type.equals("minecraft:parrot") && state.has("Variant")) {
            add(lines, "variant", index(new String[]{"Red and blue", "Blue", "Green", "Yellow and blue", "Gray"}, state.get("Variant").getAsInt()));
        } else if ((type.equals("minecraft:llama") || type.equals("minecraft:trader_llama")) && state.has("Variant")) {
            add(lines, "coat", index(new String[]{"Creamy", "White", "Brown", "Gray"}, state.get("Variant").getAsInt()));
        } else { field(lines, state, "Variant", "variant"); }
        for (String key : new String[]{"variant", "CatType", "Type", "RabbitType", "MainGene", "HiddenGene", "main_gene", "hidden_gene", "Strength", "sound_variant"}) {
            if (!state.has(key)) continue;
            String value = state.get(key).isJsonPrimitive() ? state.get(key).getAsString() : "";
            if (key.equals("RabbitType")) value = value.equals("99") ? "Killer bunny" : index(new String[]{"Brown", "White", "Black", "Black and white", "Gold", "Salt and pepper"}, Integer.parseInt(value));
            if ((key.equals("MainGene") || key.equals("HiddenGene")) && value.matches("[0-9]+")) value = index(new String[]{"Normal", "Lazy", "Worried", "Playful", "Brown", "Weak", "Aggressive"}, Integer.parseInt(value));
            String label = key.toLowerCase(Locale.ROOT).contains("hidden") ? "hidden_gene" : key.toLowerCase(Locale.ROOT).contains("gene") ? "gene"
                    : key.equals("Strength") ? "strength" : key.equals("sound_variant") ? "voice" : "variant";
            if (!value.isEmpty()) add(lines, label, readable(value));
        }
        for (var entry : java.util.Map.ofEntries(
                java.util.Map.entry("Sheared", "sheared"), java.util.Map.entry("IsScreamingGoat", "screaming"),
                java.util.Map.entry("HasLeftHorn", "left_horn"), java.util.Map.entry("HasRightHorn", "right_horn"),
                java.util.Map.entry("Tame", "tamed"), java.util.Map.entry("Sitting", "sitting"),
                java.util.Map.entry("ChestedHorse", "chest"), java.util.Map.entry("Saddle", "saddle"),
                java.util.Map.entry("HasNectar", "nectar"), java.util.Map.entry("HasStung", "stung"),
                java.util.Map.entry("HasEgg", "egg"), java.util.Map.entry("IsImmuneToZombification", "immune"),
                java.util.Map.entry("has_egg", "egg"), java.util.Map.entry("Sleeping", "sleeping"),
                java.util.Map.entry("IsChickenJockey", "jockey"), java.util.Map.entry("IsTrusting", "trusting"),
                java.util.Map.entry("Bred", "bred"), java.util.Map.entry("EatingHaystack", "eating")).entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).toList()) {
            if (state.has(entry.getKey()) && state.get(entry.getKey()).isJsonPrimitive()) {
                String value = state.get(entry.getKey()).getAsString();
                add(lines, entry.getValue(), value.equals("true") || value.equals("1") ? "Yes" : "No");
            }
        }
        field(lines, state, "Temper", "temper");
        field(lines, state, "state", "state");
        if (state.has("Owner") || state.has("owner")) add(lines, "owner", "Assigned");
        if (state.has("SaddleItem") || state.has("saddle")) add(lines, "saddle", "Yes");
        for (String field : new String[]{"Items", "items"}) {
            if (state.has(field) && state.get(field).isJsonArray() && !state.getAsJsonArray(field).isEmpty()) {
                add(lines, "cargo", Integer.toString(state.getAsJsonArray(field).size()));
            }
        }
        return lines;
    }
    private static void field(List<Component> lines, JsonObject state, String key, String label) {
        if (state.has(key) && state.get(key).isJsonPrimitive()) add(lines, label, readable(state.get(key).getAsString()));
    }
    private static void color(List<Component> lines, JsonObject state, String key, String label) {
        if (state.has(key)) {
            int color = state.get(key).getAsInt();
            if (color >= 0 && color < COLORS.length) lines.add(Component.translatable("tooltip.biotech.trait." + label, Component.translatable("color.minecraft." + COLORS[color])));
        }
    }
    private static String index(String[] values, int index) { return index >= 0 && index < values.length ? values[index] : Integer.toString(index); }
    public static String readable(String value) {
        String name = value.substring(value.lastIndexOf(':') + 1).replace('_', ' ');
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    private static void add(List<Component> lines, String label, String value) { lines.add(Component.translatable("tooltip.biotech.trait." + label, value)); }
}
