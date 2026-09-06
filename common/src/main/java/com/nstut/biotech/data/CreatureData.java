package com.nstut.biotech.data;

import com.nstut.biotech.models.Creature;
import com.nstut.biotech.models.Drop;
import com.nstut.biotech.models.Food;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CreatureData {

    public final static Creature CREATURE_CHICKEN = new Creature("chicken");
    public final static Creature CREATURE_COW = new Creature("cow");
    public final static Creature CREATURE_PIG = new Creature("pig");
    public final static Creature CREATURE_SHEEP = new Creature("sheep");
    public final static Creature CREATURE_RABBIT = new Creature("rabbit");

    public final static String FLUID_WATER = "minecraft:water";

    public final static List<Creature> CREATURES = List.of(
            CREATURE_CHICKEN,
            CREATURE_COW,
            CREATURE_PIG,
            CREATURE_SHEEP,
            CREATURE_RABBIT
    );

    public final static HashMap<Creature, List<Food>> FOODS = new HashMap<>() {{
        put(CREATURE_CHICKEN, Arrays.asList(
                new Food("minecraft:wheat_seeds", 1),
                new Food("minecraft:beetroot_seeds", 1),
                new Food("minecraft:melon_seeds", 1),
                new Food("minecraft:pumpkin_seeds", 1),
                new Food("minecraft:torchflower_seeds", 1),
                new Food("minecraft:pitcher_pod", 1),
                new Food("biotech:chicken_feed", 2)
        ));
        put(CREATURE_COW, List.of(
                new Food("minecraft:wheat", 1),
                new Food("biotech:cow_feed", 2)
        ));
        put(CREATURE_PIG, List.of(
                new Food("minecraft:carrot", 1),
                new Food("minecraft:potato", 1),
                new Food("minecraft:beetroot", 1),
                new Food("biotech:pig_feed", 2)
        ));
        put(CREATURE_SHEEP, List.of(
                new Food("minecraft:wheat", 1),
                new Food("biotech:sheep_feed", 2)
        ));
        put(CREATURE_RABBIT, List.of(
                new Food("minecraft:carrot", 1),
                new Food("minecraft:golden_carrot", 1),
                new Food("minecraft:dandelion", 1),
                new Food("biotech:rabbit_feed", 2)
        ));
    }};

    public final static HashMap<Creature, List<Drop>> DROPS = new HashMap<>() {{
        put(CREATURE_CHICKEN, List.of(
                new Drop("minecraft:chicken", 2),
                new Drop("minecraft:bone", 1),
                new Drop("minecraft:feather", 3)
        ));
        put(CREATURE_COW, List.of(
                new Drop("minecraft:beef", 4),
                new Drop("minecraft:bone", 2),
                new Drop("minecraft:leather", 3)
        ));
        put(CREATURE_PIG, List.of(
                new Drop("minecraft:porkchop", 4),
                new Drop("minecraft:bone", 2)
        ));
        put(CREATURE_SHEEP, List.of(
                new Drop("minecraft:mutton", 3),
                new Drop("minecraft:bone", 2),
                new Drop("minecraft:white_wool", 1)
        ));
        put(CREATURE_RABBIT, List.of(
                new Drop("minecraft:rabbit", 2),
                new Drop("minecraft:bone", 1),
                new Drop("minecraft:rabbit_hide", 2),
                new Drop("minecraft:rabbit_foot", 1, 0.13f)
        ));
    }};
}