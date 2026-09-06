package com.nstut.biotech.data;

import com.nstut.biotech.models.Crop;
import com.nstut.biotech.models.Drop;

import java.util.List;

public class CropData {

    final static Crop WHEAT = new Crop("wheat_seeds", List.of(
        new Drop("wheat", 2),
        new Drop("wheat_seeds", 3)
    ));
    final static Crop BEETROOT = new Crop("beetroot_seeds", List.of(
        new Drop("beetroot", 2),
        new Drop("beetroot_seeds", 3)
    ));
    final static Crop CARROT = new Crop("carrot", List.of(
        new Drop("carrot", 4)
    ));
    final static Crop POTATO = new Crop("potato", List.of(
        new Drop("potato", 4),
        new Drop("poisonous_potato", 1, 0.02f)
    ));
    final static Crop MELON = new Crop("melon_seeds", List.of(
        new Drop("melon", 1)
    ));
    final static Crop PUMPKIN = new Crop("pumpkin_seeds", List.of(
        new Drop("pumpkin", 1)
    ));
    // cactus and sugarcane
    final static Crop CACTUS = new Crop("cactus", List.of(
        new Drop("cactus", 3)
    ));
    final static Crop SUGARCANE = new Crop("sugar_cane", List.of(
        new Drop("sugar_cane", 3)
    ));

    public static final List<Crop> CROPS = List.of(
        WHEAT,
        BEETROOT,
        CARROT,
        POTATO,
        MELON,
        PUMPKIN,
        CACTUS,
        SUGARCANE
    );
}
