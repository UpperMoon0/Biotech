# Machine construction and material budget

Generated from `design/machines.json`. Layers in the guide and NsTut Lib preview are the exact native validator data. Dimensions are width × depth × height. All directions below assume a south-facing controller; rotate the entire structure and port facings together.

## Parts and crafting cost

Every new part recipe consumes 2 original Biotech casings plus 1 common component and yields 4 parts. Each four-part batch therefore costs 2 polished deepslate + 2 iron ingots + 0.5 emerald, plus its component (before batch rounding). Components: frame = iron nugget; enamel = quartz; vent = iron bars; vessel/pipe = copper ingot; drive = redstone; grow light = glowstone dust; inspection glass = glass. Original casing, controller and hatch recipes are unchanged.

No additional processing time, FE, input/output quantity, chance, tier or recipe changes are introduced. Surplus parts are reusable across all six machines.

## Greenhouse

7 × 7 growing bed with a five-block pitched glazed roof. Twenty farmland cells flank a water channel; roof lights and ridge ventilation explain the growing function. All four ports stay at ground level.

New size: **7 × 7 × 5**. New placed blocks (including controller, hatches, floors and water; excluding air): **143**.

| Part | Count |
| --- | ---: |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_output_hatch[facing=east] | 1 |
| biotech:energy_input_hatch[facing=north] | 1 |
| biotech:fluid_input_hatch[facing=north] | 1 |
| biotech:greenhouse[facing=south] | 1 |
| biotech:biotech_frame | 20 |
| biotech:biotech_framed_glass | 60 |
| biotech:biotech_grow_light | 7 |
| biotech:biotech_panel | 24 |
| biotech:biotech_vent | 2 |
| minecraft:farmland[moisture=7] | 20 |
| minecraft:water[level=0] | 5 |

| Port | Controller-relative offset (south) | Faces |
| --- | --- | --- |
| 1: item_input_hatch | (-3, 0, -3) | west |
| 2: item_output_hatch | (3, 0, -3) | east |
| 3: energy_input_hatch | (1, 0, -6) | north |
| 4: fluid_input_hatch | (-1, 0, -6) | north |

2.5 baseline: **166 placed required blocks**, including **63 original casings**. New specialized parts: **113** (29 four-part crafting batches, **58 original casings** plus 29 common components when crafted independently). New original casings: 0. Controller/hatch costs remain unchanged. The floor/water counts above are retained functional materials; all manufactured surfaces now use Biotech parts.

## Breeding Chamber

7 × 5 × 4 paired incubation bays with a center partition and two roof light banks. West inputs remain ordered parent A, parent B, food; east collects offspring. The compact clinical enclosure is distinct from the open habitat.

New size: **7 × 5 × 4**. New placed blocks (including controller, hatches, floors and water; excluding air): **106**.

| Part | Count |
| --- | ---: |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_output_hatch[facing=east] | 1 |
| biotech:energy_input_hatch[facing=north] | 1 |
| biotech:fluid_input_hatch[facing=north] | 1 |
| biotech:breeding_chamber[facing=south] | 1 |
| biotech:biotech_frame | 19 |
| biotech:biotech_framed_glass | 21 |
| biotech:biotech_grow_light | 8 |
| biotech:biotech_panel | 36 |
| biotech:biotech_vent | 6 |
| minecraft:grass_block[snowy=false] | 9 |

| Port | Controller-relative offset (south) | Faces |
| --- | --- | --- |
| 1: item_input_hatch | (-3, -1, -3) | west |
| 2: item_input_hatch | (-3, -1, -1) | west |
| 3: item_input_hatch | (-3, 0, -1) | west |
| 4: item_output_hatch | (3, -1, -2) | east |
| 5: energy_input_hatch | (1, -1, -4) | north |
| 6: fluid_input_hatch | (-1, -1, -4) | north |

2.5 baseline: **134 placed required blocks**, including **75 original casings**. New specialized parts: **90** (24 four-part crafting batches, **48 original casings** plus 24 common components when crafted independently). New original casings: 0. Controller/hatch costs remain unchanged. The floor/water counts above are retained functional materials; all manufactured surfaces now use Biotech parts.

## Terrestrial Habitat

7 × 7 × 5 animal-care habitat with a two-block-wide, three-block-high walk-in entrance beside the front control station. A level service path opens into the grass yard and a three-block-high illuminated rear shelter. Continuous corner posts support the canopy and entrance lintel; seven exposed ports retain their 2.6 positions.

New size: **7 × 7 × 5**. New placed blocks (including controller, hatches, floors and water; excluding air): **146**.

| Part | Count |
| --- | ---: |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_output_hatch[facing=east] | 1 |
| biotech:energy_input_hatch[facing=north] | 1 |
| biotech:fluid_input_hatch[facing=north] | 1 |
| biotech:fluid_output_hatch[facing=east] | 1 |
| biotech:terrestrial_habitat[facing=south] | 1 |
| biotech:biotech_frame | 37 |
| biotech:biotech_framed_glass | 18 |
| biotech:biotech_grow_light | 2 |
| biotech:biotech_panel | 56 |
| biotech:biotech_vent | 7 |
| minecraft:grass_block[snowy=false] | 18 |

| Port | Controller-relative offset (south) | Faces |
| --- | --- | --- |
| 1: item_input_hatch | (-3, -1, -5) | west |
| 2: item_input_hatch | (-3, -1, -3) | west |
| 3: item_input_hatch | (-3, -1, -1) | west |
| 4: item_output_hatch | (3, -1, -3) | east |
| 5: energy_input_hatch | (1, -1, -6) | north |
| 6: fluid_input_hatch | (-1, -1, -6) | north |
| 7: fluid_output_hatch | (3, -1, -6) | east |

