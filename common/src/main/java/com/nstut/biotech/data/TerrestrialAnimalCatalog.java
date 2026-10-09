package com.nstut.biotech.data;

import java.util.List;

/** Vanilla non-aquatic Animal types. Food and lifecycle exceptions are explicit, not inferred. */
public final class TerrestrialAnimalCatalog {
    public record Animal(String id, String food, boolean breeds, boolean baby, String egg, int since) {}
    public static final List<Animal> EXTRA = List.of(
            animal("horse", "golden_carrot"), animal("donkey", "golden_carrot"),
            new Animal("mule", "golden_carrot", false, true, "", 0),
            new Animal("skeleton_horse", "", false, true, "", 0),
            new Animal("zombie_horse", "", false, true, "", 0),
            animal("llama", "hay_block"),
            new Animal("trader_llama", "hay_block", true, true, "", 0),
            animal("camel", "cactus"), animal("goat", "wheat"),
            animal("wolf", "beef"), animal("cat", "cod"), animal("ocelot", "cod"),
            animal("fox", "sweet_berries"), animal("panda", "bamboo"),
            new Animal("polar_bear", "", false, true, "", 0),
            new Animal("parrot", "", false, false, "", 0),
            animal("bee", "dandelion"),
            new Animal("turtle", "seagrass", true, true, "turtle_egg", 0),
            new Animal("frog", "slime_ball", true, false, "frogspawn", 0),
            new Animal("sniffer", "torchflower_seeds", true, true, "sniffer_egg", 0),
            animal("mooshroom", "wheat"), animal("hoglin", "crimson_fungus"),
            animal("strider", "warped_fungus"),
            new Animal("armadillo", "spider_eye", true, true, "", 121),
            new Animal("happy_ghast", "snowball", false, true, "", 261),
            new Animal("camel_husk", "", false, false, "", 261));

    private TerrestrialAnimalCatalog() {}
    private static Animal animal(String id, String food) { return new Animal(id, food, true, true, "", 0); }
    public static List<Animal> extras(int version) { return EXTRA.stream().filter(a -> a.since() <= version).toList(); }
    public static List<String> types(int version) {
        var types = new java.util.ArrayList<>(List.of("cow", "chicken", "pig", "sheep", "rabbit"));
        extras(version).forEach(a -> types.add(a.id()));
        return List.copyOf(types);
    }
    public static int recipeCount(int version) {
        // One slaughter and renewable habitat recipe per type; optional growth/breeding, plus mule hybrid.
        return 71 + extras(version).stream().mapToInt(a -> 2 + (a.baby() ? 1 : 0) + (a.breeds() ? 1 : 0)).sum();
    }
}
