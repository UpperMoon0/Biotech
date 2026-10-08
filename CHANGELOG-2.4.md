# Biotech 2.4

Biotech 2.4 completes the stateful captured-animal processing pipeline across Forge 1.20.1, NeoForge 1.21.1, and NeoForge 26.1.2.

## Added

- Stateful Breeding Chamber offspring with deterministic variant/genetic inheritance and unchanged parent items.
- Per-individual Terrestrial Habitat growth, including batch recipes and legacy sheep-color compatibility.
- Renewable adult chicken eggs, colored sheep wool, and fluid milk, using food, water, energy, and non-consumed animals.
- Entity-loot-driven default Slaughterhouse recipes with configurable safe yield amplification and persisted output rolls.
- Exact JEI chance tooltips, persistent-input information, production timing, and scrollable layouts for large custom recipes.
- Cross-target livestock transaction regressions and opt-in full validation for draft pull requests.

## Fixed

- Strip modern leash and fall-distance fields from captured state.
- Bind prepared outputs to the actual selected animals while a transaction waits for output capacity.
- Preserve each baby's own state when growing multiple babies in one recipe.
- Generate milk recipes without accessing uninitialized game registries and enable the runtime milk fluid explicitly.
- Honor concrete entity loot tables, including Forge custom death-loot overrides, and split amplified loot before snapshot serialization.
- Keep explicit custom Slaughterhouse output lists static and consistent with their JEI presentation.

- Throttle rejected oversized animal/loot preparation through the safe transaction failure path, preserving structure and resources across retries.
- Index all sheep wool colors in JEI output searches, including focused tier-1 and tier-2 production lookups.
- Display recipe fluid products and their quantities in controller output lanes, including Habitat milk.
- Explain deterministic first-selected-parent inheritance in the in-game Breeding Chamber guide.

## Compatibility

Existing captured-item IDs and recipe schemas remain supported. See [the 2.4 upgrade guide](docs/upgrade-2.4.md) for inheritance rules, static/dynamic Slaughterhouse recipes, renewable production, configuration, and verification commands.