2.5 baseline: **141 placed required blocks**, including **84 original casings**. New specialized parts: **120** (32 four-part crafting batches, **64 original casings** plus 32 common components when crafted independently). New original casings: 0. Controller/hatch costs remain unchanged. The floor/water counts above are retained functional materials; all manufactured surfaces now use Biotech parts.

## Slaughterhouse

5 × 7 × 4 sealed processing line. West intake, internal drive units and east discharge communicate the flow. Narrow inspection windows and a vented spine replace the old red glass tower; no decorative gore.

New size: **5 × 7 × 4**. New placed blocks (including controller, hatches, floors and water; excluding air): **104**.

| Part | Count |
| --- | ---: |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_output_hatch[facing=east] | 1 |
| biotech:energy_input_hatch[facing=north] | 1 |
| biotech:fluid_input_hatch[facing=north] | 1 |
| biotech:slaughterhouse[facing=south] | 1 |
| biotech:biotech_drive | 2 |
| biotech:biotech_frame | 18 |
| biotech:biotech_framed_glass | 6 |
| biotech:biotech_grow_light | 4 |
| biotech:biotech_panel | 61 |
| biotech:biotech_vent | 7 |

| Port | Controller-relative offset (south) | Faces |
| --- | --- | --- |
| 1: item_input_hatch | (-2, -1, -5) | west |
| 2: item_input_hatch | (-2, -1, -3) | west |
| 3: item_output_hatch | (2, -1, -4) | east |
| 4: energy_input_hatch | (1, -1, -6) | north |
| 5: fluid_input_hatch | (-1, -1, -6) | north |

2.5 baseline: **101 placed required blocks**, including **75 original casings**. New specialized parts: **98** (27 four-part crafting batches, **54 original casings** plus 27 common components when crafted independently). New original casings: 0. Controller/hatch costs remain unchanged. The floor/water counts above are retained functional materials; all manufactured surfaces now use Biotech parts.

## Mixer

5 × 5 × 6 compact driven vessel, replacing a 12 × 5 × 10 gantry. A top drive and central service shaft read as an agitator. Three distinct fluid feeds face west; low east item/fluid discharge stays accessible. The controller sits in a one-block front recess.

New size: **5 × 5 × 6**. New placed blocks (including controller, hatches, floors and water; excluding air): **75**.

| Part | Count |
| --- | ---: |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_output_hatch[facing=east] | 1 |
| biotech:energy_input_hatch[facing=north] | 1 |
| biotech:fluid_input_hatch[facing=west] | 1 |
| biotech:fluid_input_hatch[facing=west] | 1 |
| biotech:fluid_input_hatch[facing=west] | 1 |
| biotech:fluid_output_hatch[facing=east] | 1 |
| biotech:mixer[facing=south] | 1 |
| biotech:biotech_drive | 2 |
| biotech:biotech_frame | 15 |
| biotech:biotech_framed_glass | 4 |
| biotech:biotech_panel | 11 |
| biotech:biotech_pipe | 7 |
| biotech:biotech_vessel | 26 |
| biotech:biotech_vent | 2 |

| Port | Controller-relative offset (south) | Faces |
| --- | --- | --- |
| 1: item_input_hatch | (-2, -1, -1) | west |
| 2: item_output_hatch | (2, -1, -1) | east |
| 3: energy_input_hatch | (0, -1, -3) | north |
| 4: fluid_input_hatch | (-2, 2, -1) | west |
| 5: fluid_input_hatch | (-2, 2, 0) | west |
| 6: fluid_input_hatch | (-2, 2, 1) | west |
| 7: fluid_output_hatch | (2, -1, -2) | east |

2.5 baseline: **285 placed required blocks**, including **97 original casings**. New specialized parts: **67** (19 four-part crafting batches, **38 original casings** plus 19 common components when crafted independently). New original casings: 0. Controller/hatch costs remain unchanged. The floor/water counts above are retained functional materials; all manufactured surfaces now use Biotech parts.

## Fermenter

7 × 7 × 6 twin tall sealed fermentation chambers with inspection strip and two passive vent stacks. A short front service vestibule distinguishes it from the single driven Mixer vessel. Feed/output hatches sit at ground level, avoiding the old raised connections.

New size: **7 × 7 × 6**. New placed blocks (including controller, hatches, floors and water; excluding air): **151**.

| Part | Count |
| --- | ---: |
| biotech:item_input_hatch[facing=west] | 1 |
| biotech:item_output_hatch[facing=east] | 1 |
| biotech:energy_input_hatch[facing=north] | 1 |
| biotech:fluid_input_hatch[facing=north] | 1 |
| biotech:fermenter[facing=south] | 1 |
| biotech:biotech_frame | 24 |
| biotech:biotech_framed_glass | 9 |
| biotech:biotech_grow_light | 2 |
| biotech:biotech_panel | 71 |
| biotech:biotech_pipe | 4 |
| biotech:biotech_vessel | 30 |
| biotech:biotech_vent | 6 |

| Port | Controller-relative offset (south) | Faces |
| --- | --- | --- |
| 1: item_input_hatch | (-3, -1, -1) | west |
| 2: item_output_hatch | (3, -1, -1) | east |
| 3: energy_input_hatch | (0, -1, -6) | north |
| 4: fluid_input_hatch | (2, -1, -6) | north |

2.5 baseline: **419 placed required blocks**, including **195 original casings**. New specialized parts: **146** (39 four-part crafting batches, **78 original casings** plus 39 common components when crafted independently). New original casings: 0. Controller/hatch costs remain unchanged. The floor/water counts above are retained functional materials; all manufactured surfaces now use Biotech parts.

