# JEI recipe presentation

All six machine categories use the same slot construction and native JEI scrolling grids on Forge 1.20.1, NeoForge 1.21.1 and NeoForge 26.1.2.

- Inputs and outputs each have a three-column, three-row window. Item and fluid entries share an ordered grid, so a second item row can never overlap a fluid row. Longer recipes remain searchable and scrollable rather than silently losing outputs.
- Chance badges belong to the actual output slot and therefore move and receive hover tooltips correctly when scrolled. Guaranteed outputs have no chance badge. Compact percentages use `~` when rounded, `<0.1%` for very small positive probabilities, and `>99.9%` for near-guaranteed probabilities.
- Chance tooltips show the unrounded percentage and the stored recipe float's round-trip decimal representation. This preserves the recipe model's available precision; it does not pretend that decimals discarded while parsing a JSON number into a float can be recovered.
- Non-consumable ingredients use JEI's non-consumable catalyst role (renamed `CRAFTING_STATION` in JEI 20+), an `R` badge, a visible legend, and an explicit tooltip. Animal inputs disclose adult/baby requirements, including generic captured-animal selectors.
- Energy is per cycle. Minimum cycle time uses integer processing ticks, rounding up a partial final tick. Rates assume 20 TPS, continuous power and inputs, and free output storage. Probabilistic item rates are expected values, not guarantees. All per-minute estimates are marked approximate.
- Empty Slaughterhouse item-output lists advertise random entity loot and the server-synchronized configured yield multiplier. Authored nonempty lists display their actual static outputs and chances. Renewable Habitat output tooltips include item or mB quantities per cycle and production rates; wool variants are disclosed.

## Automated checks

`./gradlew testAllVersions` includes the registry-free probability, layout and production-math tests. These exercise precision edge cases, 100,000 representative probabilities, every combined cardinality from zero through 512 slots (the codec permits 256 items plus 256 fluids), every scrolling window, non-overlapping geometry, exact-tick boundaries and rate calculations. Source contract tests only check that all target categories retain the shared wiring.

`./gradlew :neoforge-1.21.1:renderJeiPreviews` starts a separate development client using the GameTest world. On a headless machine run it under `xvfb-run -a`; its game directory and outputs are under the target's `build/` directory. This test loads the real JEI runtime and production categories, constructs actual JEI layouts and tooltips, invokes real scroll handlers, asserts the final item/fluid slots are reachable, and writes 14 screenshots to `neoforge-1.21.1/build/jei-previews/`. It covers:

1. All six categories with probabilistic item outputs and multiple fluids
2. Independently scrolling long input/output lists to their final fluids
3. Tiny, fractional and near-certain chance tooltips
4. Adult reusable-catalyst disclosure
5. A Habitat with three items and two fluid outputs, including milk rates
6. Both authored static and dynamic Slaughterhouse presentation

`manifest.json` is written only after all assertions and captures succeed. The Gradle verification task requires all 14 distinct 1024×832 screenshots. Inspect the screenshots for label legibility and clipping; this is not a pixel-identical golden-image test. The existing 56-case OpenUI preview task remains unchanged when JEI preview mode is off. Neither preview runner is included in release JARs.
